package com.android.jared.linden.timingtrials.data

import androidx.room.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime


@Entity(tableName = "timetrial_rider_table",
        indices = [Index("timeTrialId"), Index("riderId")],
        foreignKeys = [ForeignKey(entity =TimeTrialHeader::class, parentColumns = arrayOf("id"), childColumns = arrayOf("timeTrialId"), onDelete = ForeignKey.CASCADE, deferred = false),
            ForeignKey(entity =Rider::class, parentColumns = arrayOf("id"), childColumns = arrayOf("riderId"), onDelete = ForeignKey.CASCADE, deferred = false)])
data class TimeTrialRider(val riderId: Long,
                          val timeTrialId: Long?,
                          val courseId: Long?,
                          val index: Int,
                          val number: Int,
                          val startTimeOffset: Int = 0,
                          val finishTime: Long? = null,
                          val splits: List<Long> = listOf(),
                          val partOfTeam: Boolean = false,
                          val category: String = "",
                          val gender: Gender = Gender.UNKNOWN,
                          val club: String = "",
                          val notes: String = "",
                          val resultNote: String? = null,
                          @PrimaryKey(autoGenerate = true) val id: Long? = null){

    fun hasNotDnfed():Boolean{
        return finishTime == null || finishTime >0
    }

}

data class RiderIdStartTime(val riderId: Long, val startTime: OffsetDateTime)

data class TimeTrialRiderResult(
        @Embedded val timeTrialData: TimeTrialRider,

        @Relation(parentColumn = "riderId", entityColumn = "id", entity = Rider::class)
        val riderData: Rider,

        @Relation(parentColumn = "timeTrialId", entityColumn = "id", entity = TimeTrialHeader::class)
        val timeTrialHeader: TimeTrialHeader,

        @Relation(parentColumn = "courseId", entityColumn = "id", entity = Course::class)
        val resCourse: Course?):IResult {

    override val rider: Rider
        get() = riderData

    override val category: String
        get() = timeTrialData.category?:""

    override val course: Course
        get() = resCourse?:Course.createBlank()

    override val dateSet: OffsetDateTime?
        get() = timeTrialHeader.startTime

    override val gender: Gender
        get() = timeTrialData.gender

    override val laps: Int
        get() = timeTrialHeader.laps

    override val notes: String
        get() = timeTrialData.notes

    override val resultTime: Long
        get() = if(timeTrialData.finishTime == null){
            Long.MAX_VALUE
        }else{
            if(timeTrialData.finishTime < 0){
                Long.MAX_VALUE
            }else{
                timeTrialData.finishTime
            }
        }

    override val splits: List<Long>
        get() = if( timeTrialData.splits.isNotEmpty()) listOf(timeTrialData.splits.first()) + timeTrialData.splits.zipWithNext{a,b -> b-a} else listOf()

    override val riderClub: String
        get() = timeTrialData.club

    override val timeTrial: TimeTrialHeader?
        get() = timeTrialHeader

}

data class FilledTimeTrialRider(
        @Embedded val timeTrialData: TimeTrialRider,

        @Relation(parentColumn = "riderId", entityColumn = "id", entity = Rider::class)
        val riderData: Rider
){
    fun updateTimeTrialData(newTimeTrialData: TimeTrialRider): FilledTimeTrialRider{
        return this.copy(timeTrialData = newTimeTrialData)
    }

    fun addSplit(split: Long): FilledTimeTrialRider{
        return this.copy(timeTrialData = this.timeTrialData.copy(splits = this.timeTrialData.splits + split))
    }

    fun firstNameAndFirstLetterOfSecond():String{
        val lastL= riderData.lastName.first()
        return riderData.firstName
    }


    companion object{
        fun createFromRiderAndTimeTrial(rider: Rider, timeTrial: TimeTrial): FilledTimeTrialRider{
            if(rider.id != null){
                val ttRider = TimeTrialRider(riderId = rider.id,
                        timeTrialId = timeTrial.timeTrialHeader.id,
                        courseId = timeTrial.course?.id,
                        index = timeTrial.riderList.size,
                        number = timeTrial.riderList.size,
                        category = rider.category,
                        gender = rider.gender,
                        club = rider.club)
                return FilledTimeTrialRider(timeTrialData = ttRider, riderData = rider)
            }else{
                throw Exception("Rider ID is null")
            }


        }
    }

}






