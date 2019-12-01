package com.android.jared.linden.timingtrials.data

import androidx.room.ColumnInfo
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
        @PrimaryKey(autoGenerate = true) override val id: Long? = null
) : ITimingTrialsEntity  {



    fun fullName(): String{
        return "$firstName $lastName"
    }

    fun getCategoryStandard(): RiderCategoryStandard{
        return RiderCategoryStandard(gender, OffsetDateTime.now().year - dateOfBirth.year )
    }

    companion object {

        fun createBlank() = Rider("", "", "", OffsetDateTime.now().minusYears(20), Gender.UNKNOWN)

    }
}



