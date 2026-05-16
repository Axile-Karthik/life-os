package com.karthik.lifeos.tracker

import android.app.Application
import com.karthik.lifeos.tracker.data.local.AppDatabase
import com.karthik.lifeos.tracker.logging.TrackerLogger

/**
 * Custom Application class.
 * Initializes TrackerLogger with the database DAO on app start.
 */
class GameTimeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize TrackerLogger with Room DAO
        val db = AppDatabase.getInstance(this)
        TrackerLogger.init(db.trackerLogDao())
    }
}
