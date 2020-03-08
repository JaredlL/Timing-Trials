package com.android.jared.linden.timingtrials.domain

import android.util.LongSparseArray
import androidx.core.util.size
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.ui.RiderStatus
import org.threeten.bp.Instant
import java.util.*


class TimeTrialHelper(val timeTrial: TimeTrial) {


    fun assignRiderToEvent(ttRider: TimeTrialRider, eventTimestamp: Long): RiderAssignmentResult {

        if(ttRider.finishTime != null && ttRider.finishTime < 0){
            return RiderAssignmentResult(false, "Rider did not start or finish", timeTrial)
        }

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

    fun unassignRiderFromEvent(ttRider: TimeTrialRider, eventTimestamp: Long): TimeTrial{

        val convertedTimeStamp = eventTimestamp - getRiderStartTime(ttRider)

        return timeTrial.copy(riderList = timeTrial.riderList.map {
            if(it.timeTrialData.id == ttRider.id)
            {
                it.copy(timeTrialData = it.timeTrialData.copy(splits = it.timeTrialData.splits.minus(convertedTimeStamp)))
            }
            else{
                it
            }
        }, timeTrialHeader = timeTrial.timeTrialHeader.copy(timeStamps = timeTrial.timeTrialHeader.timeStamps + eventTimestamp))


    }

    fun riderDnf(rider: TimeTrialRider): TimeTrial{
        return timeTrial.copy(riderList = timeTrial.riderList.map {
            if(it.timeTrialData.id == rider.id)
            {
                it.copy(timeTrialData = it.timeTrialData.copy(finishTime = -2, notes = "DNF"))
            }
            else{
                it
            }
        })
    }

    fun setRiderStartTime(riderId: Long, newStartTime: Long): TimeTrial{
        return timeTrial.copy(riderList = timeTrial.riderList.map {
            if(it.timeTrialData.id == riderId)
            {
                val baseRiderStartTime = getBaseRiderStartTime(it.timeTrialData) + timeTrial.timeTrialHeader.startTimeMilis
                val offset  = newStartTime - baseRiderStartTime
                it.copy(timeTrialData = it.timeTrialData.copy(startTimeOffset = (offset/1000).toInt()))
            }
            else{
                it
            }
        })
    }

    fun riderDns(rider: TimeTrialRider): TimeTrial{
        return timeTrial.copy(riderList = timeTrial.riderList.map {
            if(it.timeTrialData.id == rider.id)
            {
                it.copy(timeTrialData = it.timeTrialData.copy(finishTime = -1, notes = "DNS"))
            }
            else{
                it
            }
        })
    }

    fun undoDnf(rider: TimeTrialRider): TimeTrial{
        return timeTrial.copy(riderList = timeTrial.riderList.map {
            if(it.timeTrialData.id == rider.id)
            {
                it.copy(timeTrialData = it.timeTrialData.copy(finishTime = null, notes = ""))
            }
            else{
                it
            }
        })
    }

    fun moveRiderToBack(rider: TimeTrialRider): TimeTrial{


        val milisNow = System.currentTimeMillis()

        val lastStartTime = timeTrial.timeTrialHeader.startTimeMilis + sortedRiderStartTimes.filter { it.value.timeTrialData.hasNotDnfed() }.keys.last()

        val interval = (if(timeTrial.timeTrialHeader.interval == 0) 60 else timeTrial.timeTrialHeader.interval) * 1000

        val nextStartTime = if(milisNow < (lastStartTime + interval)){

            lastStartTime + interval
        }else{
            val millisSinceStart = milisNow - timeTrial.timeTrialHeader.startTimeMilis
            (millisSinceStart / interval) * interval + interval *2 + timeTrial.timeTrialHeader.startTimeMilis
        }

        val offsetTime = nextStartTime -  getBaseRiderStartTime(rider) - timeTrial.timeTrialHeader.startTimeMilis
        return timeTrial.copy(riderList = timeTrial.riderList.map {
            if(it.timeTrialData.id == rider.id)
            {
                it.copy(timeTrialData = it.timeTrialData.copy(startTimeOffset = (offsetTime/1000).toInt()))
            }
            else{
                it
            }
        })



    }


    val finishedRiders: List<TimeTrialRider> by lazy {
        timeTrial.riderList.asSequence().map { it.timeTrialData }.filter { it.splits.size == timeTrial.timeTrialHeader.laps }.toList()
    }


    val sortedRiderStartTimes: SortedMap<Long, FilledTimeTrialRider> by lazy {
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

    fun getBaseRiderStartTime(rider: TimeTrialRider): Long {
        return (timeTrial.timeTrialHeader.firstRiderStartOffset + (timeTrial.timeTrialHeader.interval * rider.index)) * 1000L
    }

    val ridersInStartOrder: List<FilledTimeTrialRider> by lazy {
        timeTrial.riderList.sortedBy { it.timeTrialData.index }.toList()
    }

    val filledRiderResults : List<FilledTimeTrialRider> by lazy {
        timeTrial.riderList.asSequence()
                .sortedBy { it.timeTrialData.finishTime?:Long.MAX_VALUE }
                .toList()
    }

    val results : List<TimeTrialRiderResult> by lazy {
        timeTrial.riderList.asSequence()
                .map { TimeTrialRiderResult(it.timeTrialData, it.riderData, this.timeTrial.timeTrialHeader, this.timeTrial.course) }
                .sortedBy { it.resultTime?:Long.MAX_VALUE }
                .toList()
    }






}



