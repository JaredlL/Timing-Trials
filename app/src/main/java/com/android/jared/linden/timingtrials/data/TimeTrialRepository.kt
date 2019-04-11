package com.android.jared.linden.timingtrials.data

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.jared.linden.timingtrials.data.source.TimeTrialDao
import java.sql.Time
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface ITimeTrialRepository{

    suspend fun insert(timeTrial: TimeTrial)
    suspend fun insertOrUpdate(timeTrial: TimeTrial)
    suspend fun update(rider: TimeTrial)
    suspend fun getTimeTrialByName(name: String): TimeTrial?
    suspend fun delete(timeTrial: TimeTrial)
    fun getSetupTimeTrial(): LiveData<TimeTrial>
    fun getTimeTrialById(id: Long): LiveData<TimeTrial>
    val allTimeTrials: LiveData<List<TimeTrial>>


}

@Singleton
class RoomTimeTrialRepository @Inject constructor(private  val timeTrialDao: TimeTrialDao): ITimeTrialRepository {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun getTimeTrialByName(name: String): TimeTrial? {
        return timeTrialDao.getTimeTrialByName(name)
    }


    override val allTimeTrials: LiveData<List<TimeTrial>> = timeTrialDao.getAllTimeTrials()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(timeTrial: TimeTrial) {
        timeTrialDao.insert(timeTrial)
    }

    override fun getSetupTimeTrial(): LiveData<TimeTrial> {

        return timeTrialDao.getSetupTimeTrial()
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


   override fun getTimeTrialById(timeTrialId: Long) : LiveData<TimeTrial> {
       return timeTrialDao.getTimeTrialById(timeTrialId)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun delete(timeTrial: TimeTrial) {
        timeTrialDao.delete(timeTrial)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insertOrUpdate(timeTrial: TimeTrial){
        val id = timeTrial.id ?: 0
        if(id != 0L){
            timeTrialDao.update(timeTrial)
        }else{
            timeTrialDao.insert(timeTrial)
        }

    }

}
