package com.android.jared.linden.timingtrials.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "rider_table") @Parcelize
data class Rider(
        var firstName: String,
        var lastName: String,
        var club: String = "",
        var age: Int,
        var gender: String = "Male",
        @PrimaryKey(autoGenerate = true) var id: Long? = null
) : Parcelable {


    companion object {
        fun createBlank() = Rider("", "", "", 0)
    }
    //val fullName = "$firstName $lastName"
}