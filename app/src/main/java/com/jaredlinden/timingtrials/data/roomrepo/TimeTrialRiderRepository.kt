package com.jaredlinden.timingtrials.data.roomrepo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.source.TimeTrialRiderDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeTrialRiderRepository @Inject constructor(private val timeTrialRiderDao: TimeTrialRiderDao){

    suspend fun update(timeTrialRider: TimeTrialRider){
        timeTrialRiderDao.update(timeTrialRider)
    }

    fun getAllResults(): LiveData<List<TimeTrialRiderResult>>{
        return timeTrialRiderDao.getAllResults()
    }

    fun insert (timeTrialRider: TimeTrialRider): Long{
        return timeTrialRiderDao.insert(timeTrialRider)
    }

    fun delete(timeTrialRider: TimeTrialRider) {
        timeTrialRiderDao.delete(timeTrialRider)
    }

    fun getResultById(resultId: Long): LiveData<TimeTrialRiderResult?>{
        return timeTrialRiderDao.getResultById(resultId)
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

    fun getRidersForTimeTrial(ttId: Long): LiveData<List<FilledTimeTrialRider>>{
        return timeTrialRiderDao.getTimeTrialRiders(ttId)
    }

    suspend fun getRidersForTimeTrialSuspend(ttId: Long): List<FilledTimeTrialRider>{
        return timeTrialRiderDao.getTimeTrialRidersSuspend(ttId)
    }

    suspend fun getAllResultsSuspend(): List<TimeTrialRiderResult>{
        return timeTrialRiderDao.getAllResultsSuspend()
    }

}