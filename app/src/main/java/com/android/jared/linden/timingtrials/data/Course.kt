package com.android.jared.linden.timingtrials.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "course_table") @Parcelize
data class Course(
        var courseName: String,
        var length: Double = 0.0,
        var cttName: String = "",
        @PrimaryKey(autoGenerate = true) var id: Long? = null
) : Parcelable{
    companion object {
        fun createBlank() = Course("", 0.0, "")
    }
}