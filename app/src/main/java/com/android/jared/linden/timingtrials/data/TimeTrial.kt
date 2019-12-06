package com.android.jared.linden.timingtrials.data

import androidx.room.*
import com.android.jared.linden.timingtrials.domain.TimeTrialHelper
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit


@Entity(tableName = "timetrial_table", indices = [Index("id")])
data class TimeTrialHeader(val ttName: String,
                           val course: Course? = null,
                           val laps: Int = 1,
                           val interval:Int = 60,
                           val startTime: OffsetDateTime,
                           val firstRiderStartOffset: Int = 60,
                           val status: TimeTrialStatus = TimeTrialStatus.SETTING_UP,
                           @PrimaryKey(autoGenerate = true) override val id: Long? = null) : ITimingTrialsEntity {


    @delegate: Ignore
    val startTimeMilis: Long by lazy { startTime.toInstant().toEpochMilli() }

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

    fun equalsOtherExcludingIds(other: TimeTrial?): Boolean{
        if(other == null) return false
        if(timeTrialHeader.copy(id = null) != other.timeTrialHeader.copy(id = null)) return false
        if(riderList.size != other.riderList.size) return false
        if(riderList.asSequence().map { it.copy(id = null) }.zip(other.riderList.asSequence().map { it.copy(id = null) }).asSequence().any{ (a,b) -> a!=b }) return false
        if(eventList.size != other.eventList.size) return false
        if(eventList.asSequence().map { it.copy(id = null) }.zip(other.eventList.asSequence().map { it.copy(id = null) }).asSequence().any{ (a,b) -> a!=b }) return false
        return true

    }

    companion object {
        fun createBlank(): TimeTrial {
            return TimeTrial(TimeTrialHeader.createBlank())
        }
    }
}



