package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.ui.RiderStatus

interface ITimelineEvent {
    val timeStamp: Long
    val eventType: TimelineEventType
    val rider: FilledTimeTrialRider?
}


enum class TimelineEventType(type: Int) {
//    RIDER_DNF(-2),
//    RIDER_DNS(-1),
    RIDER_STARTED(0),
    RIDER_PASSED(1),
    RIDER_FINISHED(2)

}

data class TimeLineEvent(override val timeStamp: Long, override val eventType: TimelineEventType, override val rider: FilledTimeTrialRider?) : ITimelineEvent

class TimeLine(val timeTrial: TimeTrial, val timeStamp: Long) {
    fun isValidForTimeStamp(newTimeStamp: Long): Boolean {
        return timeTrial.helper.sparseRiderStartTimes.indexOfKey(newTimeStamp) == index
    }

    fun getRiderStatus(rider: TimeTrialRider): RiderStatus {
        return when {
            rider.finishTime?.toInt() == -2 -> RiderStatus.DNF
            rider.finishTime?.toInt() == -1 -> RiderStatus.DNS
            timeStamp < timeTrial.helper.getRiderStartTime(rider) -> RiderStatus.NOT_STARTED
            rider.splits.size < timeTrial.timeTrialHeader.laps -> RiderStatus.RIDING
            else -> RiderStatus.FINISHED
        }
    }

    val index = timeTrial.helper.sparseRiderStartTimes.indexOfKey(timeStamp)

    private fun gtl(): List<ITimelineEvent> {
        val startedEvents = timeTrial.riderList.asSequence()
                .filter {
                    (it.timeTrialData.finishTime != null && it.timeTrialData.finishTime < 0L).not()
                }
                .map { ttr -> TimeLineEvent(timeTrial.helper.getRiderStartTime(ttr.timeTrialData), TimelineEventType.RIDER_STARTED, ttr) }
                .filter { it.timeStamp <= timeStamp }

        val unassignedEvents = timeTrial.timeTrialHeader.timeStamps.asSequence()
                .map { TimeLineEvent(it, TimelineEventType.RIDER_PASSED, null) }

                .takeWhile { it.timeStamp <= timeStamp }
        val assignedEvents = timeTrial.riderList.asSequence()
                .flatMap { r ->
                    r.timeTrialData.splits.asSequence()
                            .mapIndexed { i, ts ->
                                if (i < timeTrial.timeTrialHeader.laps - 1) {
                                    TimeLineEvent(timeTrial.helper.getRiderStartTime(r.timeTrialData) + ts, TimelineEventType.RIDER_PASSED, r)
                                } else {
                                    TimeLineEvent(timeTrial.helper.getRiderStartTime(r.timeTrialData) + ts, TimelineEventType.RIDER_FINISHED, r)
                                }
                            }
                }.takeWhile { it.timeStamp <= timeStamp }
        return (startedEvents + unassignedEvents + assignedEvents).sortedBy { it.timeStamp }.toList()
    }

    val timeLine: List<ITimelineEvent> by lazy { gtl() }

}

//    val timeLine: List<ITimelineEvent> =
//        (timeTrial.riderList.asSequence()
//                .map {ttr -> StartEvent(timeTrial.helper.getRider StartTime(ttr), ttr.rider.id) } + timeTrial.helper.riderEventMap.asSequence()
//                .flatMap { rep -> rep.value.asSequence().mapIndexed { index, riderPassedEvent -> if(index + 1 < timeTrial.timeTrialHeader.laps || riderPassedEvent.riderId == null){PassEvent(riderPassedEvent)} else{FinishEvent(riderPassedEvent)} } })
//                .sortedBy { it.timeStamp }.takeWhile { it.timeStamp <= timeStamp }.toList()

