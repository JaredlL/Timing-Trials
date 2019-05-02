package com.android.jared.linden.timingtrials.ui

import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.util.ConverterUtils

class EventViewWrapper(var event: TimeTrialEvent, val timeTrialWithEvents: TimeTrial){

    val timeStampString = ConverterUtils.toTenthsDisplayString(event.timeStamp)

    private fun getRider(): Rider? = event.riderId?.let {  timeTrialWithEvents.riderList.map { it.rider }.firstOrNull{ r -> r.id == event.riderId}}


    private val riderName: String = getRider()?.let { "${it.firstName} ${it.lastName}" }?: "Null"

    val displayString: String = when(event.eventType){
        EventType.EMPTY -> "Empty Event"
        EventType.TIMETRIAL_STARTED -> "TimeTrialHeader Has Begun"
        EventType.RIDER_STARTED -> "$riderName Has Started"
        EventType.RIDER_PASSED -> event.riderId?.let { "$riderName Has Passed"}?:"Assign Rider"
        EventType.RIDER_FINISHED -> "$riderName Has Finished"
    }
}