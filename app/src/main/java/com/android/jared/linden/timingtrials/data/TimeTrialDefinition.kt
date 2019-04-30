package com.android.jared.linden.timingtrials.data

import androidx.room.*
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit


//Todo: Use proper relational mapping, for now we simply store as JSON

@Entity(tableName = "timetrial_table", indices = [Index("id")])
data class TimeTrialDefinition(var ttName: String,
                               var course: Course? = null,
                               var laps: Int = 1,
                               var interval:Int = 60,
                               var startTime: Instant,
                               var isSetup: Boolean = false,
                               var isFinished: Boolean = false,
                               @PrimaryKey(autoGenerate = true) var id: Long? = null) {

    companion object {
        fun createBlank(): TimeTrialDefinition {
            val instant = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(15, ChronoUnit.MINUTES)
            return TimeTrialDefinition(ttName = "", course = null, laps = 1, startTime = instant, isSetup = false, isFinished = false)
        }


    }

}


data class TimeTrial(
        @Embedded val timeTrialDefinition: TimeTrialDefinition,
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialRider::class)
        var riderList: List<TimeTrialRider> = listOf(),
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialEvent::class)
        var eventList: List<TimeTrialEvent> = listOf()
){
    companion object {
        fun createBlank(): TimeTrial {
            return TimeTrial(TimeTrialDefinition.createBlank())
        }
    }
}



