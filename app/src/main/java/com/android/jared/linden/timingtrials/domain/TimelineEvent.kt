package com.android.jared.linden.timingtrials.domain

interface ITimelineEvent
{
    val timeStamp: Long
    val eventType: TimelineEventType
    val message: String
    val riderId: Long?
}





enum class TimelineEventType(type:Int){
    RIDER_STARTED(0),
    RIDER_PASSED(1),
    RIDER_FINISHED(2);

}