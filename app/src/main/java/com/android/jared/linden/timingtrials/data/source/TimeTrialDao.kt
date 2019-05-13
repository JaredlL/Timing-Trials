package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialEvent
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrial

@Dao
abstract class TimeTrialDao {
    @Insert
    abstract fun insert(timeTrialHeader: TimeTrialHeader): Long

    @Update
    abstract fun update(timeTrialHeader: TimeTrialHeader)

    @Delete
    abstract fun delete(timeTrialHeader: TimeTrialHeader)

    @Transaction @Insert
    fun insert(timeTrial: TimeTrial){
        val id = insert(timeTrial.timeTrialHeader)
        //timeTrial.eventList.map { it.copy(timeTrialId = id)}
        _insertAllEvents( timeTrial.eventList.map { it.copy(timeTrialId = id)})
        _insertAllTimeTrialRiders(timeTrial.riderList.map { it.copy(timeTrialId = id)})
    }

    @Transaction @Update
    fun update(timeTrial: TimeTrial){
        timeTrial.timeTrialHeader.id?.let { ttId->
            update(timeTrial.timeTrialHeader)
            _deleteTtEvents(ttId)
            _deleteTtRiders(ttId)

            timeTrial.eventList.map { it.copy(timeTrialId = ttId)}
            _insertAllEvents(timeTrial.eventList)

            timeTrial.riderList.map { it.copy(timeTrialId = ttId)}
            _insertAllTimeTrialRiders(timeTrial.riderList)
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

    //SQLite does not have a boolean data type. Room maps it to an INTEGER column, mapping true to 1 and false to 0.
    @Transaction @Query("SELECT * FROM timetrial_table WHERE isSetup = 0 LIMIT 1") abstract fun getSetupTimeTrial(): LiveData<TimeTrial>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE isSetup = 1 AND isFinished = 0 LIMIT 1") abstract fun getTimingTimeTrial(): LiveData<TimeTrial>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE Id = :timeTrialId LIMIT 1") abstract fun getFullTimeTrial(timeTrialId: Long): LiveData<TimeTrial>


    @Query("DELETE  FROM timetrial_event_table WHERE timeTrialId = :ttId") abstract fun _deleteTtEvents(ttId: Long)

    @Query("DELETE  FROM timetrial_rider_table WHERE timeTrialId = :ttId") abstract fun _deleteTtRiders(ttId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract fun _insertAllEvents(events: List<TimeTrialEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract fun _insertAllTimeTrialRiders(riders: List<TimeTrialRider>)

}