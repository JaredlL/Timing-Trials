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
                    ttr.updateTimeTrialData(ttr.timeTrialData.copy(splits = newSplits, finishTime = newSplits.last()))

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


    val finishedRiders: List<TimeTrialRider> by lazy {
        timeTrial.riderList.asSequence().map { it.timeTrialData }.filter { it.splits.size == timeTrial.timeTrialHeader.laps }.toList()
    }


    val riderStartTimes: SortedMap<Long, FilledTimeTrialRider> by lazy {
        timeTrial.riderList.asSequence().associateBy({ getRiderStartTime(it.timeTrialData) }, { it }).toSortedMap()
    }


    val sparseRiderStartTimes: LongSparseArray<FilledTimeTrialRider> by lazy {
        val arr = LongSparseArray<FilledTimeTrialRider>(timeTrial.riderList.size)
        timeTrial.riderList.forEach { r ->
            arr.append(getRiderStartTime(r.timeTrialData), r)
        }
        return@lazy arr
    }

    fun getRiderStartTime(rider: TimeTrialRider): Long {
        return (timeTrial.timeTrialHeader.firstRiderStartOffset + rider.startTimeOffset + (timeTrial.timeTrialHeader.interval * rider.index)) * 1000L
    }


    val results : List<IResult> by lazy {
        timeTrial.riderList.map { TimeTrialRiderResult(it.timeTrialData, it.riderData, this.timeTrial.timeTrialHeader, this.timeTrial.course) }
    }




}



