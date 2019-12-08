package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.timetrialresults.ResultRowViewModel
import java.io.*

class CsvTransfer (val timeTrial: TimeTrial, val results: List<ResultRowViewModel>){


    fun writeToPath(filePath: OutputStream){
        val writer = PrintWriter(filePath)
        writer.appendln("Powered by Timing Trials")
        writer.appendln("Results for ${timeTrial.timeTrialHeader.ttName}")
        for(r in results) writeRow(r, writer)
        writer.flush()
        writer.close()
    }

    fun writeRow(row: ResultRowViewModel, writer: PrintWriter){
        for (v in row.row.dropLast(1)){
            writer.append((v.content.value) + ",")
        }
        writer.appendln(row.row.last().content.value)
    }

}