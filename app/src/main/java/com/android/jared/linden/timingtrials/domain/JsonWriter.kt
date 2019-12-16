package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.IResult
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.OutputStream
import java.io.PrintWriter

class JsonResultsWriter{

    fun writeToPath(filePath: OutputStream, timeTrial: TimeTrial){

        val exclusionStrategy = object : ExclusionStrategy{
            override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                return false
            }

            override fun shouldSkipField(f: FieldAttributes?): Boolean {
                return f?.name == "id" || f?.name == "courseId"
            }

        }

        val writer = PrintWriter(filePath)

        val output = TimeTrialResultOutput(timeTrial)
        GsonBuilder().addSerializationExclusionStrategy(exclusionStrategy).create().toJson(output,writer)
        writer.flush()
        writer.close()
    }

}

data class TimeTrialResultOutput(@Transient val timeTrial: TimeTrial){
    val timeTrialHeader = timeTrial.timeTrialHeader
    val course = timeTrial.course
    val results = timeTrial.helper.results.map { ResultOutput(it) }
}


data class ResultOutput(@Transient val result: IResult)
{
    val firstName = result.rider.firstName
    val lastName = result.rider.lastName
    val club = result.riderClub
    val category = result.category
    val gendar = result.rider.gender
    val finishTime = result.resultTime
    val splits = result.splits
    val notes = result.notes

}