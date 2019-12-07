package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.timetrialresults.ResultRowViewModel
import java.io.File
import java.io.FileWriter

class CsvTransfer (val timeTrial: TimeTrial, val results: List<ResultRowViewModel>){


    fun writeToPath(filePath: File){
        val writer = FileWriter(filePath)
        writer.append("Powered by Timing Trials")
        writer.appendln(timeTrial.timeTrialHeader.ttName)
        writer.flush()
        writer.close()
    }

}