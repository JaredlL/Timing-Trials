package com.android.jared.linden.timingtrials.domain

import android.util.LongSparseArray
import androidx.core.util.size
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.ui.RiderStatus
import java.util.*


class TimeTrialHelper(val timeTrial: TimeTrial){

    fun assignRiderToEvent(riderId: Long, eventTimestamp: Long): RiderAssignmentResult{
        val event = timeTrial.eventList.asSequence().find { it.timeStamp == eventTimestamp }
        val timeTrialRider = timeTrial.riderList.asSequence().find { r -> r.rider.id == riderId }

        if(event != null && timeTrialRider!=null && event.eventType == EventType.RIDER_PASSED){

            //val ridersWhoStartedBeforeEvent = riderStartTimes.headMap(eventTimestamp)
            val index = sparseRiderStartTimes.indexOfKey(eventTimestamp)
            val unStartedIndexes = if(index >= 0){ index }else{ Math.abs(index) - 1 }
            var i = unStartedIndexes
            while (i < sparseRiderStartTimes.size) {
                val obj = sparseRiderStartTimes.valueAt(i)
                if(obj.rider.id == riderId) return RiderAssignmentResult(false, "Rider must have started", timeTrial)
                i++
            }

            //if(!ridersWhoStartedBeforeEvent.values.asSequence().map { it.rider.id }.contains(riderId)) return RiderAssignmentResult(false, "Rider must have started", setupTimeTrial)
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

    fun getRiderById(id: Long?): TimeTrialRider?{
        return timeTrial.riderList.asSequence().firstOrNull { it.rider.id == id }
    }

    fun getRiderStatus(riderId: Long): RiderStatus{

        val status = RiderStatus.NOT_STARTED
        val riderEvents = timeTrial.eventList.filter { it.riderId == riderId }


        if(riderEvents.asSequence().any { it.eventType == EventType.RIDER_STARTED }){
            val num = riderEvents.filter { it.eventType == EventType.RIDER_PASSED }.count()
            if(num> 0){
                return if(num == timeTrial.timeTrialHeader.laps)  RiderStatus.FINISHED else RiderStatus.RIDING
            }
            return RiderStatus.RIDING
        }
        return status
    }

    val unfinishedRiders: List<TimeTrialRider> by lazy{
        timeTrial.riderList.asSequence().filter { r-> getRiderStatus(r.rider.id?:0) != RiderStatus.FINISHED }.toList()
    }

    fun addRidersAsTimeTrialRiders(riders: List<Rider>): TimeTrial{
        return timeTrial.copy(riderList =  riders.asSequence().mapIndexed { index, rider -> TimeTrialRider(rider, timeTrial.timeTrialHeader.id?:0L, number = index + 1) }.toList())
    }

    val departedRidersFromEvents: List<TimeTrialRider> by lazy {
        timeTrial.eventList.asSequence().filter { it.eventType == EventType.RIDER_STARTED}.mapNotNull { event-> timeTrial.riderList.firstOrNull { rn -> rn.rider.id == event.riderId }  }.toList()
    }

    val finishedRidersFromEvents: List<TimeTrialRider> by lazy {
        timeTrial.eventList.asSequence().filter { it.eventType == EventType.RIDER_PASSED }.groupBy { it.riderId }.filter { it.value.count() == timeTrial.timeTrialHeader.laps }.keys.mapNotNull { timeTrial.riderList.find { r-> r.rider.id == it } }
    }

    val riderStartTimes: SortedMap<Long, TimeTrialRider> by lazy {
        timeTrial.riderList.asSequence().associateBy({getRiderStartTime(it)}, {it}).toSortedMap()
    }

    val sparseRiderStartTimes: LongSparseArray<TimeTrialRider> by lazy {
        val arr = LongSparseArray<TimeTrialRider>(timeTrial.riderList.size)
        timeTrial.riderList.forEach { r->
            arr.append(getRiderStartTime(r), r)
        }
        return@lazy arr
        //setupTimeTrial.riderList.asSequence().associateBy({(setupTimeTrial.timeTrialHeader.firstRiderStartOffset + it.startTimeOffset + it.number * setupTimeTrial.timeTrialHeader.interval)* 1000L}, {it}).toSortedMap()
    }

     fun getRiderStartTime(rider: TimeTrialRider): Long{
        return (timeTrial.timeTrialHeader.firstRiderStartOffset + rider.startTimeOffset + rider.number * timeTrial.timeTrialHeader.interval)* 1000L
    }

    val results: List<TimeTrialResult> by lazy {
        timeTrial.eventList.asSequence().groupBy { it.riderId }.mapNotNull { riderEvents ->
            getRiderById(riderEvents.key)?.let { TimeTrialResult(it, riderEvents.value.asSequence().sortedBy { it.timeStamp }.zipWithNext{ a, b -> b.timeStamp - a.timeStamp }.toList()) }
        }
    }
}

