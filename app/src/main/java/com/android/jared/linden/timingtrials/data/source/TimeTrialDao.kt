package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.*

@Dao
abstract class TimeTrialDao {
    @Insert
    abstract fun insert(timeTrialHeader: TimeTrialHeader): Long

    @Update
    abstract fun update(timeTrialHeader: TimeTrialHeader)

    @Delete
    abstract fun delete(timeTrialHeader: TimeTrialHeader)

    @Transaction
    open suspend fun insert(timeTrial: TimeTrial): Long{
        val uncom = getAllUncompleteTimeTrial()
        if(timeTrial.timeTrialHeader.status != TimeTrialStatus.FINISHED){
            uncom.forEach { delete(it) }
        }

        val id = insert(timeTrial.timeTrialHeader)
        //setupTimeTrial.eventList.map { it.copy(timeTrialId = id)}
        _insertAllEvents( timeTrial.eventList.map { it.copy(timeTrialId = id)})
        _insertAllTimeTrialRiders(timeTrial.riderList.map { it.copy(timeTrialId = id)})
        return id
    }


    @Transaction
    open suspend fun update(timeTrial: TimeTrial){
        timeTrial.timeTrialHeader.id?.let { ttId->

            val uncom = getAllUncompleteTimeTrial()
            if(timeTrial.timeTrialHeader.status != TimeTrialStatus.FINISHED){
                uncom.filter { it.id != ttId }.forEach { delete(it) }
            }

            _deleteTtEvents(ttId)
            _deleteTtRiders(ttId)

            timeTrial.eventList.map { it.copy(timeTrialId = ttId)}
            _insertAllEvents(timeTrial.eventList)

            timeTrial.riderList.map { it.copy(timeTrialId = ttId)}
            _insertAllTimeTrialRiders(timeTrial.riderList)
            update(timeTrial.timeTrialHeader)
        }

    }

    @Delete
    fun delete(timeTrial: TimeTrial){
        delete(timeTrial.timeTrialHeader)
    }

    @Query("DELETE FROM timetrial_table") abstract fun deleteAll()
    @Query("DELETE FROM timetrial_rider_table") abstract fun deleteAllR()
    @Query("DELETE FROM timetrial_event_table") abstract fun deleteAllE()

    @Query("SELECT * from timetrial_table ORDER BY startTime ASC") abstract fun getAllTimeTrials(): LiveData<List<TimeTrialHeader>>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE Id = :ttId LIMIT 1") abstract fun getTimeTrialById(ttId: Long): LiveData<TimeTrial>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE ttName = :timeTrialName LIMIT 1") abstract fun getTimeTrialByName(timeTrialName: String): TimeTrial?

    @Transaction @Query("SELECT * FROM timetrial_table WHERE ttName = :timeTrialName LIMIT 1") abstract fun getLiveTimeTrialByName(timeTrialName: String): LiveData<TimeTrial>

    //SQLite does not have a boolean data type. Room maps it to an INTEGER column, mapping true to 1 and false to 0.
    @Transaction @Query("SELECT * FROM timetrial_table WHERE status != 2") abstract fun getNonFinishedTimeTrial(): LiveData<List<TimeTrial>>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE status = 0 LIMIT 1") abstract suspend fun getSetupTimeTrialSuspend(): TimeTrial?

    @Transaction @Query("SELECT * FROM timetrial_table WHERE status != 2") abstract suspend fun getAllUncompleteTimeTrial(): List<TimeTrialHeader>


    //@Transaction @Query("SELECT * FROM timetrial_table WHERE status = 1 LIMIT 1") abstract fun getTimingTimeTrial(): LiveData<TimeTrial>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE Id = :timeTrialId LIMIT 1") abstract fun getFullTimeTrial(timeTrialId: Long): LiveData<TimeTrial>


    @Query("DELETE  FROM timetrial_event_table WHERE timeTrialId = :ttId") abstract fun _deleteTtEvents(ttId: Long)

    @Query("DELETE  FROM timetrial_rider_table WHERE timeTrialId = :ttId") abstract fun _deleteTtRiders(ttId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract fun _insertAllEvents(events: List<RiderPassedEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract fun _insertAllTimeTrialRiders(riders: List<TimeTrialRider>)

}