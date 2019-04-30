package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.TimeTrialDefinition
import com.android.jared.linden.timingtrials.data.TimeTrialEvent
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrial

@Dao
abstract class TimeTrialDao {
    @Insert
    abstract fun insert(timeTrialDefinition: TimeTrialDefinition): Long

    @Update
    abstract fun update(timeTrialDefinition: TimeTrialDefinition)

    @Delete
    abstract fun delete(timeTrialDefinition: TimeTrialDefinition)

    @Transaction @Insert
    fun insert(timeTrial: TimeTrial){
        val id = insert(timeTrial.timeTrialDefinition)
        timeTrial.eventList.forEach { it.timeTrialId = id }
        _insertAllEvents(timeTrial.eventList)
        timeTrial.riderList.forEach { it.timeTrialId = id }
        _insertAllTimeTrialRiders(timeTrial.riderList)
    }

    @Transaction @Update
    fun update(timeTrial: TimeTrial){
        timeTrial.timeTrialDefinition.id?.let { ttId->
            update(timeTrial.timeTrialDefinition)
            _deleteTtEvents(ttId)
            _deleteTtRiders(ttId)

            timeTrial.eventList.forEach { it.timeTrialId = ttId }
            _insertAllTimeTrialRiders(timeTrial.riderList)

            timeTrial.riderList.forEach { it.timeTrialId = ttId }
            _insertAllEvents(timeTrial.eventList)
        }

    }

    @Delete
    fun delete(timeTrial: TimeTrial){
        delete(timeTrial.timeTrialDefinition)
    }

    @Query("DELETE FROM timetrial_table") abstract fun deleteAll()

    @Query("SELECT * from timetrial_table ORDER BY startTime ASC") abstract fun getAllTimeTrials(): LiveData<List<TimeTrialDefinition>>

    @Query("SELECT * FROM timetrial_table WHERE Id = :ttId LIMIT 1") abstract fun getTimeTrialById(ttId: Long): LiveData<TimeTrial>

    @Query("SELECT * FROM timetrial_table WHERE ttName = :timeTrialName LIMIT 1") abstract fun getTimeTrialByName(timeTrialName: String): TimeTrial?

    //SQLite does not have a boolean data type. Room maps it to an INTEGER column, mapping true to 1 and false to 0.
    @Query("SELECT * FROM timetrial_table WHERE isSetup = 0 LIMIT 1") abstract fun getSetupTimeTrial(): LiveData<TimeTrial>


    @Query("SELECT * FROM timetrial_table WHERE Id = :timeTrialId LIMIT 1") abstract fun getFullTimeTrial(timeTrialId: Long): LiveData<TimeTrial>


    @Query("DELETE  FROM timetrial_event_table WHERE timeTrialId = :ttId") abstract fun _deleteTtEvents(ttId: Long)

    @Query("DELETE  FROM timetrial_rider_table WHERE timeTrialId = :ttId") abstract fun _deleteTtRiders(ttId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract fun _insertAllEvents(events: List<TimeTrialEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract fun _insertAllTimeTrialRiders(riders: List<TimeTrialRider>)

}