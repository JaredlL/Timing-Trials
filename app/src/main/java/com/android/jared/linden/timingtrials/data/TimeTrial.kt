package com.android.jared.linden.timingtrials.data

import androidx.room.*
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit
import java.time.temporal.TemporalAmount
import java.util.*


//Todo: Use proper relational mapping, for now we simply store as JSON

@Entity(tableName = "timetrial_table", indices = [Index("id")])
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



