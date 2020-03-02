package com.android.jared.linden.timingtrials.ui

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.domain.ITimelineEvent
import com.android.jared.linden.timingtrials.domain.TimelineEventType
import com.android.jared.linden.timingtrials.util.ConverterUtils

class EventViewWrapper(val event: ITimelineEvent, val timeTrial: TimeTrial) : BaseObservable(){

    val timeStampString = ConverterUtils.toTenthsDisplayString(event.timeStamp)

    var getSelected: (ITimelineEvent) -> Boolean = { _ -> false}
    var onSelectionChanged = { _: ITimelineEvent, _:Boolean -> Unit}

    @Bindable
    fun getEventSelected():Boolean {
       return getSelected(event)
    }
    fun setEventSelected(value:Boolean) {
        onSelectionChanged(event, value)
    }

    private val riderName: String = event.rider?.let { "[${it.timeTrialData.index}] ${it.riderData.firstName} ${it.riderData.lastName}" }?: "Null"

    val displayString: String = when(event.eventType){
        TimelineEventType.RIDER_STARTED -> "$riderName Started"
        TimelineEventType.RIDER_FINISHED -> "$riderName Finished"
        TimelineEventType.RIDER_PASSED ->
        {
            if(event.rider != null){
                "$riderName Has Passed"
            }else{
                "Assign Rider"
            }
        }
    }
}