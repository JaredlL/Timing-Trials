package com.android.jared.linden.timingtrials.data.roomrepo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.data.source.TimeTrialRiderDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeTrialRiderRepository @Inject constructor(private val timeTrialRiderDao: TimeTrialRiderDao){

    fun update(timeTrialRider: TimeTrialRider){
        timeTrialRiderDao.update(timeTrialRider)
    }


    fun insert (timeTrialRider: TimeTrialRider){
        timeTrialRiderDao.update(timeTrialRider)
    }

    fun delete(timeTrialRider: TimeTrialRider) {
        timeTrialRiderDao.delete(timeTrialRider)
    }

    fun getRidersForTimeTrial(ttHeader: TimeTrialHeader): LiveData<List<FilledTimeTrialRider>>{
        val id = ttHeader.id
        if(id != null){
           return timeTrialRiderDao.getTimeTrialRiders(ttHeader.id)
        }else{
            return MutableLiveData<List<FilledTimeTrialRider>>()
        }

    }

}