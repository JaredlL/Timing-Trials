package com.android.jared.linden.timingtrials.domain

import android.util.LongSparseArray
import androidx.core.util.size
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.ui.RiderStatus
import java.util.*


class TimeTrialHelper(val timeTrial: TimeTrial) {


    fun assignRiderToEvent(ttRider: TimeTrialRider, eventTimestamp: Long): RiderAssignmentResult {

        val riderStartTime = getRiderStartTime(ttRider)
        if (riderStartTime > eventTimestamp) return RiderAssignmentResult(false, "Rider must have started", timeTrial)

        val newRiderList: List<FilledTimeTrialRider> = timeTrial.riderList.map {ttr->
            if (ttr.timeTrialData.id == ttRider.id) {
                val newSplits = (ttr.timeTrialData.splits + (eventTimestamp - riderStartTime)).sorted()

                if (ttr.timeTrialData.splits.size +1 == timeTrial.timeTrialHeader.laps){
                    ttr.updateTimeTrialData(ttr.timeTrialData.copy(splits = newSplits, finishTime = newSplits.last() - riderStartTime))

                }else{
                    ttr.updateTimeTrialData( ttr.timeTrialData.copy(splits = newSplits))
                }
            } else {
                ttr
            }
        }

        val newHeader = timeTrial.timeTrialHeader.copy(timeStamps = timeTrial.timeTrialHeader.timeStamps.minusElement(eventTimestamp))
        return RiderAssignmentResult(true, "Success", timeTrial.copy(riderList = newRiderList, timeTrialHeader = newHeader))
    }


    fun getRiderById(id: Long?): FilledTimeTrialRider? {
        return timeTrial.riderList.asSequence().firstOrNull { it.timeTrialData.riderId == id }
    }



    val finishedRiders: List<TimeTrialRider> by lazy {
        timeTrial.riderList.asSequence().map { it.timeTrialData }.filter { it.splits.size == timeTrial.timeTrialHeader.laps }.toList()
    }

//    val unFinishedRidersFromEvents: List<TimeTrialRider> by lazy {
//        timeTrial.riderList.filter { !riderEventMap.containsKey(it.id) || riderEventMap[it.id]?.size?:0 < timeTrial.timeTrialHeader.laps }
//    }

    val riderStartTimes: SortedMap<Long, FilledTimeTrialRider> by lazy {
        timeTrial.riderList.asSequence().associateBy({ getRiderStartTime(it.timeTrialData) }, { it }).toSortedMap()
    }

//    val riderEventMap: Map<Long, List<RiderPassedEvent>> by lazy {
//        timeTrial.eventList.groupBy { it.riderId?:0 }
//    }


    val sparseRiderStartTimes: LongSparseArray<FilledTimeTrialRider> by lazy {
        val arr = LongSparseArray<FilledTimeTrialRider>(timeTrial.riderList.size)
        timeTrial.riderList.forEach { r ->
            arr.append(getRiderStartTime(r.timeTrialData), r)
        }
        return@lazy arr
        //setupTimeTrial.riderList.asSequence().associateBy({(setupTimeTrial.timeTrialHeader.firstRiderStartOffset + it.startTimeOffset + it.number * setupTimeTrial.timeTrialHeader.interval)* 1000L}, {it}).toSortedMap()
    }

    fun getRiderStartTime(rider: TimeTrialRider): Long {
        return (timeTrial.timeTrialHeader.firstRiderStartOffset + rider.startTimeOffset + (timeTrial.timeTrialHeader.interval * rider.index)) * 1000L
    }



    val results : List<IResult> by lazy {
        timeTrial.riderList.map { TimeTrialRiderResult(it.timeTrialData, it.riderData, this.timeTrial.timeTrialHeader, this.timeTrial.course) }
    }




}
//    val results: List<TimeTrialResult> by lazy {
//       riderEventMap.mapNotNull { rek -> getRiderById(rek.key)?.let { rider -> TimeTrialResult(rider, (listOf(getRiderStartTime(rider)) + rek.value.map { it.timeStamp }).zipWithNext{a, b -> b - a}, timeTrial) }  }
//    }

//        val results3: List<IResult> by lazy {
//            riderEventMap.mapNotNull { rek ->
//                getRiderById(rek.key)?.let { rider ->
//                    val splits = (listOf(getRiderStartTime(rider)) + rek.value.map { it.timeStamp }).zipWithNext { a, b -> b - a }
//                    val totalTime = splits.sum()
//                    FilledResult.fromRiderCourseTT(
//                            rider.rider,
//                            timeTrial.timeTrialHeader.course ?: Course.createBlank(),
//                            totalTime,
//                            splits,
//                            timeTrial.timeTrialHeader,
//                            "")
//                }
//            }
//        }

//    val results2: List<TimeTrialResult> by lazy {
//        timeTrial.eventList.asSequence().groupBy { it.riderId }.mapNotNull { riderEvents ->
//            getRiderById(riderEvents.key)?.let { TimeTrialResult(it, riderEvents.value.asSequence().sortedBy {ev-> ev.timeStamp }.zipWithNext{ a, b -> b.timeStamp - a.timeStamp }.toList(), timeTrial) }
//        }
//    }



