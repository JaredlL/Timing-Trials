package com.android.jared.linden.timingtrials.data

import androidx.room.*
import com.android.jared.linden.timingtrials.domain.RiderAssignmentResult
import com.android.jared.linden.timingtrials.domain.TimeTrialHelper
import com.android.jared.linden.timingtrials.ui.RiderStatus
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit
import java.util.*


@Entity(tableName = "timetrial_table", indices = [Index("id")])
data class TimeTrialHeader(val ttName: String,
                           val course: Course? = null,
                           val laps: Int = 1,
                           val interval:Int = 60,
                           val startTime: OffsetDateTime,
                           val firstRiderStartOffset: Int = 60,
                           val isSetup: Boolean = false,
                           val isFinished: Boolean = false,
                           @PrimaryKey(autoGenerate = true) val id: Long? = null) {

    companion object {
        fun createBlank(): TimeTrialHeader {
            val instant = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(15, ChronoUnit.MINUTES)
            return TimeTrialHeader(ttName = "", course = null, laps = 1, startTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()), isSetup = false, isFinished = false)
        }


    }

}


interface ITimeTrial {
    val timeTrialHeader: TimeTrialHeader
    val riderList: List<TimeTrialRider>
    val eventList: List<TimeTrialEvent>

}

data class TimeTrial(
        @Embedded val timeTrialHeader: TimeTrialHeader,
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialRider::class)
        val riderList: List<TimeTrialRider> = listOf(),
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialEvent::class)
        val eventList: List<TimeTrialEvent> = listOf()
) {


    @Ignore
    val helper = TimeTrialHelper(this)

    companion object {
        fun createBlank(): TimeTrial {
            return TimeTrial(TimeTrialHeader.createBlank())
        }
    }
}



