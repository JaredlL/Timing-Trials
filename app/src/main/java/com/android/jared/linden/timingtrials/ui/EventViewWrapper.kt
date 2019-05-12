package com.android.jared.linden.timingtrials.ui

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.BR
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.timing.IEventSelectionData
import com.android.jared.linden.timingtrials.util.ConverterUtils

class EventViewWrapper(var event: TimeTrialEvent, val timeTrial: TimeTrial) : BaseObservable(){

    val timeStampString = ConverterUtils.toTenthsDisplayString(event.timeStamp)

    private fun getRider(): TimeTrialRider? = event.riderId?.let {  timeTrial.riderList.firstOrNull{ r -> r.rider.id == event.riderId}}

    var getSelected: (TimeTrialEvent) -> Boolean = { _ -> false}
    var onSelectionChanged = { e: TimeTrialEvent, b:Boolean -> Unit}

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
        EventType.RIDER_STARTED -> "$riderName Started"
        EventType.RIDER_PASSED ->
        {
            event.riderId?.let { id->
               return@let when(timeTrial.helper.getRiderStatus(id)){
                   RiderStatus.FINISHED -> "$riderName Finished"
                   else -> "$riderName Has Passed"
                }
            }?:"Assign Rider"
        }
    }
}