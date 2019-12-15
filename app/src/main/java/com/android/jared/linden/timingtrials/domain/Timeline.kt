package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.ui.RiderStatus

interface ITimelineEvent
{
    val timeStamp: Long
    val eventType: TimelineEventType
    val rider: FilledTimeTrialRider?
}

//data class StartEvent(override val timeStamp: Long, override val riderId: Long?): ITimelineEvent{
//    override val eventType: TimelineEventType = TimelineEventType.RIDER_STARTED
//    val m2 = riderId
//}
//
//data class PassEvent(val timeTrialRider: TimeTrialRider, override val timeStamp: Long): ITimelineEvent{
//   //override val timeStamp: Long = riderPassedEvent.timeStamp
//   override val eventType: TimelineEventType = TimelineEventType.RIDER_PASSED
//   override val riderId: Long? = riderPassedEvent.riderId
//}
//
//data class FinishEvent(val timeTrialRider: TimeTrialRider, override val timeStamp: Long): ITimelineEvent{
//    override val eventType: TimelineEventType = TimelineEventType.RIDER_FINISHED
//    override val riderId: Long? = riderPassedEvent.riderId
//}


enum class TimelineEventType(type:Int){
    RIDER_STARTED(0),
    RIDER_PASSED(1),
    RIDER_FINISHED(2)

}

data class TimeLineEvent(override val timeStamp: Long, override val eventType: TimelineEventType, override val rider: FilledTimeTrialRider?):ITimelineEvent

class TimeLine(val timeTrial: TimeTrial, val timeStamp: Long)
{
    fun isValidForTimeStamp(newTimeStamp: Long): Boolean{
       return timeTrial.helper.sparseRiderStartTimes.indexOfKey(newTimeStamp) == index
    }

    fun getRiderStatus(rider:TimeTrialRider): RiderStatus{
        if(timeStamp < timeTrial.helper.getRiderStartTime(rider)) return RiderStatus.NOT_STARTED
        else if(rider.splits.size < timeTrial.timeTrialHeader.laps) return RiderStatus.RIDING
        else return RiderStatus.FINISHED
    }

    val index = timeTrial.helper.sparseRiderStartTimes.indexOfKey(timeStamp)

    private fun gtl(): List<ITimelineEvent>{
        val startedEvents = timeTrial.riderList.asSequence().map { ttr-> TimeLineEvent(timeTrial.helper.getRiderStartTime(ttr.timeTrialData), TimelineEventType.RIDER_STARTED, ttr) }
        val unassignedEvents = timeTrial.timeTrialHeader.timeStamps.asSequence().map { TimeLineEvent(it, TimelineEventType.RIDER_PASSED, null) }
        val assignedEvents = timeTrial.riderList.asSequence().flatMap { r-> r.timeTrialData.splits.asSequence().mapIndexed { i,ts ->
            if(i <= timeTrial.timeTrialHeader.laps-1){
                TimeLineEvent(timeTrial.helper.getRiderStartTime(r.timeTrialData) + ts, TimelineEventType.RIDER_PASSED, r)
            }
            else{
                TimeLineEvent(timeTrial.helper.getRiderStartTime(r.timeTrialData) + ts, TimelineEventType.RIDER_FINISHED, r)
            }
             } }
        return (startedEvents + unassignedEvents + assignedEvents).takeWhile { it.timeStamp <= timeStamp }.sortedBy { it.timeStamp }.toList()
    }

    val timeLine: List<ITimelineEvent> by lazy { gtl() }

}

//    val timeLine: List<ITimelineEvent> =
//        (timeTrial.riderList.asSequence()
//                .map {ttr -> StartEvent(timeTrial.helper.getRiderStartTime(ttr), ttr.rider.id) } + timeTrial.helper.riderEventMap.asSequence()
//                .flatMap { rep -> rep.value.asSequence().mapIndexed { index, riderPassedEvent -> if(index + 1 < timeTrial.timeTrialHeader.laps || riderPassedEvent.riderId == null){PassEvent(riderPassedEvent)} else{FinishEvent(riderPassedEvent)} } })
//                .sortedBy { it.timeStamp }.takeWhile { it.timeStamp <= timeStamp }.toList()

