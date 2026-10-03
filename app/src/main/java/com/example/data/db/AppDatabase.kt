package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.HotspotProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [HotspotProfile::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hotspotProfileDao(): HotspotProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "netshare_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialProfiles(database.hotspotProfileDao())
                    }
                }
            }

            private suspend fun populateInitialProfiles(dao: HotspotProfileDao) {
                dao.insertProfile(
                    HotspotProfile(
                        title = "Home Fast Share (বাসার হটস্পট)",
                        ssid = "NetShare_Home",
                        password = "fastshare2026",
                        securityType = "WPA2",
                        note = "Default fast network sharing profile"
                    )
                )
                dao.insertProfile(
                    HotspotProfile(
                        title = "Guest Access (অতিথি নেটওয়ার্ক)",
                        ssid = "Guest_Hotspot",
                        password = "welcomeguest88",
                        securityType = "WPA2",
                        note = "Temporary guest hotspot profile"
                    )
                )
            }
        }
    }
}
