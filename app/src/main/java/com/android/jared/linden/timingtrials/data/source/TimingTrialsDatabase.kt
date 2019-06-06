package com.android.jared.linden.timingtrials.data.source

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.jared.linden.timingtrials.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime

@Database(entities = [Rider::class, Course::class, TimeTrialHeader::class, RiderPassedEvent::class, TimeTrialRider::class], version = 24, exportSchema = false)
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
            riderDao.insert(createBaseRider("Jared", "Linden", "RDFCC", 1990, Gender.MALE))
            riderDao.insert(createBaseRider("Adam", "Taylor", "RDFCC", 1976, Gender.MALE))
            riderDao.insert(createBaseRider("John", "Linden", "RDFCC", 1955, Gender.MALE))
            riderDao.insert(createBaseRider("Lauren", "Johnston", "Avid", 1993, Gender.FEMALE))
            riderDao.insert(createBaseRider("Steve", "Beal", "VeloVitesse", 1976, Gender.MALE))
            riderDao.insert(createBaseRider("Earl", "Smith", "RDFCC", 1976, Gender.MALE))
            riderDao.insert(createBaseRider("Jo", "Jago", "Performance Cycles", 1979, Gender.FEMALE))
            riderDao.insert(createBaseRider("Dave", "Pearce", "RDFCC", 1977, Gender.MALE))
            riderDao.insert(createBaseRider("Craig ", "Buffry", "RDFCC", 1992, Gender.MALE))
            riderDao.insert(createBaseRider("Collin", "Parry", "RDFCC", 1975, Gender.MALE))
            riderDao.insert(createBaseRider("Rob", "Borek", "Forever Pedalling", 1992, Gender.MALE))
            riderDao.insert(createBaseRider("Michelle ", "Lee", "RDFCC", 1980, Gender.FEMALE))
            riderDao.insert(createBaseRider("Pfeiffer", "Georgi", "Sunweb", 1996, Gender.FEMALE))
            riderDao.insert(createBaseRider("Bob", "Parry", "RDFCC", 1949, Gender.MALE))
            riderDao.insert(createBaseRider("Megan", "Dickerson", "Bristol South CC", 1993, Gender.FEMALE))
            riderDao.insert(createBaseRider("Lucy", "Gadd", "On Form", 1997, Gender.FEMALE))
            riderDao.insert(createBaseRider("Louise", "Hart", "Ross", 1987, Gender.FEMALE))
            riderDao.insert(createBaseRider("Dave", "Bucknall", "RDFCC", 1970, Gender.MALE))
            riderDao.insert(createBaseRider("Rob", "Hussey", "RDFCC", 1975, Gender.MALE))
            riderDao.insert(createBaseRider("Paul", "Jones", "RDFCC", 1975, Gender.MALE))
            riderDao.insert(createBaseRider("Bradley", "Wiggins", "RDFCC", 1980, Gender.MALE))
            riderDao.insert(createBaseRider("Lance", "Armstrong", "Postal", 1975, Gender.MALE))
            riderDao.insert(createBaseRider("Tom", "Knight", "Ross", 1960, Gender.MALE))
            riderDao.insert(createBaseRider("Paul", "Stephens", "Ross", 1965, Gender.MALE))
            riderDao.insert(createBaseRider("Richard", "Harrington", "Ross", 1965, Gender.MALE))
            riderDao.insert(createBaseRider("Phil", "Sims", "Newport", 1968, Gender.MALE))
            riderDao.insert(createBaseRider("Jon", "Morris", "Chepstow CC", 1976, Gender.MALE))
            riderDao.insert(createBaseRider("Gordon", "Marcus", "Severn RC", 1970, Gender.MALE))
            riderDao.insert(createBaseRider("Joe", "Griffiths", "78 Degrees", 1992, Gender.MALE))
            riderDao.insert(createBaseRider("Matt", "Fratesi", "TORQ", 1996, Gender.MALE))
            riderDao.insert(createBaseRider("Marcin", "Biablocki", "Nopinz", 1983, Gender.MALE))
            riderDao.insert(createBaseRider("Geraint", "Thomas", "Sky", 1988, Gender.MALE))
            riderDao.insert(createBaseRider("Peter", "Sagan", "Bora", 1970, Gender.MALE))
            riderDao.insert(createBaseRider("Tom", "Sharp", "BRC", 1992, Gender.MALE))
            riderDao.insert(createBaseRider("Felix", "Young", "Avid Sport", 1996, Gender.MALE))
            riderDao.insert(createBaseRider("John", "Russell", "BRC", 1983, Gender.MALE))
            riderDao.insert(createBaseRider("Andy", "Edwards", "BRC", 1988, Gender.MALE))



        }

        fun createBaseRider(fname:String, lname:String, club:String, dob:Int, gender: Gender):Rider{
            val bd: OffsetDateTime = OffsetDateTime.now().minusYears((2019 - dob).toLong())
            return Rider(fname, lname, club, bd, gender)
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