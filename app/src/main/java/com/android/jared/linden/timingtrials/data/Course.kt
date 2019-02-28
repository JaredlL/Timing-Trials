package com.android.jared.linden.timingtrials.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "course_table") @Parcelize
data class Course(
       var name: String,
       var length: Double = 0.0,
       var cttname: String = "",
       @PrimaryKey(autoGenerate = true) var Id: Long? = null
) : Parcelable