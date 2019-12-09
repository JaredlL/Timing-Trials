package com.android.jared.linden.timingtrials.domain.csv

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.timetrialresults.ResultRowViewModel
import java.io.*

class CsvResultWriter (val timeTrial: TimeTrial, val results: List<ResultRowViewModel>){


    fun writeToPath(filePath: OutputStream){
        val writer = PrintWriter(filePath)
        writer.appendln(surroundQuotes("Powered by Timing Trials"))
        writer.appendln(surroundQuotes("Results for ${timeTrial.timeTrialHeader.ttName}"))
        for(r in results) writeRow(r, writer)
        writer.flush()
        writer.close()
    }

    fun writeRow(row: ResultRowViewModel, writer: PrintWriter){
        for (v in row.row.dropLast(1)){
            writer.append(surroundQuotes(v.content.value))
        }
        writer.appendln(surroundQuotes(row.row.last().content.value))
    }

    private fun surroundQuotes(string: String?): String{
        return "\"$string\""
    }

}