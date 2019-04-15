package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.Rider
import java.util.*

enum class EventType(id: Int){
    RIDERSTARTED(2), RIDERPASSED(3)
}

abstract class TimetrialEvent{
   abstract val type: EventType
   abstract val timeStamp: Date
}

class UnasignedEvent(override val type: EventType, override val timeStamp: Date): TimetrialEvent()

class RiderEvent(override val type: EventType, override val timeStamp: Date, val rider: Rider): TimetrialEvent()