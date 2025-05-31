package com.jaredlinden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialBasicInfo
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.TimeTrialRider
import timber.log.Timber

@Dao
abstract class TimeTrialDao(db: RoomDatabase) {

    init{
        Timber.d("DB init")
    }

    @Insert
    abstract fun insert(timeTrialHeader: TimeTrialHeader): Long

    @Insert
    abstract fun insertMultipleRiders(timeTrialRiderList: List<TimeTrialRider>)

    @Insert
    abstract fun insertRider(timeTrialRider: TimeTrialRider)

    @Delete
    abstract fun deleteRider(timeTrialRider: TimeTrialRider)

    @Update
    abstract fun updateTimeTrialRider(timeTrialRider: TimeTrialRider)

    @Update
    abstract fun update(timeTrialHeader: TimeTrialHeader)

    @Delete
    abstract fun delete(timeTrialHeader: TimeTrialHeader)

    @Transaction
    open suspend fun insertFull(timeTrial: TimeTrial): Long
    {
        val id = insert(timeTrial.timeTrialHeader)
        Timber.d("Insert New TT $id + ${timeTrial.timeTrialHeader.ttName}, ${timeTrial.riderList.count()} riders into DB")

        val newRiderList = timeTrial.riderList.map { it.timeTrialData.copy(timeTrialId = id) }

        _insertAllTimeTrialRiders(newRiderList)

        return id
    }

    @Delete
    fun delete(timeTrial: TimeTrial){
        timeTrial.timeTrialHeader.id?.let {ttId ->
            _deleteTtRiders(ttId)
            delete(timeTrial.timeTrialHeader)
        }
    }

    @Transaction
    open suspend fun update(timeTrial: TimeTrial){
        timeTrial.timeTrialHeader.id?.let { ttId->

            update(timeTrial.timeTrialHeader)
            val idSet = timeTrial.riderList.asSequence().map { it.timeTrialData }.groupBy { it.id }

            getTimeTrialRiders(ttId).forEach {ttr->
                ttr.id?.let {
                    val new = idSet[it]?.firstOrNull()
                    if(new == null){
                        deleteRider(ttr)
                    }else if(new != ttr){
                        updateTimeTrialRider(new.copy(courseId = timeTrial.course?.id))
                    }
                }
            }
            idSet[null]?.let {
                if(it.isNotEmpty()){
                    insertMultipleRiders(it.map {rd-> rd.copy(courseId = timeTrial.course?.id) })
                }
            }
        }
    }

    @Query("DELETE FROM timetrial_table WHERE id = :ttId")
    abstract fun deleteTimeTrialById(ttId: Long)

    @Query("DELETE FROM timetrial_table")
    abstract fun deleteAll()

    @Query("DELETE FROM timetrial_rider_table")
    abstract fun deleteAllR()

    @Query("SELECT courseId, laps, interval, id FROM timetrial_table")
    abstract fun getAllHeaderBasicInfo(): List<TimeTrialBasicInfo>

    @Query("SELECT * from timetrial_table ORDER BY status ASC, startTime ASC")
    abstract fun getAllTimeTrials(): LiveData<List<TimeTrialHeader>>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE id = :ttId AND  status == 0 LIMIT 1")
    abstract fun getSetupTimeTrialById(ttId: Long): LiveData<TimeTrial?>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE id = :ttId AND  status == 2 LIMIT 1")
    abstract fun getResultTimeTrialById(ttId: Long): LiveData<TimeTrial?>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE status == 1")
    abstract fun getTimingTimeTrials(): LiveData<List<TimeTrial>>

    @Transaction @Query("SELECT * FROM timetrial_table")
    abstract fun getAllCompleteTimeTrials(): List<TimeTrial>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE ttName = :timeTrialName LIMIT 1")
    abstract fun getTimeTrialByName(timeTrialName: String): TimeTrial?

    @Transaction @Query("SELECT * FROM timetrial_table WHERE ttName = :timeTrialName")
    abstract fun getHeadersByName(timeTrialName: String): List<TimeTrialHeader>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE courseId = :cId")
    abstract fun getTimeTrialsOnCourse(cId: Long): List<TimeTrialHeader>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE ttName = :timeTrialName LIMIT 1")
    abstract fun getLiveTimeTrialByName(timeTrialName: String): LiveData<TimeTrial>

    //SQLite does not have a boolean data type. Room maps it to an INTEGER column, mapping true to 1 and false to 0.
    @Transaction @Query("SELECT * FROM timetrial_table WHERE id = :timeTrialId LIMIT 1")
    abstract fun getFullTimeTrial(timeTrialId: Long): LiveData<TimeTrial>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE id = :timeTrialId LIMIT 1")
    abstract fun getFullTimeTrialSuspend(timeTrialId: Long): TimeTrial

    @Query("DELETE  FROM timetrial_rider_table WHERE timeTrialId = :ttId")
    abstract fun _deleteTtRiders(ttId: Long)

    @Query("SELECT * from timetrial_rider_table")
    abstract fun _allTtRiders(): List<TimeTrialRider>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun _insertAllTimeTrialRiders(riders: List<TimeTrialRider>)

    @Query("SELECT * FROM timetrial_rider_table WHERE timeTrialId = :timeTrialId ORDER BY `index`")
    abstract fun getTimeTrialRiders(timeTrialId: Long): List<TimeTrialRider>
}