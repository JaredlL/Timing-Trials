package com.android.jared.linden.timingtrials.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime


@Entity(tableName = "rider_table")
data class Rider(
        val firstName: String,
        val lastName: String,
        val club: String = "",
        val dateOfBirth: LocalDate? = null,
        val category: String? = null,
        val gender: Gender = Gender.UNKNOWN,
        @PrimaryKey(autoGenerate = true) override val id: Long? = null
) : ITimingTrialsEntity  {



    fun fullName(): String{
        return "$firstName $lastName"
    }

    companion object {

        fun createBlank() = Rider("", "")

    }
}



