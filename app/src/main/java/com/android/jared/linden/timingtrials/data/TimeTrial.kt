package com.android.jared.linden.timingtrials.data

import androidx.room.*
import com.android.jared.linden.timingtrials.domain.TimeTrialHelper
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit


@Entity(tableName = "timetrial_table", indices = [Index("id")])
data class TimeTrialHeader(val ttName: String,
                           val course: CourseLight? = null,
                           val laps: Int = 1,
                           val interval:Int = 60,
                           val startTime: OffsetDateTime,
                           val firstRiderStartOffset: Int = 60,
                           val status: TimeTrialStatus = TimeTrialStatus.SETTING_UP,
                           @PrimaryKey(autoGenerate = true) val id: Long? = null) {

    companion object {
        fun createBlank(): TimeTrialHeader {
            val instant = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(15, ChronoUnit.MINUTES)
            return TimeTrialHeader(ttName = "", course = null, startTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()))
        }


    }

}

data class TimeTrial(
        @Embedded val timeTrialHeader: TimeTrialHeader,
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialRider::class)
        val riderList: List<TimeTrialRider> = listOf(),
        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = RiderPassedEvent::class)
        val eventList: List<RiderPassedEvent> = listOf()
) {


    @Ignore
    val helper = TimeTrialHelper(this)


    companion object {
        fun createBlank(): TimeTrial {
            return TimeTrial(TimeTrialHeader.createBlank())
        }
    }
}



