package com.android.jared.linden.timingtrials.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime


@Entity(tableName = "rider_table")
data class Rider(
        val firstName: String,
        val lastName: String,
        val club: String = "",
        val dateOfBirth: OffsetDateTime,
        val gender: Gender,
        val personalBests: List<PersonalBest> = listOf(),
        @PrimaryKey(autoGenerate = true) val id: Long? = null
)  {


    companion object {

        fun createBlank() = Rider("", "", "", OffsetDateTime.now().minusYears(20), Gender.UNKNOWN)
    }
    //val fullName = "$firstName $lastName"
}


