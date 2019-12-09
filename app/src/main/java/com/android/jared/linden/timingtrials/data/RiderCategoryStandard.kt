package com.android.jared.linden.timingtrials.data

import org.threeten.bp.LocalDate



interface ICategoryFactory{
    fun getCategory(gender: Gender ,ageInYears: Int?): String

}

object StandardCategoryGetter : ICategoryFactory{
    override fun getCategory(gender: Gender, ageInYears: Int?): String {
        val ageString = when(ageInYears){
            null->""
            in 0..16 -> "Juv"
            in 17..18 -> "Jun"
            in 19..39 -> "S"
            else -> { "V${(ageInYears) / 10}0" }

        }
        return ageString + gender.gendarString()
    }
}


data class RiderCategoryStandard (val gender: Gender, val ageInYears: Int){


    fun categoryId() = getAgeString() + gender.gendarString()

    fun readableName(): String {return categoryId()}

    private fun getAgeString(): String{

        return when(ageInYears){
            in 0..16 -> "Juv"
            in 17..18 -> "Jun"
            in 19..39 -> "S"
            else -> {
                return "V${(ageInYears) / 10}0"
            }

        }
    }
}

data class TeamCategoryStandard(val riderCategories: List<RiderCategoryStandard>)