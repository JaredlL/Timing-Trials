package com.android.jared.linden.timingtrials.domain.csv

import com.android.jared.linden.timingtrials.data.Gender
import com.android.jared.linden.timingtrials.domain.ILineToObjectConverter
import com.android.jared.linden.timingtrials.domain.TimeTrialRiderIO
import com.android.jared.linden.timingtrials.domain.StringToObjectField
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeParseException

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
            get() = heading.indexOfFirst { (it.contains("club", true) || it.contains("team", true)) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            return target.copy(club = valString)
        }
    }



    class CategoryFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("category", true))}

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            return target.copy(category = valString)}
    }

    class GenderFieldSetter(private val heading: List<String>): StringToObjectField<TimeTrialRiderIO>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("gender", true)) }

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {
            val gen = when {
                valString.equals("male", true) -> { Gender.MALE }
                valString.equals("female", true) -> { Gender.FEMALE }
                valString.equals("m", true) -> { Gender.MALE }
                valString.equals("f", true) -> { Gender.FEMALE }
                valString.equals("other", true) -> { Gender.OTHER }
                else -> Gender.UNKNOWN
            }

            return target.copy(gender = gen)
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

            //val times = valString.split(":",".").reversed()

            val splitAtPoint = valString.split(".")
            if(splitAtPoint.size > 1){

                val ms = splitAtPoint.last().let { ".$it" }.toDoubleOrNull()?.let { it * 1000}?.toInt()?:0

                val splits = splitAtPoint.first().split(":").reversed()
                val sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
                val min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
                val hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0

                val sum = (hour + min + sec + ms).toLong()
                val fval = if(sum > 0) sum else null

                return target.copy(finishTime = fval)

            }else{
                val splits = splitAtPoint.first().split(":").reversed()
                var ms = 0
                var sec = 0
                var min = 0
                var hour = 0

                if(splits.firstOrNull()?.length == 1){
                    ms = splits.getOrNull(0)?.let { ".$it" }?.toDoubleOrNull()?.let { it * 1000}?.toInt()?:0
                    sec = splits.getOrNull(1)?.toIntOrNull()?.times(1000)?:0
                    min = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60)?:0
                    hour = splits.getOrNull(3)?.toIntOrNull()?.times(1000 * 60 * 60)?:0
                }else{
                    sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
                    min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
                    hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0
                }
                val sum = (hour + min + sec + ms).toLong()
                val fval = if(sum > 0) sum else null

                return target.copy(finishTime = fval)
            }

            //val ms = times.getOrNull(0)?.let { ".$it" }?.toDouble()?.let { it * 1000 }?.toInt()?:0

        }
    }

    class SplitFieldSetter(private val heading: List<String>, val splitIndex: Int): StringToObjectField<TimeTrialRiderIO>() {

        override val fieldIndex: Int?
            get() = splitIndex

        override fun applyFieldFromString(valString: String, target: TimeTrialRiderIO): TimeTrialRiderIO {

            //val times = valString.split(":",".").reversed()

            val splitAtPoint = valString.split(".")
            if(splitAtPoint.size > 1){

                val ms = splitAtPoint.last().let { ".$it" }.toDoubleOrNull()?.let { it * 1000}?.toInt()?:0

                val splits = splitAtPoint.first().split(":").reversed()
                val sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
                val min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
                val hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0


                return target.copy(splits = target.splits + (hour + min + sec + ms).toLong())

            }else{
                val splits = splitAtPoint.first().split(":").reversed()
                var ms = 0
                var sec = 0
                var min = 0
                var hour = 0

                if(splits.firstOrNull()?.length == 1){
                    ms = splits.getOrNull(0)?.let { ".$it" }?.toDoubleOrNull()?.let { it * 1000}?.toInt()?:0
                    sec = splits.getOrNull(1)?.toIntOrNull()?.times(1000)?:0
                    min = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60)?:0
                    hour = splits.getOrNull(3)?.toIntOrNull()?.times(1000 * 60 * 60)?:0
                }else{
                    sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
                    min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
                    hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0
                }


                return target.copy(splits = target.splits + (hour + min + sec + ms).toLong())
            }
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

        return (line.contains("Rider", ignoreCase = true) || line.contains("athlete", ignoreCase = true)) && line.contains("name", ignoreCase = true) || isCttHeading(line)

    }

    fun isCttHeading(heading: String): Boolean{
        return heading.contains("firstname", ignoreCase = true) && heading.contains("lastname", ignoreCase = true)
    }


    var stringToFieldList: List<StringToObjectField<TimeTrialRiderIO>> = listOf()

    override fun setHeading(headingLine: String) {
        val splitHeading = headingLine.split(",")
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

    override fun importLine(dataLine: String): TimeTrialRiderIO? {
        var importRider = TimeTrialRiderIO()

        for(fieldFiller in stringToFieldList){
            importRider = fieldFiller.applyFieldToObject(CSVUtils.lineToList(dataLine), importRider)
        }
        val ir = importRider
        return if(ir.firstName.isNotBlank()){
            ir
        }else{
            null
        }
    }


}