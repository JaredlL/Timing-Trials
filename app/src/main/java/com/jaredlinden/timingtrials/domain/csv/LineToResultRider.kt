package com.jaredlinden.timingtrials.domain.csv

import com.jaredlinden.timingtrials.domain.ILineToObjectConverter
import com.jaredlinden.timingtrials.domain.TimeTrialRiderIO
import com.jaredlinden.timingtrials.domain.StringToObjectField
import com.opencsv.CSVReader
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeParseException
import java.io.StringReader

class LineToResultRiderConverter: ILineToObjectConverter<TimeTrialRiderIO> {

    class ToRiderFirstName(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { it.contains("first", true) && it.contains("name", true) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            return target.copy(firstName = valString)
        }
    }

    class LastNameFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("sur", true) || it.contains("last", true)) && it.contains("name", true) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            return target.copy(lastName = valString)
        }
    }

    class FullNameFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { it.contains("name", true) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            val split = valString.split(" ", ignoreCase = true)
            return target.copy(firstName = split.first(), lastName = split.drop(1).joinToString(" "))
        }
    }

    class ClubFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(CLUB, true)) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            return target.copy(club = valString)
        }
    }



    class CategoryFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(CATEGORY, true))}

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            return target.copy(category = valString)}
    }

    class GenderFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(GENDER, true)) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {

            return target.copy(gender = ObjectFromString.gender(valString))
        }
    }

    class BibFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("bib", true))}

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            return target.copy(bib = valString.toIntOrNull())}
    }

    class StartTimeFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("start", true)) && (it.contains("time", true)) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            val lt = try {
                LocalTime.parse(valString)
            } catch (e: DateTimeParseException) {
                null
            }
            return target.copy(startTime = lt)
        }
    }

    class FinishTimeFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("time", true)) && !((it.contains("start", true))) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {

            return target.copy(finishTime =  ObjectFromString.time(valString))
        }
    }

    class SplitFieldSetter(private val heading: List<String>, val splitIndex: Int): StringToObjectField<TimeTrialRiderIO>() {

        override val fieldIndex: Int?
            get() = splitIndex

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {

            val newSplit = ObjectFromString.time(valString)

            return newSplit?.let {
                target.copy(splits = target.splits + it)
            }?:target

        }
    }
    class NotesNameFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { it.contains("note", true) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            return target.copy(notes = valString)
        }
    }

    override fun isHeading(line: String): Boolean {
        return line.startsWith(">>Rider") || isCttResultHeading(line) || isCttStartsheettHeading(line)
    }

    fun isCttResultHeading(heading: String): Boolean{
        return heading.contains("position", ignoreCase = true) && heading.contains("tricycle", ignoreCase = true)
    }

    fun isCttStartsheettHeading(heading: String): Boolean{
        return heading.contains("start_time", ignoreCase = true) && heading.contains("bib", ignoreCase = true)
    }

    var stringToFieldList: List<StringToObjectField<TimeTrialRiderIO>> = listOf()

    override fun setHeading(headingLine: String) {
        val splitHeading = CSVReader(StringReader(headingLine)).readNext().map { it }

        if(splitHeading.filter { it.contains("name", ignoreCase = true) }.count() == 1){
            stringToFieldList = listOf(
                    FullNameFieldSetter(splitHeading),
                    ClubFieldSetter(splitHeading),
                    CategoryFieldSetter(splitHeading),
                    GenderFieldSetter(splitHeading),
                    FinishTimeFieldSetter(splitHeading),
                    NotesNameFieldSetter(splitHeading),
                    StartTimeFieldSetter(splitHeading),
                    BibFieldSetter(splitHeading)
            )
        }else{
            stringToFieldList = listOf(
                    ToRiderFirstName(splitHeading),
                    LastNameFieldSetter(splitHeading),
                    ClubFieldSetter(splitHeading),
                    CategoryFieldSetter(splitHeading),
                    GenderFieldSetter(splitHeading),
                    FinishTimeFieldSetter(splitHeading),
                    NotesNameFieldSetter(splitHeading),
                    StartTimeFieldSetter(splitHeading),
                    BibFieldSetter(splitHeading)
            )
        }

        val splitsList = splitHeading.mapIndexedNotNull  { index, s ->
            if (s.contains("split", ignoreCase = true)) {
                SplitFieldSetter(splitHeading, index)
            }else{
                null
            }
        }
        stringToFieldList = stringToFieldList + splitsList
    }

    override fun importLine(dataLine: List<String>): TimeTrialRiderIO? {
        var importRider = TimeTrialRiderIO()

        for(fieldFiller in stringToFieldList){
            importRider = fieldFiller.applyFieldToObject(dataLine, importRider)
        }
        val ir = importRider
        return if(ir.firstName.isNotBlank()){
            ir
        }else{
            null
        }
    }
}