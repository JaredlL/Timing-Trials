package com.jaredlinden.timingtrials.domain.csv

import com.jaredlinden.timingtrials.data.CompleteInformationRow
import com.jaredlinden.timingtrials.domain.ILineToObjectConverter
import com.jaredlinden.timingtrials.domain.StringToObjectField
import com.jaredlinden.timingtrials.domain.TimeTrialRiderIO
import com.jaredlinden.timingtrials.util.LengthConverter
import com.opencsv.CSVReader
import java.io.StringReader

class LineToCompleteResult: ILineToObjectConverter<CompleteInformationRow>{


    companion object{

    }



    override fun isHeading(line: String): Boolean {
        return line.startsWith(">>timing trials mixed results")
    }

    var stringToFieldList: List<StringToObjectField<CompleteInformationRow>> = listOf()
    override fun setHeading(headingLine: String) {
        val splitHeading = CSVReader(StringReader(headingLine)).readNext().map { it }
        if(splitHeading.filter { it.contains(RIDER, ignoreCase = true) }.count() == 1){
            stringToFieldList = listOf(
                    FullNameFieldSetter(splitHeading),
                    ClubFieldSetter(splitHeading),
                    CategoryFieldSetter(splitHeading),
                    GenderFieldSetter(splitHeading),
                    FinishTimeFieldSetter(splitHeading),
                    CourseNameFieldSetter(splitHeading),
                    CourseCttNameFieldSetter(splitHeading),
                    CourseDistanceFieldSetter(splitHeading),
                    TimeTrialNameFieldSetter(splitHeading),
                    TimeTrialDateFieldSetter(splitHeading),
                    TimeTrialLapsSetter(splitHeading),
                    TimeTrialDescriptionSetter(splitHeading),
                    ResultTimeFieldSetter(splitHeading),
                    ResultNoteFieldSetter(splitHeading)
            )
        }else{
            stringToFieldList = listOf(
                    FirstNameFieldSetter(splitHeading),
                    LastNameFieldSetter(splitHeading),
                    ClubFieldSetter(splitHeading),
                    CategoryFieldSetter(splitHeading),
                    GenderFieldSetter(splitHeading),
                    FinishTimeFieldSetter(splitHeading),
                    CourseNameFieldSetter(splitHeading),
                    CourseCttNameFieldSetter(splitHeading),
                    CourseDistanceFieldSetter(splitHeading),
                    TimeTrialNameFieldSetter(splitHeading),
                    TimeTrialDateFieldSetter(splitHeading),
                    TimeTrialLapsSetter(splitHeading),
                    TimeTrialDescriptionSetter(splitHeading),
                    ResultTimeFieldSetter(splitHeading),
                    ResultNoteFieldSetter(splitHeading)
            )
        }
    }

    override fun importLine(dataLine: List<String>): CompleteInformationRow? {
        var row = CompleteInformationRow()
        for(fieldFiller in stringToFieldList){
            row = fieldFiller.applyFieldToObject(dataLine, row)
        }
        return row
    }



    class FullNameFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { it.contains(RIDER, true) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            val split = valString.split(" ", ignoreCase = true)
            return target.copy(riderFirstName = split.first(), riderLastName = split.drop(1).joinToString(" "))
        }
    }

    class FirstNameFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(FIRSTNAME, true))}

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(riderFirstName = valString)
        }
    }

    class LastNameFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(SURNAME, true) || it.contains(LASTNAME, true))}

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(riderLastName = valString)
        }
    }

    class ClubFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(CLUB, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(riderClub = valString)
        }
    }

    class CategoryFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(CATEGORY, true))}

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(riderCategory = valString)}
    }

    class GenderFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("gender", true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {

            return target.copy(riderGender = ObjectFromString.gender(valString))
        }
    }
    class FinishTimeFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(TIME, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {

            return target.copy(resultTime =  ObjectFromString.time(valString))

        }
    }

    class CourseNameFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(COURSE, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(courseName =  valString)

        }
    }

    class CourseCttNameFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(CTTNAME, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(courseCttName =  valString)

        }
    }

    class CourseDistanceFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {



        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(LENGTH, true) || it.contains(DISTANCE, true)) }

        val headingConverter: LengthConverter = fieldIndex?.let {
            heading.getOrNull(it)?.let {
                LengthConverter.findLengthConverterForString(it)
            }
        }?: LengthConverter.default

                override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
                    val dub = valString.toDoubleOrNull()
                    return if (dub != null){
                        target.copy(courseDistance =  headingConverter.convert(dub))
                    }else{
                        target.copy(courseDistance =  ObjectFromString.distance(valString))
                    }


        }
    }

    class TimeTrialNameFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(TIME_TRIAL_NAME, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(timeTrialName =  valString)

        }
    }

    class TimeTrialDateFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(DATE, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(timeTrialDate =  ObjectFromString.date(valString))

        }
    }

    class TimeTrialLapsSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(LAPS, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(timeTrialLaps =  valString.toIntOrNull()?:1)

        }
    }

    class TimeTrialDescriptionSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(DESCRIPTION, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(timeTrialDescription =  valString)

        }
    }

    class ResultTimeFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(TIME, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(resultTime =  ObjectFromString.time(valString))

        }
    }

    class ResultNoteFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(NOTES, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(resultNotes =  valString)

        }
    }

    class SplitFieldSetter(private val heading: List<String>): StringToObjectField<CompleteInformationRow>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains(SPLIT, true)) }

        override fun applyFieldFromString(valString: String, target: CompleteInformationRow): CompleteInformationRow {
            return target.copy(resultNotes =  valString)

        }
    }






}