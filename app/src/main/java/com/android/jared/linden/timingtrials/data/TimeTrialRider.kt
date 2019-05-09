package com.android.jared.linden.timingtrials.data

import androidx.room.*

@Entity(tableName = "timetrial_rider_table",
        indices = [Index("timeTrialId")],
        foreignKeys = [ForeignKey(entity =TimeTrialHeader::class, parentColumns = arrayOf("id"), childColumns = arrayOf("timeTrialId"), onDelete = ForeignKey.CASCADE, deferred = true)])
data class TimeTrialRider(@Embedded(prefix = "rider_") val rider: Rider,
                          val timeTrialId: Long? = null,
                          val number: Int? = null,
                          val startTime: Long,
                          val partOfTeam: Boolean = false,
                          val notes: String = "",
                          @PrimaryKey(autoGenerate = true) val id: Long? = null)



