package com.android.jared.linden.timingtrials.data

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.jared.linden.timingtrials.data.source.TimeTrialDao
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface ITimeTrialRepository{

    suspend fun insert(timeTrial: TimeTrial)
    suspend fun update(rider: TimeTrial)
    fun getSetupTimeTrial(): LiveData<TimeTrial>

}

@Singleton
class RoomTimeTrialRepository @Inject constructor(private  val timeTrialDao: TimeTrialDao): ITimeTrialRepository {


    val allTimeTrials: LiveData<List<TimeTrial>> = timeTrialDao.gatAllTimeTrials()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(timeTrial: TimeTrial) {
        timeTrialDao.insert(timeTrial)
    }

    override fun getSetupTimeTrial(): MutableLiveData<TimeTrial> {
        val c = Calendar.getInstance()
        c.add(Calendar.MINUTE, 10)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return MutableLiveData(TimeTrial("", startTime =  c.time))
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(rider: TimeTrial) {
        timeTrialDao.update(rider)
    }


    fun getTimeTrial(timeTrialId: Long) : LiveData<TimeTrial> {
       return timeTrialDao.getTimeTrialById(timeTrialId)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(timeTrial: TimeTrial) {
        timeTrialDao.delete(timeTrial)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertOrUpdate(timeTrial: TimeTrial){
        val id = timeTrial.id ?: 0
        if(id != 0L){
            timeTrialDao.update(timeTrial)
        }else{
            timeTrialDao.insert(timeTrial)
        }

    }

}
