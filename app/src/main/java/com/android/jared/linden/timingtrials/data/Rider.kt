package com.android.jared.linden.timingtrials.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

@Entity(tableName = "rider_table") @Parcelize
data class Rider(
        var firstName: String,
        var lastName: String,
        var club: String = "",
        var dateOfBirth: Instant,
        var gender: String = "Male",
        var pbString: String = "",
        @PrimaryKey(autoGenerate = true) var id: Long? = null
) : Parcelable {


    companion object {
        fun createBlank() = Rider("", "", "", Instant.now().truncatedTo(ChronoUnit.YEARS))
    }
    //val fullName = "$firstName $lastName"
}