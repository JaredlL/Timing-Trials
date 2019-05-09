package com.android.jared.linden.timingtrials.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_table")
data class Course(
        val courseName: String,
        val length: Double = 0.0,
        val cttName: String = "",
        val courseRecords: List<CourseRecord> = listOf(),
        @PrimaryKey(autoGenerate = true) val id: Long? = null
) {
    companion object {
        fun createBlank() = Course("", 0.0, "")
    }
}