package com.android.jared.linden.timingtrials.data

import androidx.room.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime


@Entity(tableName = "timetrial_rider_table",
        indices = [Index("timeTrialId")],
        foreignKeys = [ForeignKey(entity =TimeTrialHeader::class, parentColumns = arrayOf("id"), childColumns = arrayOf("timeTrialId"), onDelete = ForeignKey.CASCADE, deferred = false)])
data class TimeTrialRider(@Embedded(prefix = "rider_") val rider: Rider,
                          val timeTrialId: Long,
                          val number: Int,
                          val startTimeOffset: Int = 0,
                          val partOfTeam: Boolean = false,
                          val notes: String = "",
                          @PrimaryKey(autoGenerate = true) val id: Long? = null){

    fun getCategory(time: OffsetDateTime): RiderCategoryStandard{
        return RiderCategoryStandard(rider.gender, time.year - rider.dateOfBirth.year)
    }
}



