package com.jaredlinden.timingtrials.data.source

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jaredlinden.timingtrials.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Rider::class, Course::class, TimeTrialHeader::class, TimeTrialRider::class], version = 46 , exportSchema = false)
@TypeConverters(Converters::class)
abstract class TimingTrialsDatabase : RoomDatabase() {

    abstract fun riderDao() : RiderDao
    abstract fun courseDao(): CourseDao
    abstract fun timeTrialDao(): TimeTrialDao
    abstract fun timeTrialRiderDao(): TimeTrialRiderDao

    val mDbIsPopulated = MutableLiveData(false)

    companion object {
        @Volatile private var INSTANCE: TimingTrialsDatabase? = null

        val MIGRATION_46_47 = object : Migration(46,47) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): TimingTrialsDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(context,
                        TimingTrialsDatabase::class.java, "timingtrials_database")
                        .fallbackToDestructiveMigration()
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

                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                            database.mDbIsPopulated.postValue(true)
                    }
                }
            }
        }
    }
}