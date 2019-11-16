package com.android.jared.linden.timingtrials.data.roomrepo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.data.source.TimeTrialDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Time
import javax.inject.Inject
import javax.inject.Singleton

interface ITimeTrialRepository{

    suspend fun insert(timeTrial: TimeTrial):Long
    suspend fun update(timeTrial: TimeTrial)
    suspend fun getTimeTrialByName(name: String): TimeTrial?
    suspend fun delete(timeTrial: TimeTrial)
    suspend fun getNonFinishedTimeTrialSuspend(): TimeTrial?
    fun getNonFinishedTimeTrial(): LiveData<TimeTrial?>
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
        if(timeTrial.timeTrialHeader.status != TimeTrialStatus.FINISHED){
            throw Exception("Cannot insertFull non finished TT")
        }
       return timeTrialDao.insertFull(timeTrial)
    }


    val nonFinishedMediator = MediatorLiveData<TimeTrial>()

    init {
        nonFinishedMediator.addSource(timeTrialDao.getNonFinishedTt()){timeTrial->
            if(timeTrial == null){
                CoroutineScope(Dispatchers.IO).launch {
                    timeTrialDao.insert(TimeTrialHeader.createBlank())
                }

                //nonFinishedMediator.value = TimeTrial.createBlank()
            }else{
                nonFinishedMediator.value = timeTrial
            }
        }
    }

    override fun getNonFinishedTimeTrial(): LiveData<TimeTrial?> {
        return nonFinishedMediator
    }

    override suspend fun getNonFinishedTimeTrialSuspend(): TimeTrial? {
        return timeTrialDao.getNonFinishedTimeTrialSuspend()
    }


    override fun getLiveTimeTrialByName(name:String): LiveData<TimeTrial> {

        return timeTrialDao.getLiveTimeTrialByName(name)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(timeTrial: TimeTrial) {
        System.out.println("JAREDMSG -> Updating ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
        if((timeTrial.timeTrialHeader.id ?: 0L) == 0L){
            throw Exception("TT ID cannot be null")
        }
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


}
