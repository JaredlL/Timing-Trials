package com.android.jared.linden.timingtrials.data

import androidx.lifecycle.LiveData

interface ITimeTrialEventRepository{

    suspend fun updateTimeTrialEvents(timeTrialWithEvents: TimeTrialWithEvents)
    fun getTimeTrialWithEvents(timeTrialId: Long): LiveData<TimeTrialWithEvents>

}