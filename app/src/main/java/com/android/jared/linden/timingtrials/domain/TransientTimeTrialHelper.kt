package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.ui.RiderStatus

//class TransientTimeTrialHelper(val timeTrial:TimeTrial, val timeStamp:Long)
//{
//    val helper = timeTrial.helper
//
//    fun assignRiderToEvent(riderId: Long, eventTimestamp:Long): RiderAssignmentResult{
//
//        if(helper.getRiderStartTime(helper.getRiderById(riderId)) < eventTimestamp)
//
//        val event = timeTrial.eventList.asSequence().find { it.timeStamp == eventTimestamp }
//        val timeTrialRider = timeTrial.riderList.asSequence().find { r -> r.rider.id == riderId }
//
//    }
//
//    val startedRiders: Sequence<TimeTrialRider> by lazy{
//        sequence {
//            val index = helper.sparseRiderStartTimes.indexOfKey(timeStamp)
//            val startedIndexes = if(index >= 0){ index }else{ Math.abs(index) - 2 }
//            var i = 0
//            while (i <= startedIndexes) {
//                val obj = helper.sparseRiderStartTimes.valueAt(i)
//                obj?.let { yield(it) }
//                i++
//            }
//        }
//    }
//
//    fun getRiderStatus(riderId: Long, timeStap: Long): RiderStatus {
//
//        val status = RiderStatus.NOT_STARTED
//        val riderEvents = timeTrial.eventList.asSequence().filter { it.riderId == riderId }
//
//        if(riderEvents.any { it.eventType == EventType.RIDER_STARTED }){
//            val num = riderEvents.filter { it.eventType == EventType.RIDER_PASSED }.count()
//            if(num> 0){
//                return if(num == timeTrial.timeTrialHeader.laps)  RiderStatus.FINISHED else RiderStatus.RIDING
//            }
//            return RiderStatus.RIDING
//        }
//        return status
//    }
//}