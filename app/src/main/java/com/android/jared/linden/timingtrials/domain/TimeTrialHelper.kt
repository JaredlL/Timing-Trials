package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.ui.RiderStatus
import java.util.*

class TimeTrialHelper(val timeTrial: TimeTrial){

    fun assignRiderToEvent(riderId: Long, eventTimestamp: Long): RiderAssignmentResult{
        val event = timeTrial.eventList.find { it.timeStamp == eventTimestamp }
        val timeTrialRider = timeTrial.riderList.find { r -> r.rider.id == riderId }

        if(event != null && timeTrialRider!=null && event.eventType == EventType.RIDER_PASSED){

            val ridersWhoStartedBeforeEvent = riderStartTimes.headMap(eventTimestamp)

            if(!ridersWhoStartedBeforeEvent.values.map { it.rider.id }.contains(riderId)) return RiderAssignmentResult(false, "Rider must have started", timeTrial)
            return when(getRiderStatus(riderId)){
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

    fun getRiderStatus(riderId: Long): RiderStatus{

        val status = RiderStatus.NOT_STARTED
        val riderEvents = timeTrial.eventList.filter { it.riderId == riderId }


        if(riderEvents.any { it.eventType == EventType.RIDER_STARTED }){
            val num = riderEvents.filter { it.eventType == EventType.RIDER_PASSED }.count()
            if(num> 0){
                return if(num == timeTrial.timeTrialHeader.laps)  RiderStatus.FINISHED else RiderStatus.RIDING
            }
            return RiderStatus.RIDING
        }
        return status
    }

    val unfinishedRiders: List<TimeTrialRider> by lazy{
        timeTrial.riderList.filter { r-> getRiderStatus(r.rider.id?:0) != RiderStatus.FINISHED }
    }

    fun addRidersAsTimeTrialRiders(riders: List<Rider>): TimeTrial{
        return timeTrial.copy(riderList =  riders.mapIndexed { index, rider -> TimeTrialRider(rider, timeTrial.timeTrialHeader.id?:0L, index + 1, (timeTrial.timeTrialHeader.firstRiderStartOffset + index * timeTrial.timeTrialHeader.interval).toLong()) })
    }

    val departedRidersFromEvents: List<TimeTrialRider> by lazy {
        timeTrial.eventList.filter { it.eventType == EventType.RIDER_STARTED}.mapNotNull { event-> timeTrial.riderList.firstOrNull { rn -> rn.rider.id == event.riderId }  }
    }

    val finishedRidersFromEvents: List<TimeTrialRider> by lazy {
        timeTrial.eventList.filter { it.eventType == EventType.RIDER_PASSED }.groupBy { it.riderId }.filter { it.value.count() == timeTrial.timeTrialHeader.laps }.keys.mapNotNull { timeTrial.riderList.find { r-> r.rider.id == it } }
    }

    val riderStartTimes: SortedMap<Long, TimeTrialRider> by lazy {

        timeTrial.riderList.associateBy({timeTrial.timeTrialHeader.firstRiderStartOffset + it.startTimeOffset * 1000 + it.number * timeTrial.timeTrialHeader.interval * 1000}, {it}).toSortedMap()
        //return mapOf(map.associateBy ( {it.first * this.timeTrialHeader.interval}, {it.second} ))
    }
}