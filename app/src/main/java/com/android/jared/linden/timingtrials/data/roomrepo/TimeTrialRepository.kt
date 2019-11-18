package com.android.jared.linden.timingtrials.data.roomrepo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.data.source.TimeTrialDao
import kotlinx.coroutines.*
import java.sql.Time
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

interface ITimeTrialRepository{

    suspend fun insert(timeTrial: TimeTrial):Long
    suspend fun update(timeTrial: TimeTrial)
    suspend fun getTimeTrialByName(name: String): TimeTrial?
    suspend fun delete(timeTrial: TimeTrial)
    val nonFinishedTimeTrial: LiveData<TimeTrial?>
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
        System.out.println("JAREDMSG -> TTREPO -> Inserting New TT ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
        if(timeTrial.timeTrialHeader.status != TimeTrialStatus.FINISHED){
            throw Exception("Cannot insertFull non finished TT")
        }
       return timeTrialDao.insertFull(timeTrial)
    }


    private val inserting: MutableLiveData<Boolean> = MutableLiveData(false)

    override val nonFinishedTimeTrial: LiveData<TimeTrial?>
        get() = nonFinishedMediator

    private val nonFinishedMediator = MediatorLiveData<TimeTrial>()

    private val insertingBool = AtomicBoolean()
    init {
        nonFinishedMediator.addSource(timeTrialDao.getNonFinishedTt()){timeTrial->
            println("JAREDMSG -> TTREPO -> Getting time trial ${timeTrial?.timeTrialHeader?.id} ${timeTrial?.timeTrialHeader?.ttName}")
            if(timeTrial == null) {
                if (!insertingBool.get()) {
                    insertingBool.set(true)
                    println("JAREDMSG -> TTREPO -> Set inserting to TRUE")
                    CoroutineScope(Dispatchers.IO).launch {
                        println("JAREDMSG -> TTREPO Corotine launched, going to insert")
                        val id = timeTrialDao.insert(TimeTrialHeader.createBlank())
                        println("JAREDMSG -> TTREPO TT Inserted ${id} -> Set inserting to FALSE")
                        insertingBool.set(false)
                    }
                }
            }
            println("JAREDMSG -> TTREPO -> SET nonFinishedMediator to ${timeTrial?.timeTrialHeader?.id} ${timeTrial?.timeTrialHeader?.ttName}")
            nonFinishedMediator.value = timeTrial
        }
    }




    override fun getLiveTimeTrialByName(name:String): LiveData<TimeTrial> {

        return timeTrialDao.getLiveTimeTrialByName(name)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(timeTrial: TimeTrial) {
        System.out.println("JAREDMSG -> TTREPO -> Updating ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
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
