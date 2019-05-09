package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.ui.RiderStatus

object TimeTrialHelper{

    fun assignRiderToEvent(riderId: Long, eventTimestamp: Long, timeTrial: TimeTrial): RiderAssignmentResult{
        val event = timeTrial.eventList.find { it.timeStamp == eventTimestamp }
        val timeTrialRider = timeTrial.riderList.find { r -> r.rider.id == riderId }

        if(event != null && timeTrialRider!=null && event.eventType == EventType.RIDER_PASSED){
            if(event.timeStamp <= timeTrialRider.startTime) return RiderAssignmentResult(false, "Rider must have started", timeTrial)
            return when(timeTrial.getRiderStatus(riderId)){
                RiderStatus.NOT_STARTED -> RiderAssignmentResult(false, "This rider has not started", timeTrial)
                RiderStatus.FINISHED -> RiderAssignmentResult(false, "Rider has already finished", timeTrial)
                RiderStatus.RIDING -> {
                    val newEvent = event.copy(riderId = riderId)
                    val updatedList = timeTrial.eventList.map { e -> if (e.timeStamp == eventTimestamp) newEvent else e }
                    RiderAssignmentResult(true, "Success", timeTrial.copy(eventList = updatedList))
                }
            }
        }else{
            return RiderAssignmentResult(false, "Error", timeTrial)
        }
    }
}