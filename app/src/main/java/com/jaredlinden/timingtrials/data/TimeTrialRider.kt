package com.jaredlinden.timingtrials.data

import androidx.room.*
import org.threeten.bp.OffsetDateTime


@Entity(tableName = "timetrial_rider_table",
        indices = [Index("timeTrialId"), Index("riderId")],
        foreignKeys =
        [
            ForeignKey(
                entity = TimeTrialHeader::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("timeTrialId"),
                onDelete = ForeignKey.CASCADE,
                deferred = false),
            ForeignKey(
                entity = Rider::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("riderId"),
                onDelete = ForeignKey.CASCADE,
                deferred = false)
        ])
data class TimeTrialRider(val riderId: Long,
                          val timeTrialId: Long,
                          val courseId: Long?,
                          val index: Int,
                          val startTimeOffset: Int = 0,
                          val finishCode: Long? = null,
                          val splits: List<Long> = listOf(),
                          val assignedNumber: Int? = null,
                          val nextInTeam: Long = 0L,
                          val category: String = "",
                          val gender: Gender = Gender.UNKNOWN,
                          val club: String = "",
                          val notes: String = "",
                          @PrimaryKey(autoGenerate = true) val id: Long = 0L){

    fun hasNotDnfed():Boolean{
        return finishCode == null || finishCode >0
    }

    fun hasFinished():Boolean{
        return finishCode?.let { it>0 }?:false
    }

    fun finishTime():Long?{
        return if (finishCode != null && finishCode > 0){
            finishCode
        }else{
            null
        }
    }

    companion object {
        fun fromRiderAndTimeTrial(rider:Rider, ttId: Long): TimeTrialRider{
            return TimeTrialRider(
                rider.id,
                ttId,
                null,
                1,
                gender = rider.gender,
                category = rider.category,
                club = rider.club)
        }
    }
}

data class RiderIdStartTime(val riderId: Long, val startTime: OffsetDateTime)

data class TimeTrialRiderResult(
        @Embedded val timeTrialData: TimeTrialRider,

        @Relation(parentColumn = "riderId", entityColumn = "id", entity = Rider::class)
        val riderData: Rider,

        @Relation(parentColumn = "timeTrialId", entityColumn = "id", entity = TimeTrialHeader::class)
        val timeTrialHeader: TimeTrialHeader,

        @Relation(
            parentColumn = "courseId",
            entityColumn = "id")
        val resCourse: Course?):IResult {

    override val rider: Rider
        get() = riderData

    override val category: String
        get() = timeTrialData.category

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

    override val resultTime: Long?
        get() = timeTrialData.finishTime()
    override val splits: List<Long>
        get() = if( timeTrialData.splits.isNotEmpty()) listOf(timeTrialData.splits.first()) + timeTrialData.splits.zipWithNext{a,b -> b-a} else listOf()

    override val riderClub: String
        get() = timeTrialData.club

    override val timeTrial: TimeTrialHeader?
        get() = timeTrialHeader

}

data class FilledTimeTrialRider(
        @Embedded val timeTrialRiderData: TimeTrialRider,
        @Relation(parentColumn = "riderId", entityColumn = "id", entity = Rider::class)
        val riderData: Rider)
{
    fun updateTimeTrialData(newTimeTrialData: TimeTrialRider): FilledTimeTrialRider{
        return this.copy(timeTrialRiderData = newTimeTrialData)
    }

    fun riderId(): Long? = riderData.id

    companion object{
        fun createFromRiderAndTimeTrialAndNumber(rider: Rider, timeTrial: TimeTrial, number: Int?): FilledTimeTrialRider{
            if(timeTrial.riderList.mapNotNull { it.riderId() }.contains(rider.id)){
                throw Exception("Rider already in this time trial")
            }

            val ttRider = TimeTrialRider(riderId = rider.id,
                    timeTrialId = timeTrial.timeTrialHeader.id,
                    courseId = timeTrial.course?.id,
                    assignedNumber = number,
                    index = timeTrial.riderList.size,
                    category = rider.category,
                    gender = rider.gender,
                    club = rider.club)
            return FilledTimeTrialRider(timeTrialRiderData = ttRider, riderData = rider)

        }

        fun createFromRiderAndTimeTrial(rider: Rider, timeTrial: TimeTrial): FilledTimeTrialRider{
            if(timeTrial.riderList.mapNotNull { it.riderId() }.contains(rider.id)){
                throw Exception("Rider already in this time trial")
            }
            else{
                val ttRider = TimeTrialRider(riderId = rider.id,
                        timeTrialId = timeTrial.timeTrialHeader.id,
                        courseId = timeTrial.course?.id,
                        index = timeTrial.riderList.size,
                        category = rider.category,
                        gender = rider.gender,
                        club = rider.club)
                return FilledTimeTrialRider(timeTrialRiderData = ttRider, riderData = rider)
            }
        }
    }
}






