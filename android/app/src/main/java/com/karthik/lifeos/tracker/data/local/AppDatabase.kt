package com.karthik.lifeos.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ActivitySession::class, TrackerLog::class, AppMetrics::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun activitySessionDao(): ActivitySessionDao
    abstract fun trackerLogDao(): TrackerLogDao
    abstract fun appMetricsDao(): AppMetricsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Thread-safe singleton accessor.
         * Double-checked locking ensures only one database instance is created
         * even when accessed from multiple coroutines/threads.
         *
         * Uses fallbackToDestructiveMigration() for pre-production schema evolution.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lifeos_tracker_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
