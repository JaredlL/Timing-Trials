package com.android.jared.linden.timingtrials.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.*

@Entity(tableName = "timetrial_table")
data class TimeTrial(var ttName: String,
                     var course: Course? = null,
                     var riders:List<Rider> = listOf(),
                     var laps: Int = 1,
                     var interval:Int = 60,
                     var startTime: Date,
                     var isSetup: Boolean = false,
                     var isFinished: Boolean = false,
                     @PrimaryKey(autoGenerate = true) var id: Long? = null) {

    companion object {

        fun createBlank(): TimeTrial {
            val c = Calendar.getInstance()
            c.add(Calendar.MINUTE, 10)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)

            return TimeTrial(ttName = "", course = null, riders = listOf(), laps = 1, interval = 60, startTime = c.time, isSetup = false, isFinished = false)
        }
    }

}

//Todo: Use proper relational mapping, for now we siply store as JSON

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