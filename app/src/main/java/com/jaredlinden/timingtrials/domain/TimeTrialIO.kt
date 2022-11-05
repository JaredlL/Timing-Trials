package com.jaredlinden.timingtrials.domain

import com.jaredlinden.timingtrials.data.*
import org.threeten.bp.LocalTime


data class ImportAttempt<T>(val sucess: Boolean, val data: T, val message: String?)

data class TimingTrialsExport(val timingTrialsData: List<TimeTrialIO>)

data class TimeTrialIO(var timeTrialHeader: TimeTrialHeader? = null, var course: Course? = null, val timeTrialRiders: MutableList<TimeTrialRiderIO> = mutableListOf()){
    constructor(timeTrial: TimeTrial) : this(
            timeTrial.timeTrialHeader,
            timeTrial.course,
            timeTrial.helper.results.map { TimeTrialRiderIO(it) }.toMutableList()
    )
}

data class TimeTrialRiderIO(
        val firstName:String = "",
        val lastName:String = "",
        val club: String = "",
        val category:String = "",
        val gender: Gender = Gender.UNKNOWN,
        val finishTime: Long? = null,
        val splits: List<Long> = listOf(),
        val notes:String = "",
        val bib: Int? = null,
        val startTime: LocalTime? = null) {

    constructor(result:IResult) : this(
            result.rider.firstName,
            result.rider.lastName,
            result.riderClub,
            result.category,
            result.gender,
            result.resultTime,
            result.splits,
            result.notes)

}


interface ILineToObjectConverter<T>{
    fun isHeading(line:String): Boolean
    fun setHeading(headingLine: String)
    fun importLine(dataLine: List<String>): T?
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