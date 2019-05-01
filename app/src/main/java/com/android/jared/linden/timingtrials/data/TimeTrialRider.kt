package com.android.jared.linden.timingtrials.data

import androidx.room.*

@Entity(tableName = "timetrial_rider_table",
        indices = [Index("timeTrialId")],
        foreignKeys = [ForeignKey(entity =TimeTrialDefinition::class, parentColumns = arrayOf("id"), childColumns = arrayOf("timeTrialId"), onDelete = ForeignKey.CASCADE, deferred = true)])
data class TimeTrialRider(@Embedded(prefix = "rider_") val rider: Rider,
                          var timeTrialId: Long?,
                          var number: Int? = null,
                          var startTime: Long? = null,
                          @PrimaryKey(autoGenerate = true) var id: Long? = null)



