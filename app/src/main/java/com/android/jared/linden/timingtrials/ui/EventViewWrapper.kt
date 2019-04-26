package com.android.jared.linden.timingtrials.ui

import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrialEvent
import com.android.jared.linden.timingtrials.data.TimeTrialWithEvents
import com.android.jared.linden.timingtrials.util.ConverterUtils

class EventViewWrapper(val event: TimeTrialEvent, val timeTrialWithEvents: TimeTrialWithEvents){

    val timeStampString = ConverterUtils.toTenthsDisplayString(event.timeStamp.minusMillis(timeTrialWithEvents.timeTrial.startTime.toEpochMilli()))

    private fun getRider(): Rider? = event.riderId?.let {  timeTrialWithEvents.timeTrial.riders.firstOrNull{rider -> rider.id == event.riderId}}


    private val riderName: String = getRider()?.let { "${it.firstName} ${it.lastName}" }?: "Null"

    val displayString: String = when(event.eventType){
        EventType.EMPTY -> "Empty Event"
        EventType.TIMETRIAL_STARTED -> "TimeTrial Has Begun"
        EventType.RIDER_STARTED -> "$riderName Has Started"
        EventType.RIDER_PASSED -> "$riderName Has Passed"
        EventType.RIDER_FINISHED -> "$riderName Has Finished"
    }
}