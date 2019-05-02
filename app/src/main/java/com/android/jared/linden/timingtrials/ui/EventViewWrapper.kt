package com.android.jared.linden.timingtrials.ui

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.BR
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.timing.IEventSelectionData
import com.android.jared.linden.timingtrials.util.ConverterUtils

class EventViewWrapper(var event: TimeTrialEvent, val timeTrialWithEvents: TimeTrial) : BaseObservable(){

    val timeStampString = ConverterUtils.toTenthsDisplayString(event.timeStamp)

    private fun getRider(): TimeTrialRider? = event.riderId?.let {  timeTrialWithEvents.riderList.firstOrNull{ r -> r.rider.id == event.riderId}}

    var getSelected: (TimeTrialEvent) -> Boolean = { _ -> false}
    var onSelectionChanged = { _: TimeTrialEvent, _:Boolean -> Unit}

    @Bindable
    fun getEventSelected():Boolean {
       return getSelected(event)
    }
    fun setEventSelected(value:Boolean) {
        onSelectionChanged(event, value)
    }

    private val riderName: String = getRider()?.let { "[${it.number}] ${it.rider.firstName} ${it.rider.lastName}" }?: "Null"

    val displayString: String = when(event.eventType){
        EventType.EMPTY -> "Empty Event"
        EventType.TIMETRIAL_STARTED -> "TimeTrialHeader Has Begun"
        EventType.RIDER_STARTED -> "$riderName Has Started"
        EventType.RIDER_PASSED -> event.riderId?.let { "$riderName Has Passed"}?:"Assign Rider"
        EventType.RIDER_FINISHED -> "$riderName Has Finished"
    }
}