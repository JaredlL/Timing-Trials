package com.android.jared.linden.timingtrials.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

enum class Gender{
    UNKNOWN{
        override fun gendarString(): String { return ""}
    },
    MALE{
        override fun gendarString(): String { return "M"}
    },
    FEMALE{
        override fun gendarString(): String { return "F"}
    },
    OTHER{
        override fun gendarString(): String { return "O"}
    };

    abstract fun gendarString(): String

}

data class RiderCategoryStandard (val gender: Gender, val yob: Int){


    fun categoryId() = getAgeString() + gender.gendarString()

    private fun getAgeString(): String{
        val curentYear = LocalDate.now().year

        return when(curentYear - yob){
            in 0..16 -> "Juvenile"
            in 17..18 -> "Junior"
            in 19..39 -> "Sen"
            else -> {
                return "V + ${(curentYear - yob) / 10} + 0"
            }

        }
    }
}

data class TeamCategoryStandard(val riderCategories: List<RiderCategoryStandard>)