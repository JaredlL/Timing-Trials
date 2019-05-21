package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.RiderPassedEvent
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.ui.RiderStatus

interface ITimelineEvent
{
    val timeStamp: Long
    val eventType: TimelineEventType
    val riderId: Long?
}

data class StartEvent(override val timeStamp: Long, override val riderId: Long?): ITimelineEvent{
    override val eventType: TimelineEventType = TimelineEventType.RIDER_STARTED
    val m2 = riderId
}

data class PassEvent(val riderPassedEvent: RiderPassedEvent): ITimelineEvent{
   override val timeStamp: Long = riderPassedEvent.timeStamp
   override val eventType: TimelineEventType = TimelineEventType.RIDER_PASSED
   override val riderId: Long? = riderPassedEvent.riderId
}


enum class TimelineEventType(type:Int){
    RIDER_STARTED(0),
    RIDER_PASSED(1)

}

class TimeLine(val timeTrial: TimeTrial, val timeStamp: Long)
{
    fun isValidForTimeStamp(newTimeStamp: Long): Boolean{
       return timeTrial.helper.sparseRiderStartTimes.indexOfKey(newTimeStamp) == index
    }

    fun getRiderStatus(rider:TimeTrialRider): RiderStatus{
        if(timeStamp < timeTrial.helper.getRiderStartTime(rider)) return RiderStatus.NOT_STARTED
        if(timeTrial.eventList.asSequence().filter { it.riderId == rider.rider.id }.count() >= timeTrial.timeTrialHeader.laps) return RiderStatus.FINISHED
        return RiderStatus.RIDING
    }

    val index = timeTrial.helper.sparseRiderStartTimes.indexOfKey(timeStamp)

    val timeLine: List<ITimelineEvent> by lazy {
        val eventMap = timeTrial.eventList.groupBy { it.riderId }
        fds
        (timeTrial.riderList.asSequence().map {ttr -> StartEvent(timeTrial.helper.getRiderStartTime(ttr), ttr.rider.id) } + timeTrial.eventList.asSequence().map { PassEvent(it) }).sortedBy { it.timeStamp }.takeWhile { it.timeStamp < timeStamp }.toList()
    }
}