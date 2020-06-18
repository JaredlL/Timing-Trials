package com.jaredlinden.timingtrials.data

import androidx.room.*
import com.jaredlinden.timingtrials.domain.TimeTrialHelper
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit
import java.util.*


@Entity(tableName = "timetrial_table", indices = [Index("courseId")],
        foreignKeys = [ForeignKey(entity =Course::class, parentColumns = arrayOf("id"), childColumns = arrayOf("courseId"), onDelete = ForeignKey.CASCADE, deferred = false)])
data class TimeTrialHeader(val ttName: String,
                           val courseId: Long? = null,
                           val laps: Int = 1,
                           val interval:Int = 60,
                           val startTime: OffsetDateTime,
                           val firstRiderStartOffset: Int = 60,
                           val status: TimeTrialStatus = TimeTrialStatus.SETTING_UP,
                           val numberRules: NumberRules = NumberRules(),
                           val timeStamps: List<Long> = listOf(),
                           val description: String = "",
                           val guid:String = UUID.randomUUID().toString(),
                           @PrimaryKey(autoGenerate = true) override val id: Long? = null) : ITimingTrialsEntity {


    @delegate: Ignore
    @delegate: Transient
    val startTimeMilis: Long by lazy { startTime.toInstant().toEpochMilli() }

    companion object {
        fun createBlank(): TimeTrialHeader {
            val instant = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(15, ChronoUnit.MINUTES)
            return TimeTrialHeader(ttName = "", courseId = null, startTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()))
        }


    }

}

data class TimeTrialBasicInfo(
        val courseId: Long? = null,
        val laps: Int = 1,
        val interval:Int = 60,
        val id: Long? = null
)

data class TimeTrialWithCourse(
    @Embedded val timeTrialHeader: TimeTrialHeader,

    @Relation(parentColumn = "courseId", entityColumn = "id", entity = Course::class)
    val course: Course? = null
){

    fun updateCourse(newCourse: Course): TimeTrialWithCourse{
        return this.copy(course = newCourse, timeTrialHeader = this.timeTrialHeader.copy(courseId = newCourse.id))
    }
}

data class TimeTrial(
        @Embedded val timeTrialHeader: TimeTrialHeader,

        @Relation(parentColumn = "courseId", entityColumn = "id", entity = Course::class)
        val course: Course? = null,

        @Relation(parentColumn = "id", entityColumn = "timeTrialId", entity = TimeTrialRider::class)
        val riderList: List<FilledTimeTrialRider> = listOf()
) {



    fun updateCourse(newCourse: Course):TimeTrial{
        val newHeader = this.timeTrialHeader.copy(courseId = newCourse.id)
        return this.copy(course = newCourse, timeTrialHeader = newHeader, riderList = this.riderList.map { it.copy(timeTrialData = it.timeTrialData.copy(courseId = newCourse.id)) })
    }

    fun updateHeader(newTimeTrialHeader: TimeTrialHeader): TimeTrial{
        return this.copy(timeTrialHeader = newTimeTrialHeader)
    }

    fun updateRiderList(newRiderList: List<FilledTimeTrialRider>): TimeTrial{
        val ensureCorrect = newRiderList.mapIndexed {i,r-> r.updateTimeTrialData( r.timeTrialData.copy(timeTrialId = this.timeTrialHeader.id, courseId = this.course?.id, index = i)) }
        return  this.copy(riderList = ensureCorrect)
    }

    fun addRider(newRider: Rider): TimeTrial{
        val newTtRider = FilledTimeTrialRider.createFromRiderAndTimeTrial(newRider, this)
        return this.updateRiderList(this.riderList + newTtRider )
    }

    fun addRiders(newRiders: List<Rider>): TimeTrial{
       return updateRiderList(newRiders.map { FilledTimeTrialRider.createFromRiderAndTimeTrial(it,this) })
    }

    fun removeRider(riderToRemove: Rider): TimeTrial{
        return this.updateRiderList(this.riderList.filterNot { it.riderData.id == riderToRemove.id })
    }

    fun getRiderNumber(index: Int): Int{
        return timeTrialHeader.numberRules.numberFromIndex(index, riderList.size)
    }



    @Ignore @Transient
     val helper = TimeTrialHelper(this)

    fun equalsOtherExcludingIds(other: TimeTrial?): Boolean{
        if(other == null) return false
        if(timeTrialHeader.copy(id = null) != other.timeTrialHeader.copy(id = null)) return false
        if(riderList.size != other.riderList.size) return false
        return riderList == other.riderList
    }

    companion object {
        fun createBlank(): TimeTrial {
            return TimeTrial(TimeTrialHeader.createBlank())
        }
    }
}



