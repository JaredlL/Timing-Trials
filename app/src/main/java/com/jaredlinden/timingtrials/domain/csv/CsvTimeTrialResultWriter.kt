package com.jaredlinden.timingtrials.domain.csv

import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.resultexplorer.ResultExplorerSpreadSheet
import com.jaredlinden.timingtrials.timetrialresults.ResultRowViewModel
import com.jaredlinden.timingtrials.util.LengthConverter
import com.opencsv.CSVWriter
import org.threeten.bp.format.DateTimeFormatter
import java.io.*

class CsvTimeTrialResultWriter (val timeTrial: TimeTrial, val results: List<ResultRowViewModel>, val lengthConverter: LengthConverter){


    fun writeToPath(outputStream: OutputStream){
        val lines: MutableList<List<String>> = mutableListOf()
        lines.add(listOf("Results, Powered by Timing Trials"))
        addTimeTrialRow(lines)
        writeCourseRow(lines)
        for(r in results){
            lines.add(r.row.map { it.content.value?:"" })
        }

        val csvWriter =  CSVWriter(outputStream.bufferedWriter())
        csvWriter.writeAll(lines.map { it.toTypedArray() })
        csvWriter.flush()
        csvWriter.close()
    }

    private fun addTimeTrialRow(lines: MutableList<List<String>>){
        val formatter = DateTimeFormatter.ofPattern("d/M/y")

        lines.add(listOf("TimeTrial Name","TimeTrial Date","TimeTrial Laps","TimeTrial Description"))
        lines.add(listOf(timeTrial.timeTrialHeader.ttName, timeTrial.timeTrialHeader.startTime.format(formatter),timeTrial.timeTrialHeader.laps.toString(), timeTrial.timeTrialHeader.description))
    }

    private fun writeCourseRow(lines: MutableList<List<String>>){
        val course = timeTrial.course
        if(course != null){
            lines.add(listOf("Course Name","Course Length (${lengthConverter.unitDef.name})","Course CTT Name"))
            lines.add(listOf(course.courseName,lengthConverter.lengthToDisplay(course.length),course.cttName))
        }else{
            lines.add(listOf("Unknown Course"))
        }
    }

//    private fun writeRowNewLine(row: ResultRowViewModel, writer: PrintWriter){
//        for (v in row.row.dropLast(1)){
//            writer.append("${surroundQuotes(v.content.value)},")
//        }
//        writer.appendln(surroundQuotes(row.row.last().content.value))
//    }
//
//    private fun writeLastRow(row: ResultRowViewModel, writer: PrintWriter){
//        for (v in row.row.dropLast(1)){
//            writer.append("${surroundQuotes(v.content.value)},")
//        }
//        writer.append(surroundQuotes(row.row.last().content.value))
//    }
//
//    private fun surroundQuotes(string: String?): String{
//        return "\"$string\""
//    }

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

