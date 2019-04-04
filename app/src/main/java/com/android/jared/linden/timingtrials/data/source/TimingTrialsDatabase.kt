package com.android.jared.linden.timingtrials.data.source

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import javax.inject.Inject

@Database(entities = [Rider::class, Course::class, TimeTrial::class], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TimingTrialsDatabase : RoomDatabase() {

    abstract fun riderDao() : RiderDao
    abstract fun courseDao(): CourseDao
    abstract fun timeTrialDao(): TimeTrialDao

    companion object {
        @Volatile private var INSTANCE: TimingTrialsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TimingTrialsDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(context,
                        TimingTrialsDatabase::class.java,
                        "timingtrials_database")
                        .fallbackToDestructiveMigration()
                        .addCallback(TimingTrialsDatabaseCallback(scope))
                        .build()
                INSTANCE = instance
                instance
            }
        }

        class TimingTrialsDatabaseCallback(val scope: CoroutineScope) : RoomDatabase.Callback() {
            /**
             * Override the onOpen method to populate the database.
             * For this sample, we clear the database every time it is created or opened.
             */

            /**
             * DI provides the database
             */

            //@Inject lateinit var ttdb: TimingTrialsDatabase

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // If you want to keep the data through app restarts,
                // comment out the following line.
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateRiders(database.riderDao())
                        populateCourses(database.courseDao())
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
            riderDao.insert(Rider("Lauren", "Johnston", "Avid", 25, gender = "Female"))
            riderDao.insert(Rider("Steve", "Beal", "VeloVitesse", 42))
            riderDao.insert(Rider("Earl", "Smith", "RDFCC", 42))
            riderDao.insert(Rider("Jo", "Jago", "Performance Cycles", 39, gender = "Female"))
            riderDao.insert(Rider("Dave", "Pearce", "RDFCC", 42))
            riderDao.insert(Rider("Craig ", "Buffry", "RDFCC", 42))
            riderDao.insert(Rider("Collin", "Parry", "RDFCC", 36))
            riderDao.insert(Rider("Rob", "Borek", "Forever Pedalling", 23))
            riderDao.insert(Rider("Michelle ", "Lee", "RDFCC", 23, gender = "Female"))
            riderDao.insert(Rider("Pfeiffer", "Georgi", "Sunweb", 18, gender = "Female"))
            riderDao.insert(Rider("Bob", "Parry", "RDFCC", 70))
            riderDao.insert(Rider("Megan", "Dickerson", "Bristol South CC", 24, gender = "Female"))
            riderDao.insert(Rider("Lucy", "Gadd", "On Form", 18, gender = "Female"))
            riderDao.insert(Rider("Louise", "Hart", "Ross", 29, gender = "Female"))
            riderDao.insert(Rider("Dave", "Bucknall", "RDFCC", 40))
            riderDao.insert(Rider("Rob", "Hussey", "RDFCC", 40))
            riderDao.insert(Rider("Paul", "Jones", "RDFCC", 40))
            riderDao.insert(Rider("Bradley", "Wiggins", "RDFCC", 40))
            riderDao.insert(Rider("Lance", "Armstrong", "Postal", 45))
            riderDao.insert(Rider("Tom", "Knight", "Ross", 50))
            riderDao.insert(Rider("Paul", "Stephens", "Ross", 50))
            riderDao.insert(Rider("Richard", "Harrington", "Ross", 50))
            riderDao.insert(Rider("Phil", "Sims", "Newport", 50))
            riderDao.insert(Rider("Jon", "Morris", "Chepstow CC", 50))
            riderDao.insert(Rider("Gordon", "Marcus", "Severn RC", 40))
            riderDao.insert(Rider("Joe", "Griffiths", "78 Degrees", 23))
            riderDao.insert(Rider("Nino", "Schurter", "Scott", 23))
        }

        fun populateCourses(courseDao: CourseDao){
            courseDao.deleteAll()
            courseDao.insert(Course("Lydbrook 10", 16093.4, "UC603"))
            courseDao.insert(Course("Hilly Lydbrook", 24140.2, "UC612"))
            courseDao.insert(Course("Cannop", 19312.1, "UC611 "))
            courseDao.insert(Course("Tomarton", 37014.9, "U601B"))
            courseDao.insert(Course("Tintern 10", 16093.4, "UC620"))
            courseDao.insert(Course("Speech House 10", 16093.4, "UC606"))
        }

    }

}