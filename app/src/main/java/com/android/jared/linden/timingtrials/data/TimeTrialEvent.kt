package com.android.jared.linden.timingtrials.data

import androidx.room.*
import com.android.jared.linden.timingtrials.data.source.Converters

@Entity(tableName = "timetrialevent_table")
data class TimeTrialEvent(var timeTrialId: Long,
                          var rider: Rider?,
                          var timeStamp: Long,
                          @TypeConverters(Converters::class) var eventType: EventType,
                          @PrimaryKey(autoGenerate = true) var id: Long? = null)

data class TimeTrialWithEvents(@Embedded var
                               timeTrial: TimeTrial,
                               @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialEvent::class)
                               var list: List<TimeTrialEvent> = listOf())

enum class EventType(val type: Int){
    EMPTY(0),
    TIMETRIAL_STARTED(1),
    RIDER_STARTED(2),
    RIDER_LAPPED(3),
    RIDER_FINISHED(4);

    companion object {
        private val map = EventType.values().associateBy(EventType::type)
        fun fromInt(type: Int) = map[type]
    }

}