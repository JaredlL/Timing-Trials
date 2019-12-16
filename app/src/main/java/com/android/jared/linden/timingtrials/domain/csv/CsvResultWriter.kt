package com.android.jared.linden.timingtrials.domain.csv

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.timetrialresults.ResultRowViewModel
import java.io.*

class CsvResultWriter (val timeTrial: TimeTrial, val results: List<ResultRowViewModel>){


    fun writeToPath(filePath: OutputStream){
        val writer = PrintWriter(filePath)
        writer.appendln(surroundQuotes("Powered by Timing Trials"))
        writeTimeTrialRow(writer)
        writeCourseRow(writer)
        for(r in results.dropLast(1)) writeRowNewLine(r, writer)
        writeLastRow(results.last(), writer)
        writer.flush()
        writer.close()
    }

    private fun writeTimeTrialRow(writer: PrintWriter){
        writer.append(surroundQuotes("Results for ${timeTrial.timeTrialHeader.ttName},"))
        writer.appendln(surroundQuotes("On ${timeTrial.timeTrialHeader.startTime}"))
    }

    private fun writeCourseRow(writer: PrintWriter){
        writer.append(surroundQuotes("${timeTrial.timeTrialHeader.laps} laps,"))
        val course = timeTrial.course
        if(course != null){
            writer.append(surroundQuotes("${course.courseName} ${course.cttName},"))
            writer.appendln(surroundQuotes("${course.length/1000L} km"))
        }else{
            writer.appendln(surroundQuotes("Unknown Course"))
        }

    }

    private fun writeRowNewLine(row: ResultRowViewModel, writer: PrintWriter){
        for (v in row.row.dropLast(1)){
            writer.append("${surroundQuotes(v.content.value)},")
        }
        writer.appendln(surroundQuotes(row.row.last().content.value))
    }

    private fun writeLastRow(row: ResultRowViewModel, writer: PrintWriter){
        for (v in row.row.dropLast(1)){
            writer.append("${surroundQuotes(v.content.value)},")
        }
        writer.append(surroundQuotes(row.row.last().content.value))
    }

    private fun surroundQuotes(string: String?): String{
        return "\"$string\""
    }

}