package com.android.jared.linden.timingtrials.domain.csv

import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.Gender
import com.android.jared.linden.timingtrials.data.TimeTrialHeader

data class ImportResult(val result: Boolean, val message:String, val addedRiders: Int, val duplicateRiders: Int)

data class ImportTimeTrial(var header: TimeTrialHeader? = null, var course: Course? = null, val importRiderList: MutableList<ImportRider> = mutableListOf())

data class ImportRider(val firstName:String, val lastName:String?, val club: String?, val category:String?, val gender: Gender = Gender.UNKNOWN, val finishTime: Long, val splits: List<Long>, val notes:String?){
    companion object{
        fun createBlank():ImportRider{
            return ImportRider("", "", "",null, Gender.UNKNOWN, 0L, listOf(), null)
        }
    }
}


interface ILineToObjectConverter<T>{
    fun isHeading(line:String): Boolean
    fun setHeading(headingLine: String)
    fun importLine(dataLine: String): T?
}



abstract class StringToObjectField<T>
{
    abstract val fieldIndex: Int?

    fun applyFieldToObject(row: List<String>, target:T): T{
        val fi = fieldIndex
        if(fi !=null){
            row.getOrNull(fi)?.let {
                if(it.isNotBlank()){
                    return applyFieldFromString(it, target)
                }

            }
        }
        return target
    }

    abstract fun applyFieldFromString(valString: String, target: T):T
}
