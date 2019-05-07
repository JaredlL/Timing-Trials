package com.android.jared.linden.timingtrials.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_table")
data class Course(
        var courseName: String,
        var length: Double = 0.0,
        var cttName: String = "",
        var courseRecords: List<CourseRecord> = listOf(),
        @PrimaryKey(autoGenerate = true) var id: Long? = null
) {
    companion object {
        fun createBlank() = Course("", 0.0, "")
    }
}