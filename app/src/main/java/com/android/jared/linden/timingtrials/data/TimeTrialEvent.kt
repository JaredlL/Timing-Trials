package com.android.jared.linden.timingtrials.data

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.android.jared.linden.timingtrials.data.source.Converters

@Entity(tableName = "timetrial_event_table",
        indices = [Index("timeTrialId")],
        foreignKeys = [ForeignKey(entity =TimeTrialHeader::class, parentColumns = arrayOf("id"), childColumns = arrayOf("timeTrialId"), onDelete = CASCADE, deferred = true)])
data class TimeTrialEvent(var timeTrialId: Long,
                          var riderId: Long?,
                          var timeStamp: Long,
                          @TypeConverters(Converters::class) var eventType: EventType,
                          @PrimaryKey(autoGenerate = true) var id: Long? = null)


//data class TimeTrialWithEvents(@Embedded var timeTrialHeader: TimeTrialHeader,
//                               @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialEvent::class)
//                               var eventList: List<TimeTrialEvent> = listOf())


enum class EventType(val type: Int){
    EMPTY(0),
    TIMETRIAL_STARTED(1),
    RIDER_STARTED(2),
    RIDER_PASSED(3),
    RIDER_FINISHED(4);

    companion object {
        private val map = values().associateBy(EventType::type)
        fun fromInt(type: Int) = map[type]
    }

}