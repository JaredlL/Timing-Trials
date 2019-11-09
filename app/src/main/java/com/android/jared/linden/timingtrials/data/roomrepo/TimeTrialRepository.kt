package com.android.jared.linden.timingtrials.data.roomrepo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.source.TimeTrialDao
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface ITimeTrialRepository{

    suspend fun insert(timeTrial: TimeTrial):Long
    suspend fun insertOrUpdate(timeTrial: TimeTrial): Long
    suspend fun update(timeTrial: TimeTrial)
    suspend fun getTimeTrialByName(name: String): TimeTrial?
    suspend fun delete(timeTrial: TimeTrial)
    suspend fun getSetupTimeTrialSuspend(): TimeTrial
    fun getNonFinishedTimeTrial(): LiveData<TimeTrial>
    fun getLiveTimeTrialByName(name:String): LiveData<TimeTrial>
    fun getTimeTrialById(id: Long): LiveData<TimeTrial>
    val allTimeTrialsHeader: LiveData<List<TimeTrialHeader>>
}

@Singleton
class RoomTimeTrialRepository @Inject constructor(private val timeTrialDao: TimeTrialDao): ITimeTrialRepository {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun getTimeTrialByName(name: String): TimeTrial? {
        return timeTrialDao.getTimeTrialByName(name)
    }


    override val allTimeTrialsHeader: LiveData<List<TimeTrialHeader>> = timeTrialDao.getAllTimeTrials()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(timeTrial: TimeTrial):Long {
        System.out.println("JAREDMSG -> Inserting New TT ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
       return timeTrialDao.insert(timeTrial)
    }

    override fun getNonFinishedTimeTrial(): LiveData<TimeTrial> {
        return timeTrialDao.getNonFinishedTimeTrial()
    }

    override suspend fun getSetupTimeTrialSuspend(): TimeTrial {

        val tt = timeTrialDao.getSetupTimeTrialSuspend()
        return if(tt == null){
           val newtt = TimeTrial.createBlank()
           val id = insert(newtt)
           return timeTrialDao.getSetupTimeTrialSuspend()?: newtt.copy(timeTrialHeader = newtt.timeTrialHeader.copy(id = id))
        }else{
            tt
        }

    }


    override fun getLiveTimeTrialByName(name:String): LiveData<TimeTrial> {

        return timeTrialDao.getLiveTimeTrialByName(name)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(timeTrial: TimeTrial) {
        System.out.println("JAREDMSG -> Updating ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
        timeTrialDao.update(timeTrial)
    }


   override fun getTimeTrialById(id: Long) : LiveData<TimeTrial> {
       return timeTrialDao.getTimeTrialById(id)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun delete(timeTrial: TimeTrial) {
        timeTrialDao.delete(timeTrial)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insertOrUpdate(timeTrial: TimeTrial): Long{
        System.out.println("JAREDMSG -> Inserting ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
        val id = timeTrial.timeTrialHeader.id
        return if(id != null){
            timeTrialDao.update(timeTrial)
             id
        }else{
            timeTrialDao.insert(timeTrial)
        }

    }

}
