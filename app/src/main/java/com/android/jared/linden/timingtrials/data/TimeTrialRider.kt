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
                          val category: String? = null,
                          val gender: Gender = Gender.UNKNOWN,
                          val club: String = "",
                          val notes: String = "",
                          @PrimaryKey(autoGenerate = true) val id: Long? = null){

}

data class RiderInfo(val firstName: String, val lastName: String, val gender: Gender)

data class FilledTimeTrialRider(
        @Embedded val timeTrialData: TimeTrialRider,

        @Relation(parentColumn = "riderId", entityColumn = "id", entity = Rider::class)
        val riderData: Rider
){
    fun updateTimeTrialData(newTimeTrialData: TimeTrialRider): FilledTimeTrialRider{
        return this.copy(timeTrialData = newTimeTrialData)
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






