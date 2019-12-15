package com.android.jared.linden.timingtrials.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "course_table")
data class Course(
        val courseName: String,
        val length: Double = 0.0,
        val cttName: String = "",
        @PrimaryKey(autoGenerate = true) override val id: Long? = null
) : ITimingTrialsEntity {
   // fun toCourseLight(): CourseLight{
   //     return CourseLight(courseName, length,cttName,id)
   // }
    companion object {
        fun createBlank() = Course("", 0.0, "")
    }
}

//data class CourseLight(
//        @ColumnInfo(name = "courseName") val courseName: String,
//        @ColumnInfo(name = "length") val length: Double = 0.0,
//        @ColumnInfo(name = "cttName") val cttName: String = "",
//        @ColumnInfo(name = "id") val id: Long? = null
//)

