package com.android.jared.linden.timingtrials.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit
import java.time.temporal.TemporalAmount
import java.util.*

@Entity(tableName = "timetrial_table")
data class TimeTrial(var ttName: String,
                     var course: Course? = null,
                     var riders:List<Rider> = listOf(),
                     var laps: Int = 1,
                     var interval:Duration = Duration.ofSeconds(60L),
                     var startTime: Instant,
                     var isSetup: Boolean = false,
                     var isFinished: Boolean = false,
                     @PrimaryKey(autoGenerate = true) var id: Long? = null) {

    companion object {

        fun createBlank(): TimeTrial {
            val instant = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(15, ChronoUnit.MINUTES)
            return TimeTrial(ttName = "", course = null, riders = listOf(), laps = 1, startTime = instant, isSetup = false, isFinished = false)
        }


    }

}

//Todo: Use proper relational mapping, for now we simply store as JSON

@Entity(tableName = "timetrial_rider_table")
data class TimeTrialRider(@PrimaryKey(autoGenerate = true)val id: Long,
                          val riderId: Long,
                          val timeTrialId: Long)

data class TimeTrialWithRiders(
        @Embedded val timeTrial:TimeTrial,
        @Relation(parentColumn = "id",
                entityColumn = "timeTrialId",
                entity = TimeTrialRider::class
        ) val riderIdList: List<Long>

)