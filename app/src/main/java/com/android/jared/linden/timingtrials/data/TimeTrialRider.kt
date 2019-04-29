package com.android.jared.linden.timingtrials.data

import androidx.room.*
import org.threeten.bp.Instant

@Entity(tableName = "timetrial_rider_table", indices = [Index("timeTrialId")])
data class TimeTrialRider(@Embedded val rider: Rider,
                          val timeTrialId: Long,
                          val number: Int,
                          val startTime: Instant,
                          @PrimaryKey(autoGenerate = true)val id: Long)

data class TimeTrialWithRiders(
        @Embedded val timeTrial:TimeTrial,
        @Relation(parentColumn = "id",
                entityColumn = "timeTrialId",
                entity = TimeTrialRider::class
        ) val riderList: List<TimeTrialRider>

)

data class TimeTrialWithEventsAndRiders(
        @Embedded val timeTrial: TimeTrial,
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialRider::class)
        var riderList: List<TimeTrialRider>,
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialEvent::class)
        var eventList: List<TimeTrialEvent> = listOf()
)
