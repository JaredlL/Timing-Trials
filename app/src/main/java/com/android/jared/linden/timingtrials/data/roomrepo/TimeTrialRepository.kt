package com.android.jared.linden.timingtrials.data.roomrepo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.data.source.TimeTrialDao
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface ITimeTrialRepository{

    suspend fun insert(timeTrial: TimeTrial):Long

    suspend fun insertNewHeader(timeTrialHeader: TimeTrialHeader):Long

    suspend fun update(timeTrialHeader: TimeTrialHeader)
    suspend fun updateFull(timeTrial: TimeTrial)
    suspend fun getTimeTrialByName(name: String): TimeTrial?
    suspend fun getHeadersByName(name: String): List<TimeTrialHeader>

    suspend fun delete(timeTrial: TimeTrial)

    suspend fun deleteHeader(timeTrialHeader: TimeTrialHeader)
    //val nonFinishedTimeTrial: LiveData<TimeTrialWithCourse?>
    fun getSetupTimeTrialById(timeTrialId: Long): LiveData<TimeTrial?>

    fun getTimingTimeTrial():LiveData<TimeTrial?>

    fun getLiveTimeTrialByName(name:String): LiveData<TimeTrial>
    fun getResultTimeTrialById(id: Long): LiveData<TimeTrial?>
    val allTimeTrialsHeader: LiveData<List<TimeTrialHeader>>
}

@Singleton
class RoomTimeTrialRepository @Inject constructor(private val timeTrialDao: TimeTrialDao): ITimeTrialRepository {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun getTimeTrialByName(name: String): TimeTrial? {
        return timeTrialDao.getTimeTrialByName(name)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun getHeadersByName(name: String): List<TimeTrialHeader> {
        return timeTrialDao.getHeadersByName(name)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun updateFull(timeTrial: TimeTrial) {
        Timber.d("JAREDMSG -> TTREPO -> Updating ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
        if((timeTrial.timeTrialHeader.id ?: 0L) == 0L){
            throw Exception("TT ID cannot be null")
        }
        timeTrialDao.update(timeTrial)
    }

    override val allTimeTrialsHeader: LiveData<List<TimeTrialHeader>> = timeTrialDao.getAllTimeTrials()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(timeTrial: TimeTrial):Long {
        Timber.d("JAREDMSG -> TTREPO -> Inserting New TT ${timeTrial.timeTrialHeader.id} ${timeTrial.timeTrialHeader.ttName} into DB from background thread")
//        if(timeTrial.timeTrialHeader.status != TimeTrialStatus.FINISHED){
//            throw Exception("Cannot insertFull non finished TT")
//        }
       return timeTrialDao.insertFull(timeTrial)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insertNewHeader(timeTrial: TimeTrialHeader):Long {
        Timber.d("JAREDMSG -> TTREPO -> Inserting New TT Header into DB from background thread")
        return timeTrialDao.insert(timeTrial)
    }


//    private val inserting: MutableLiveData<Boolean> = MutableLiveData(false)
//
//
//
//    private val nonFinishedMediator = MediatorLiveData<TimeTrial>()
//
//    private val insertingBool = AtomicBoolean()
//    init {
//        nonFinishedMediator.addSource(timeTrialDao.getNonFinishedFullTtLive()){timeTrial->
//            println("JAREDMSG -> TTREPO -> Getting time trial ${timeTrial?.timeTrialHeader?.id} ${timeTrial?.timeTrialHeader?.ttName}")
//            if(timeTrial == null) {
//                if (!insertingBool.get()) {
//                    insertingBool.set(true)
//                    println("JAREDMSG -> TTREPO -> Set inserting to TRUE")
//                    CoroutineScope(Dispatchers.IO).launch {
//                        println("JAREDMSG -> TTREPO Corotine launched, going to insert")
//                        val id = timeTrialDao.insert(TimeTrialHeader.createBlank())
//                        println("JAREDMSG -> TTREPO TT Inserted ${id} -> Set inserting to FALSE")
//                        insertingBool.set(false)
//                    }
//                }
//            }
//            println("JAREDMSG -> TTREPO -> SET nonFinishedMediator to ${timeTrial?.timeTrialHeader?.id} ${timeTrial?.timeTrialHeader?.ttName}")
//            nonFinishedMediator.value = timeTrial
//        }
//    }

//    override val nonFinishedFullTimeTrial: LiveData<TimeTrial?> = Transformations.map(nonFinishedMediator){tt->
//        tt?.copy(riderList = tt.riderList.sortedBy { it.timeTrialData.index }) ?: tt
//    }




    override fun getLiveTimeTrialByName(name:String): LiveData<TimeTrial> {

        return timeTrialDao.getLiveTimeTrialByName(name)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(timeTrialHeader: TimeTrialHeader) {
        System.out.println("JAREDMSG -> TTREPO -> Updating ${timeTrialHeader.id} ${timeTrialHeader.ttName} into DB from background thread")
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
        return Transformations.map(timeTrialDao.getTimingTimeTrials()){
            if(it.size > 1){
                throw Exception("Multiple Timing TimeTrials In DB!!!")
            }
            it.firstOrNull()
        }
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun delete(timeTrial: TimeTrial) {
        timeTrialDao.delete(timeTrial)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun deleteHeader(timeTrialHeader: TimeTrialHeader) {
        timeTrialDao.delete(timeTrialHeader)
    }


}
