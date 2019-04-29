package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.TimeTrialEvent
import com.android.jared.linden.timingtrials.data.TimeTrialWithEvents

@Dao
interface TimeTrialEventDao {

    @Transaction fun updateTimeTrialEvents(ttWithEvents: TimeTrialWithEvents){
        ttWithEvents.timeTrial.id?.let {
            deleteTimeTrialsEvents(it)
            insertEvents(ttWithEvents.eventList)
        }


    }

    @Transaction
    @Query ("SELECT * from timetrial_table") fun getAllTimeTrialsEvents(): LiveData<List<TimeTrialWithEvents>>

    @Transaction
    @Query("SELECT * FROM timetrial_table WHERE Id = :timeTrialId LIMIT 1") fun getTimeTrialEvents(timeTrialId: Long): LiveData<TimeTrialWithEvents>

    @Query("DELETE  FROM timetrialevent_table WHERE timeTrialId = :ttId") fun deleteTimeTrialsEvents(ttId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertEvents(events: List<TimeTrialEvent>)
}