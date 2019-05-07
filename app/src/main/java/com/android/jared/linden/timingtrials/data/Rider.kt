package com.android.jared.linden.timingtrials.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.jared.linden.timingtrials.domain.Gender
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "rider_table")
data class Rider(
        var firstName: String,
        var lastName: String,
        var club: String = "",
        var dateOfBirth: OffsetDateTime,
        var gender: Gender,
        var personalBests: List<PersonalBest> = listOf(),
        @PrimaryKey(autoGenerate = true) var id: Long? = null
)  {


    companion object {
        fun createBlank() = Rider("", "", "", OffsetDateTime.now().minusYears(20), Gender.UNKNOWN)
    }
    //val fullName = "$firstName $lastName"
}