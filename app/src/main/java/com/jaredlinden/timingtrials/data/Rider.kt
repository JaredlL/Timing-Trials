package com.jaredlinden.timingtrials.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate


@Entity(tableName = "rider_table")
data class Rider(
        val firstName: String,
        val lastName: String,
        val club: String = "",
        val dateOfBirth: LocalDate? = null,
        val category: String = "",
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



