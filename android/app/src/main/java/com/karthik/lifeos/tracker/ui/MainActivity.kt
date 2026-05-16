package com.karthik.lifeos.tracker.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.karthik.lifeos.tracker.R
import com.karthik.lifeos.tracker.data.local.AppDatabase
import com.karthik.lifeos.tracker.data.local.SessionEndReason
import com.karthik.lifeos.tracker.data.local.SessionState
import com.karthik.lifeos.tracker.logging.TrackerLogger
import com.karthik.lifeos.tracker.tracker.EventType
import com.karthik.lifeos.tracker.tracker.SessionManager
import com.karthik.lifeos.tracker.tracker.SessionScanWorker
import com.karthik.lifeos.tracker.tracker.TrackerService
import com.karthik.lifeos.tracker.tracker.UsageTracker
import com.karthik.lifeos.tracker.worker.CleanupWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var statsAdapter: StatsAdapter
    private lateinit var rvSessions: RecyclerView
    private lateinit var rvStats: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvStatsEmpty: TextView
    private lateinit var tvTrackingStatus: TextView
    private lateinit var switchTracking: MaterialSwitch
    private lateinit var fabScan: FloatingActionButton

    private val db by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        // Bind views
        rvSessions = findViewById(R.id.rvSessions)
        rvStats = findViewById(R.id.rvStats)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvStatsEmpty = findViewById(R.id.tvStatsEmpty)
        tvTrackingStatus = findViewById(R.id.tvTrackingStatus)
        switchTracking = findViewById(R.id.switchTracking)
        fabScan = findViewById(R.id.fabScan)

        // Sessions RecyclerView (vertical)
        sessionAdapter = SessionAdapter()
        rvSessions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = sessionAdapter
        }

        // Stats RecyclerView (horizontal)
        statsAdapter = StatsAdapter()
        rvStats.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity, LinearLayoutManager.HORIZONTAL, false
            )
            adapter = statsAdapter
        }

        // Service toggle
        switchTracking.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!hasUsageStatsPermission()) {
                    switchTracking.isChecked = false
                    showPermissionDialog()
                    return@setOnCheckedChangeListener
                }
                TrackerService.start(this)
                tvTrackingStatus.setText(R.string.tracking_active)
            } else {
                TrackerService.stop(this)
                tvTrackingStatus.setText(R.string.tracking_inactive)
            }
        }

        fabScan.setOnClickListener { performManualScan() }

        // WorkManager as safety-net fallback (runs even if service is killed)
        schedulePeriodicScan()
        scheduleCleanup()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all -> {
                showClearAllDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showClearAllDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear All Data")
            .setMessage("This will delete all tracked sessions and stats. Are you sure?")
            .setPositiveButton("Delete All") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.activitySessionDao().deleteAllSessions()
                    }
                    loadSessions()
                    loadStats()
                    Toast.makeText(this@MainActivity, "All data cleared", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()

        if (!hasUsageStatsPermission()) {
            showPermissionDialog()
        }

        // Sync switch state with service
        switchTracking.isChecked = TrackerService.isRunning
        tvTrackingStatus.setText(
            if (TrackerService.isRunning) R.string.tracking_active else R.string.tracking_inactive
        )

        // Auto-scan on resume: when the user returns to the app after closing a game,
        // this immediately picks up the PAUSED event and heals open sessions.
        // Without this, sessions stay "In progress" until the next periodic scan.
        if (hasUsageStatsPermission()) {
            silentScan()
        } else {
            loadSessions()
            loadStats()
        }
    }

    /**
     * Lightweight scan that runs every time the user returns to the app.
     * Scans the last 5 minutes, heals open sessions, inserts new ones,
     * then refreshes the UI. No toast — runs silently.
     */
    private fun silentScan() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val endTime = System.currentTimeMillis()
                val startTime = endTime - (5 * 60 * 1000L) // Last 5 minutes

                val dao = db.activitySessionDao()

                // Heal open sessions with events from current window
                val usageTracker = UsageTracker(this@MainActivity)
                val currentEvents = usageTracker.queryEvents(startTime, endTime)
                val openSessions = dao.getOpenSessions()

                android.util.Log.d("MainActivity", "silentScan: ${openSessions.size} open session(s) to heal")

                for (session in openSessions) {
                    val pauseEvent = currentEvents.firstOrNull { event ->
                        event.packageName == session.packageName &&
                        event.eventType == EventType.BACKGROUND &&
                        event.timestamp > session.startTime
                    }
                    if (pauseEvent != null) {
                        val duration = pauseEvent.timestamp - session.startTime
                        dao.updateSessionEnd(
                            session.id, pauseEvent.timestamp, duration,
                            SessionState.HEALED.name, SessionEndReason.NORMAL_BACKGROUND.name
                        )
                        android.util.Log.d("MainActivity", "  ✅ Healed (BACKGROUND): ${session.gameName} → ${duration/1000}s")
                    } else {
                        // SMART HEALING: No BACKGROUND event for this game specifically,
                        // but maybe the user switched to ANOTHER app (like this tracker).
                        // Find the first event (any app) that happened after the game started.
                        val switchEvent = currentEvents
                            .filter { it.timestamp > session.startTime && it.packageName != session.packageName }
                            .minByOrNull { it.timestamp }

                        if (switchEvent != null) {
                            val duration = switchEvent.timestamp - session.startTime
                            if (duration >= 5000) { // 5s minimum
                                dao.updateSessionEnd(
                                    session.id, switchEvent.timestamp, duration,
                                    SessionState.IMPLICIT_END.name, SessionEndReason.APP_SWITCH.name
                                )
                                android.util.Log.d("MainActivity", "  ✅ Healed (IMPLICIT): ${session.gameName} ended when ${switchEvent.packageName} started → ${duration/1000}s")
                            } else {
                                // Too short, probably noise
                                android.util.Log.d("MainActivity", "  ⏳ Too short to heal implicitly: ${session.gameName} (${duration}ms)")
                            }
                        } else {
                            android.util.Log.d("MainActivity", "  ❌ Still in progress: ${session.gameName} (started ${(endTime - session.startTime)/1000}s ago)")
                        }
                    }
                }

                // Reconstruct any new sessions
                val sessionManager = SessionManager(this@MainActivity, dao)
                val result = sessionManager.scanForSessions(startTime, endTime)
                if (result.sessions.isNotEmpty()) {
                    dao.insertSessions(result.sessions)
                }
            }

            // Refresh UI after scan
            loadSessions()
            loadStats()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun showPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.grant_permission))
            .setMessage(getString(R.string.permission_required))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            .setCancelable(false)
            .show()
    }

    private fun loadSessions() {
        lifecycleScope.launch {
            val sessions = withContext(Dispatchers.IO) {
                db.activitySessionDao().getAllSessions()
            }
            sessionAdapter.submitList(sessions)
            tvEmptyState.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
            rvSessions.visibility = if (sessions.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun loadStats() {
        lifecycleScope.launch {
            val stats = withContext(Dispatchers.IO) {
                db.activitySessionDao().getActivityStats()
            }
            statsAdapter.submitList(stats)
            tvStatsEmpty.visibility = if (stats.isEmpty()) View.VISIBLE else View.GONE
            rvStats.visibility = if (stats.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun performManualScan() {
        if (!hasUsageStatsPermission()) {
            showPermissionDialog()
            return
        }

        Toast.makeText(this, R.string.scanning, Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val count = withContext(Dispatchers.IO) {
                val endTime = System.currentTimeMillis()
                val startTime = endTime - (60 * 60 * 1000L)

                val dao = db.activitySessionDao()
                val sessionManager = SessionManager(this@MainActivity, dao)
                val result = sessionManager.scanForSessions(startTime, endTime)

                if (result.sessions.isNotEmpty()) {
                    dao.insertSessions(result.sessions)
                }
                result.sessions.size
            }

            Toast.makeText(
                this@MainActivity,
                getString(R.string.scan_complete, count),
                Toast.LENGTH_SHORT
            ).show()

            loadSessions()
            loadStats()
        }
    }

    private fun schedulePeriodicScan() {
        val workRequest = PeriodicWorkRequestBuilder<SessionScanWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SessionScanWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleCleanup() {
        val workRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
