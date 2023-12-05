package com.jaredlinden.timingtrials.domain

import com.jaredlinden.timingtrials.data.FilledTimeTrialRider
import com.jaredlinden.timingtrials.data.FinishCode
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialRider
import com.jaredlinden.timingtrials.ui.RiderStatus

interface ITimelineEvent {
    val timeStamp: Long
    val eventType: TimelineEventType
    val rider: FilledTimeTrialRider?
}


enum class TimelineEventType(type: Int) {
    RIDER_STARTED(0),
    RIDER_PASSED(1),
    RIDER_FINISHED(2)

}

data class TimeLineEvent(override val timeStamp: Long, override val eventType: TimelineEventType, override val rider: FilledTimeTrialRider?) : ITimelineEvent

class TimeLine(val timeTrial: TimeTrial, private val timeStamp: Long) {
    fun isValidForTimeStamp(newTimeStamp: Long): Boolean {
        return timeTrial.helper.sparseRiderStartTimes.indexOfKey(newTimeStamp) == index
    }

    fun getRiderStatus(rider: TimeTrialRider): RiderStatus {
        return when {
            rider.finishCode == FinishCode.DNF.type -> RiderStatus.DNF
            rider.finishCode == FinishCode.DNS.type -> RiderStatus.DNS
            timeStamp < timeTrial.helper.getRiderStartTime(rider) -> RiderStatus.NOT_STARTED
            rider.splits.size < timeTrial.timeTrialHeader.laps -> RiderStatus.RIDING
            else -> RiderStatus.FINISHED
        }
    }

    val index = timeTrial.helper.sparseRiderStartTimes.indexOfKey(timeStamp)

    private fun createTimeLine(): List<ITimelineEvent> {
        val startedEvents = timeTrial.riderList.asSequence()
                .filter {
                    (it.timeTrialData.finishCode != null && it.timeTrialData.finishCode == FinishCode.DNS.type).not()
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
                }
                .takeWhile{ it.timeStamp <= timeStamp }
        return (startedEvents + unassignedEvents + assignedEvents).sortedBy { it.timeStamp }.toList()
    }

    val timeLine: List<ITimelineEvent> by lazy { createTimeLine() }

}

