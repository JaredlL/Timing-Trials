package com.jaredlinden.timingtrials.data

import com.jaredlinden.timingtrials.util.ConverterUtils
import org.threeten.bp.*

const val UNKNOWN = "Unknown"
const val COMPLETE_INFORMATION_TAG = ">>timing trials mixed results"

data class CompleteInformationRow(
        val riderFirstName:String? = null,
        val riderLastName:String ="",
        val riderClub:String ="",
        val riderGender: Gender = Gender.UNKNOWN,
        val riderCategory: String ="",
        val courseName: String? = null,
        val courseCttName: String ="",
        val courseDistance: Double? = null,
        val timeTrialName: String? = null,
        val timeTrialDate: LocalDate? = null,
        val timeTrialLaps: Int = 1,
        val timeTrialDescription: String = "",
        val resultTime: Long? = null,
        val resultNotes: String ="",
        val resultSplits: List<Long> = listOf()
) {

    fun rider(): Rider?{
        return riderFirstName?.let {
           Rider(it, riderLastName, riderClub, null, riderCategory, riderGender)
        }
    }

    fun timeTrialHeader(): TimeTrialHeader?{
        val ttName = timeTrialName()
        return if(ttName != null){
            val offsetDateTime = timeTrialDate?.let{
                OffsetDateTime.of(it, LocalTime.of(19,0,0), ZoneId.systemDefault().rules.getOffset(Instant.now()))
            }

            TimeTrialHeader(ttName,
                laps = timeTrialLaps,
                startTime = offsetDateTime,
                description = timeTrialDescription,
                status = TimeTrialStatus.FINISHED)
        }else{
            null
        }
    }

    fun timeTrialName():String?{
       return when{
            timeTrialName != null -> timeTrialName
            courseName != null && timeTrialDate != null -> "$courseName ${ConverterUtils.localDateToDisplay(timeTrialDate)}"
            courseName != null -> "$UNKNOWN $courseName"
            else -> null
        }
    }

    fun  course():Course?{
        return courseName?.let { Course(it, courseDistance?:0.0, courseCttName) }
    }
}