package com.android.jared.linden.timingtrials.ui

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.domain.ITimelineEvent
import com.android.jared.linden.timingtrials.domain.TimelineEventType
import com.android.jared.linden.timingtrials.util.ConverterUtils

class EventViewWrapper(val event: ITimelineEvent, val timeTrial: TimeTrial) : BaseObservable(){

    val timeStampString = ConverterUtils.toTenthsDisplayString(event.timeStamp)

    private fun getRider(): TimeTrialRider? = event.riderId?.let {  timeTrial.riderList.firstOrNull{ r -> r.rider.id == event.riderId}}

    var getSelected: (ITimelineEvent) -> Boolean = { _ -> false}
    var onSelectionChanged = { _: ITimelineEvent, _:Boolean -> Unit}

    @Bindable
    fun getEventSelected():Boolean {
       return getSelected(event)
    }
    fun setEventSelected(value:Boolean) {
        onSelectionChanged(event, value)
    }

    private val riderName: String = getRider()?.let { "[${it.number}] ${it.rider.firstName} ${it.rider.lastName}" }?: "Null"

    val displayString: String = when(event.eventType){
        TimelineEventType.RIDER_STARTED -> "$riderName Started"
        TimelineEventType.RIDER_FINISHED -> "$riderName Finished"
        TimelineEventType.RIDER_PASSED ->
        {
            if(event.riderId != null){
                "$riderName Has Passed"
            }else{
                "Assign Rider"
            }
        }
    }
}