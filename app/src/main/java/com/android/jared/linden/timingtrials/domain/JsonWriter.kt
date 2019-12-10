package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.IResult
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.google.gson.Gson
import java.io.OutputStream
import java.io.PrintWriter

class JsonResultsWriter{

    fun writeToPath(filePath: OutputStream, timeTrial: TimeTrial){
        val writer = PrintWriter(filePath)

        val output = TimeTrialResultOutput(timeTrial)
        Gson().toJson(output, writer)
        writer.flush()
        writer.close()
    }

}

data class TimeTrialResultOutput(@Transient val timeTrial: TimeTrial){
    val header = timeTrial.timeTrialHeader
    val results = timeTrial.helper.results3.map { ResultOutput(it) }
}

data class ResultOutput(@Transient val result: IResult)
{
    val rider = result.rider
    val category = result.category
    val resultTime = result.resultTime
    val splits = result.splits
    val notes = result.notes

}