package com.jaredlinden.timingtrials.domain.csv

import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.resultexplorer.ResultExplorerSpreadSheet
import com.jaredlinden.timingtrials.timetrialresults.ResultRowViewModel
import com.jaredlinden.timingtrials.util.LengthConverter
import com.opencsv.CSVWriter
import org.threeten.bp.format.DateTimeFormatter
import java.io.*

class CsvTimeTrialResultWriter (val timeTrial: TimeTrial, val results: List<List<String>>, val lengthConverter: LengthConverter){

    fun writeToPath(outputStream: OutputStream){
        val lines: MutableList<List<String>> = mutableListOf()
        lines.add(listOf("Results, Powered by Timing Trials"))
        addTimeTrialRow(lines)
        writeCourseRow(lines)
        for(r in results){
            lines.add(r)
        }

        val csvWriter =  CSVWriter(outputStream.bufferedWriter())
        csvWriter.writeAll(lines.map { it.toTypedArray() })
        csvWriter.flush()
        csvWriter.close()
    }

    private fun addTimeTrialRow(lines: MutableList<List<String>>){
        val formatter = DateTimeFormatter.ofPattern("d/M/y")

        lines.add(listOf(">>Time Trial Name","Date","Laps","Description"))
        lines.add(listOf(timeTrial.timeTrialHeader.ttName, timeTrial.timeTrialHeader.startTime?.format(formatter)?:"",timeTrial.timeTrialHeader.laps.toString(), timeTrial.timeTrialHeader.description))
    }

    private fun writeCourseRow(lines: MutableList<List<String>>){
        val course = timeTrial.course
        if(course != null){
            lines.add(listOf(">>Course Name","Length (${lengthConverter.unitDef.name})","CTT Code"))
            lines.add(listOf(course.courseName, course.length?.let{lengthConverter.lengthToDisplay(it)}?:"",course.cttName))
        }else{
            lines.add(listOf("Unknown Course"))
        }
    }
}

class CsvSheetWriter(val sheet: ResultExplorerSpreadSheet){
    fun writeToPath(outputStream: OutputStream){

        val all = listOf(listOf(">>timing trials mixed results")) + listOf( sheet.sheetColumns.map { it.headingText }) + (sheet.data)

        val csvWriter =  CSVWriter(outputStream.bufferedWriter())
        csvWriter.writeAll(all.map { it.toTypedArray() })
        csvWriter.flush()
        csvWriter.close()
    }
}

