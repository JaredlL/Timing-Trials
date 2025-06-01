package com.jaredlinden.timingtrials.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.TimeTrialRider
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [Rider::class, Course::class, TimeTrialHeader::class, TimeTrialRider::class],
    version = 46 ,
    exportSchema = true)
@TypeConverters(Converters::class)
abstract class TimingTrialsDatabase : RoomDatabase() {

    abstract fun riderDao() : RiderDao
    abstract fun courseDao(): CourseDao
    abstract fun timeTrialDao(): TimeTrialDao
    abstract fun timeTrialRiderDao(): TimeTrialRiderDao

    companion object {
        @Volatile
        private var INSTANCE: TimingTrialsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TimingTrialsDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    TimingTrialsDatabase::class.java, "timingtrials_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}