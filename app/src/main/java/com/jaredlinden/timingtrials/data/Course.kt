package com.jaredlinden.timingtrials.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "course_table")
data class Course(
        val courseName: String,
        val length: Double? = null,
        val cttName: String = "",
        @PrimaryKey(autoGenerate = true) override val id: Long? = null
) : ITimingTrialsEntity {

    companion object {
        fun createBlank() = Course("", 0.0, "")
    }
}


