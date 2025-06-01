package com.jaredlinden.timingtrials.data.roomrepo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialBasicInfo
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.source.TimeTrialDao
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface ITimeTrialRepository{

    suspend fun insert(timeTrial: TimeTrial):Long
    suspend fun insertNewHeader(timeTrialHeader: TimeTrialHeader):Long
    suspend fun update(timeTrialHeader: TimeTrialHeader)
    suspend fun updateFull(timeTrial: TimeTrial)
    suspend fun getTimeTrialByName(name: String): TimeTrial?
    suspend fun getResultTimeTrialByIdSuspend(id: Long): TimeTrial
    suspend fun getHeadersByName(name: String): List<TimeTrialHeader>
    suspend fun delete(timeTrial: TimeTrial)
    suspend fun allTimeTrials(): List<TimeTrial>
    suspend fun allTimeTrialsOnCourse(courseId: Long): List<TimeTrialHeader>
    suspend fun deleteById(ttId: Long)
    suspend fun getAllHeaderBasicInfo(): List<TimeTrialBasicInfo>
    suspend fun deleteHeader(timeTrialHeader: TimeTrialHeader)
    fun getSetupTimeTrialById(timeTrialId: Long): LiveData<TimeTrial?>
    fun getTimingTimeTrial():LiveData<TimeTrial?>
    fun getLiveTimeTrialByName(name:String): LiveData<TimeTrial>
    fun getResultTimeTrialById(id: Long): LiveData<TimeTrial?>
    val allTimeTrialsHeader: LiveData<List<TimeTrialHeader>>
}

@Singleton
class RoomTimeTrialRepository @Inject constructor(private val timeTrialDao: TimeTrialDao): ITimeTrialRepository {

    @WorkerThread
    override suspend fun getTimeTrialByName(name: String): TimeTrial? {
        return timeTrialDao.getTimeTrialByName(name)
    }

    override suspend fun getResultTimeTrialByIdSuspend(id: Long): TimeTrial {
        return timeTrialDao.getFullTimeTrialSuspend(id)
    }

    @WorkerThread
    override suspend fun getHeadersByName(name: String): List<TimeTrialHeader> {
        return timeTrialDao.getHeadersByName(name)
    }

    @WorkerThread
    override suspend fun updateFull(timeTrial: TimeTrial) {
        Timber.d("Updating ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
        if((timeTrial.timeTrialHeader.id ?: 0L) == 0L){
            throw Exception("TT ID cannot be null")
        }
        timeTrialDao.update(timeTrial)
    }

    override suspend fun getAllHeaderBasicInfo(): List<TimeTrialBasicInfo> {
        return timeTrialDao.getAllHeaderBasicInfo()
    }

    override val allTimeTrialsHeader: LiveData<List<TimeTrialHeader>> = timeTrialDao.getAllTimeTrials()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @WorkerThread
    override suspend fun insert(timeTrial: TimeTrial):Long {
        Timber.d("Inserting New TT ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
//        if(timeTrial.timeTrialHeader.status != TimeTrialStatus.FINISHED){
//            throw Exception("Cannot insertFull non finished TT")
//        }
       return timeTrialDao.insertFull(timeTrial)
    }

    @WorkerThread
    override suspend fun insertNewHeader(timeTrialHeader: TimeTrialHeader):Long {
        Timber.d("Inserting New TT Header into DB from background thread")
        return timeTrialDao.insert(timeTrialHeader)
    }


    override suspend fun allTimeTrials(): List<TimeTrial> {
        return timeTrialDao.getAllCompleteTimeTrials()
    }

    override suspend fun allTimeTrialsOnCourse(courseId: Long): List<TimeTrialHeader> {
        return timeTrialDao.getTimeTrialsOnCourse(courseId)
    }


    override fun getLiveTimeTrialByName(name:String): LiveData<TimeTrial> {

        return timeTrialDao.getLiveTimeTrialByName(name)
    }


    @WorkerThread
    override suspend fun update(timeTrialHeader: TimeTrialHeader) {
        Timber.d("Updating ${timeTrialHeader.id} ${timeTrialHeader.ttName} into DB from background thread")
        if((timeTrialHeader.id ?: 0L) == 0L){
            throw Exception("TT ID cannot be null")
        }
        timeTrialDao.update(timeTrialHeader)
    }


   override fun getResultTimeTrialById(id: Long) : LiveData<TimeTrial?> {
       return timeTrialDao.getResultTimeTrialById(id)
    }

    override fun getSetupTimeTrialById(timeTrialId: Long): LiveData<TimeTrial?>{
        return timeTrialDao.getSetupTimeTrialById(timeTrialId)
    }

    override fun getTimingTimeTrial(): LiveData<TimeTrial?> {
        return timeTrialDao.getTimingTimeTrials().map{
            it.firstOrNull()
        }
    }

    @WorkerThread
    override suspend fun delete(timeTrial: TimeTrial) {
        timeTrialDao.delete(timeTrial)
    }

    @WorkerThread
    override suspend fun deleteById(id: Long) {
        timeTrialDao.deleteTimeTrialById(id)
    }

    @WorkerThread
    override suspend fun deleteHeader(timeTrialHeader: TimeTrialHeader) {
        timeTrialDao.delete(timeTrialHeader)
    }
}
