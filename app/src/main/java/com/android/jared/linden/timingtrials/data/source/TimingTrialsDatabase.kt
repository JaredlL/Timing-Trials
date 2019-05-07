package com.android.jared.linden.timingtrials.data.source

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.jared.linden.timingtrials.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Rider::class, Course::class, TimeTrialHeader::class, TimeTrialEvent::class, TimeTrialRider::class], version = 14, exportSchema = false)
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
                        populateTt(database.timeTrialDao(), database.riderDao(), database.courseDao())
                    }
                }
            }
        }

        fun populateRiders(riderDao: RiderDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            riderDao.deleteAll()
            riderDao.insert(Rider("Jared", "Linden", "RDFCC", 1990, GENDER_M))
            riderDao.insert(Rider("Adam", "Taylor", "RDFCC", 1976, GENDER_M))
            riderDao.insert(Rider("John", "Linden", "RDFCC", 1955, GENDER_M))
            riderDao.insert(Rider("Lauren", "Johnston", "Avid", 1993, GENDER_F))
            riderDao.insert(Rider("Steve", "Beal", "VeloVitesse", 1976, GENDER_M))
            riderDao.insert(Rider("Earl", "Smith", "RDFCC", 1976, GENDER_M))
            riderDao.insert(Rider("Jo", "Jago", "Performance Cycles", 1979, GENDER_F))
            riderDao.insert(Rider("Dave", "Pearce", "RDFCC", 1977, GENDER_M))
            riderDao.insert(Rider("Craig ", "Buffry", "RDFCC", 1992, GENDER_M))
            riderDao.insert(Rider("Collin", "Parry", "RDFCC", 1975, GENDER_M))
            riderDao.insert(Rider("Rob", "Borek", "Forever Pedalling", 1992, GENDER_M))
            riderDao.insert(Rider("Michelle ", "Lee", "RDFCC", 1980, GENDER_F))
            riderDao.insert(Rider("Pfeiffer", "Georgi", "Sunweb", 1996, GENDER_F))
            riderDao.insert(Rider("Bob", "Parry", "RDFCC", 1949, GENDER_M))
            riderDao.insert(Rider("Megan", "Dickerson", "Bristol South CC", 1993, GENDER_F))
            riderDao.insert(Rider("Lucy", "Gadd", "On Form", 1997, GENDER_F))
            riderDao.insert(Rider("Louise", "Hart", "Ross", 1987, GENDER_F))
            riderDao.insert(Rider("Dave", "Bucknall", "RDFCC", 1970, GENDER_M))
            riderDao.insert(Rider("Rob", "Hussey", "RDFCC", 1975, GENDER_M))
            riderDao.insert(Rider("Paul", "Jones", "RDFCC", 1975, GENDER_M))
            riderDao.insert(Rider("Bradley", "Wiggins", "RDFCC", 1980, GENDER_M))
            riderDao.insert(Rider("Lance", "Armstrong", "Postal", 1975, GENDER_M))
            riderDao.insert(Rider("Tom", "Knight", "Ross", 1960, GENDER_M))
            riderDao.insert(Rider("Paul", "Stephens", "Ross", 1965, GENDER_M))
            riderDao.insert(Rider("Richard", "Harrington", "Ross", 1965, GENDER_M))
            riderDao.insert(Rider("Phil", "Sims", "Newport", 1968, GENDER_M))
            riderDao.insert(Rider("Jon", "Morris", "Chepstow CC", 1976, GENDER_M))
            riderDao.insert(Rider("Gordon", "Marcus", "Severn RC", 1970, GENDER_M))
            riderDao.insert(Rider("Joe", "Griffiths", "78 Degrees", 1992, GENDER_M))
            riderDao.insert(Rider("Matt", "Fratesi", "TORQ", 1996, GENDER_M))
            riderDao.insert(Rider("Marcin", "Biablocki", "Nopinz", 1983, GENDER_M))
            riderDao.insert(Rider("Geraint", "Thomas", "Sky", 1988, GENDER_M))



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

        fun populateTt(timeTrialDao: TimeTrialDao, riderDao: RiderDao, courseDao: CourseDao){

            timeTrialDao.deleteAllR()
            timeTrialDao.deleteAllE()
            timeTrialDao.deleteAll()
        }

    }

}