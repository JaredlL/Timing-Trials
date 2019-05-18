//package com.android.jared.linden.timingtrials.data
//
//import androidx.annotation.WorkerThread
//import androidx.lifecycle.LiveData
//import com.android.jared.linden.timingtrials.data.source.TimeTrialEventDao
//import javax.inject.Inject
//import javax.inject.Singleton
//
//interface ITimeTrialEventRepository{
//
//    suspend fun updateTimeTrialEvents(setupTimeTrial: TimeTrialWithEvents)
//    fun getSetupTimeTrial(timeTrialId: Long): LiveData<TimeTrialWithEvents>
//    fun getAllTimeTrialEvents(): LiveData<List<TimeTrialWithEvents>>
//
//}
//
//@Singleton
//class RoomTimeTrialEventRepository @Inject  constructor(private val timeTrialEventDao: TimeTrialEventDao) : ITimeTrialEventRepository{
//
//
//    override fun getSetupTimeTrial(timeTrialId: Long): LiveData<TimeTrialWithEvents> {
//        return timeTrialEventDao.getTimeTrialEvents(timeTrialId)
//    }
//
//    override fun getAllTimeTrialEvents(): LiveData<List<TimeTrialWithEvents>> {
//        return timeTrialEventDao.getAllTimeTrialsEvents()
//    }
//
//
//    @WorkerThread
//    override suspend fun updateTimeTrialEvents(setupTimeTrial: TimeTrialWithEvents) {
//        timeTrialEventDao.updateTimeTrialEvents(setupTimeTrial)
//    }
//
//}