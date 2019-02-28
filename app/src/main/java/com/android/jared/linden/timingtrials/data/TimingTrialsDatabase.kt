package com.android.jared.linden.timingtrials.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Rider::class, Course::class], version = 4, exportSchema = false)
abstract class TimingTrialsDatabase : RoomDatabase() {

    abstract fun riderDao() : RiderDao
    abstract fun courseDao(): CourseDao

    companion object {
        @Volatile private var INSTANCE: TimingTrialsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TimingTrialsDatabase{
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(context,
                        TimingTrialsDatabase::class.java,
                        "timingtrials_database")
                        .fallbackToDestructiveMigration()
                        .addCallback(TimingtrialsDatabaseCallback(scope))
                        .build()
                INSTANCE = instance
                instance
            }
        }

        private class TimingtrialsDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            /**
             * Override the onOpen method to populate the database.
             * For this sample, we clear the database every time it is created or opened.
             */
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // If you want to keep the data through app restarts,
                // comment out the following line.
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateRiders(database.riderDao())
                    }
                }
            }
        }

        fun populateRiders(riderDao: RiderDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            riderDao.deleteAll()
            riderDao.insert(Rider("Jared", "Linden", "RDFCC", 28))
            riderDao.insert(Rider("Adam", "Taylor", "RDFCC", 42))
            riderDao.insert(Rider("John", "Linden", "RDFCC", 42))
            riderDao.insert(Rider("Lauren", "Johnston", "Avid", 25))
            riderDao.insert(Rider("Steve", "Beal", "VeloVitesse", 42))
            riderDao.insert(Rider("Jo", "Jago", "PerformanceCycles", 39))
        }

        fun populateCourses(courseDao: CourseDao){
            courseDao.deleteAll()
            courseDao.insert(Course("Lydbrook 10", 10.0))
            courseDao.insert(Course("Hilly Lydbrook"))
            courseDao.insert(Course("Cannop"))
        }

    }

}