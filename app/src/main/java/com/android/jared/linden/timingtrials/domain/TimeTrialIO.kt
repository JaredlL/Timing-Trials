package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.*


data class ImportAttempt<T>(val sucess: Boolean, val data: T, val message: String?)

data class TimingTrialsExport(val timingTrialsData: List<TimeTrialIO>)

data class TimeTrialIO(var timeTrialHeader: TimeTrialHeader? = null, var course: Course? = null, val results: MutableList<RiderResultIO> = mutableListOf()){
    constructor(timeTrial: TimeTrial) : this(
            timeTrial.timeTrialHeader,
            timeTrial.course,
            timeTrial.helper.results.map { RiderResultIO(it) }.toMutableList()
    )
}

data class RiderResultIO(
        val firstName:String = "",
        val lastName:String = "",
        val club: String = "",
        val category:String = "",
        val gender: Gender = Gender.UNKNOWN,
        val finishTime: Long = 0L,
        val splits: List<Long> = listOf(),
        val notes:String = "") {

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