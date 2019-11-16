package com.android.jared.linden.timingtrials.data

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.android.jared.linden.timingtrials.data.source.Converters
import com.android.jared.linden.timingtrials.domain.ITimelineEvent

@Entity(tableName = "timetrial_event_table",
        indices = [Index("timeTrialId")],
        foreignKeys = [ForeignKey(entity =TimeTrialHeader::class, parentColumns = arrayOf("id"), childColumns = arrayOf("timeTrialId"), onDelete = CASCADE, deferred = false)])
data class RiderPassedEvent(val timeTrialId: Long,
                            val riderId: Long?,
                            val timeStamp: Long,
                            @PrimaryKey(autoGenerate = true) val id: Long? = null)
