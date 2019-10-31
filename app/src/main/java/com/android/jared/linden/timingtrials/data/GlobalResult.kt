package com.android.jared.linden.timingtrials.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime


@Entity(tableName = "globalresult_table",
        indices = [Index("riderId"),Index("courseId")],
        foreignKeys = [
            ForeignKey(entity = Rider::class, parentColumns = arrayOf("id"), childColumns = arrayOf("riderId")),
            ForeignKey(entity = Course::class, parentColumns = arrayOf("id"), childColumns = arrayOf("courseId"))
        ])

data class GlobalResult(val riderId: Long,
                        val courseId: Long,
                        val riderClub: String,
                        val laps: Int,
                        val millisTime: Long,
                        val dateSet: OffsetDateTime?,
                        val timeTrialId: Long?,
                        val notes:String,
                        @PrimaryKey(autoGenerate = true) val resultId: Long? = null)