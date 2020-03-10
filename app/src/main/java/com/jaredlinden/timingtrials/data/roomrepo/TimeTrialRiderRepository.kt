package com.jaredlinden.timingtrials.data.roomrepo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.source.TimeTrialRiderDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeTrialRiderRepository @Inject constructor(private val timeTrialRiderDao: TimeTrialRiderDao){

    fun update(timeTrialRider: TimeTrialRider){
        timeTrialRiderDao.update(timeTrialRider)
    }


    fun insert (timeTrialRider: TimeTrialRider): Long{
        return timeTrialRiderDao.insert(timeTrialRider)
    }

    fun delete(timeTrialRider: TimeTrialRider) {
        timeTrialRiderDao.delete(timeTrialRider)
    }

    fun getRiderResults(riderId: Long): LiveData<List<TimeTrialRiderResult>>{
        return timeTrialRiderDao.getRiderResults(riderId)
    }

    fun getCourseResults(courseId: Long): LiveData<List<TimeTrialRiderResult>>{
        return timeTrialRiderDao.getCourseResults(courseId)
    }

    fun getCourseResultsSuspend(courseId: Long): List<TimeTrialRider>{
        return timeTrialRiderDao.getCourseResultsSuspend(courseId)
    }

    fun getByRiderTimeTrialIds(riderId: Long, timeTrialId: Long): List<TimeTrialRider>{
        return timeTrialRiderDao.getByRiderTimeTrialIds(riderId, timeTrialId)
    }

    fun lastTimeTrialRiders(): LiveData<List<RiderIdStartTime>> {
        return timeTrialRiderDao.getRiderIdTimeTrialStartTime()
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