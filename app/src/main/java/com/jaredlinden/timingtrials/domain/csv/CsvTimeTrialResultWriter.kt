package com.jaredlinden.timingtrials.domain.csv

import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.resultexplorer.ResultExplorerSpreadSheet
import com.jaredlinden.timingtrials.timetrialresults.ResultRowViewModel
import com.opencsv.CSVWriter
import org.threeten.bp.format.DateTimeFormatter
import java.io.*

class CsvTimeTrialResultWriter (val timeTrial: TimeTrial, val results: List<ResultRowViewModel>){


    fun writeToPath(filePath: OutputStream){
        val writer = PrintWriter(filePath)
        writer.appendln(surroundQuotes("Results, Powered by Timing Trials"))
        writeTimeTrialRow(writer)
        writeCourseRow(writer)
        for(r in results.dropLast(1)) writeRowNewLine(r, writer)
        writeLastRow(results.last(), writer)
        writer.flush()
        writer.close()
    }

    private fun writeTimeTrialRow(writer: PrintWriter){
        val formatter = DateTimeFormatter.ofPattern("d/M/y")
        writer.appendln("TimeTrial Name,TimeTrial Date,TimeTrial Laps")
        writer.appendln("${timeTrial.timeTrialHeader.ttName},${timeTrial.timeTrialHeader.startTime.format(formatter)},${timeTrial.timeTrialHeader.laps}")
    }

    private fun writeCourseRow(writer: PrintWriter){
        val course = timeTrial.course
        if(course != null){
            writer.appendln("Course Name,Course Length,Course CTT Name")
            writer.appendln("${course.courseName}.${course.length/1000L},${course.cttName}")
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
class CsvSheetWriter(val sheet: ResultExplorerSpreadSheet){
    fun writeToPath(ouputStream: OutputStream){

        val all = listOf(sheet.sheetColumns.map { it.headingText }) + (sheet.data)

       val csvWriter =  CSVWriter(ouputStream.bufferedWriter())
        csvWriter.writeAll(all.map { it.toTypedArray() })
        csvWriter.flush()
        csvWriter.close()
    }
}

