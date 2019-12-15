package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.android.jared.linden.timingtrials.TitleViewModel
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.util.ConverterUtils
import org.threeten.bp.OffsetDateTime

interface ISetupConformationViewModel{
    val title: LiveData<String>
    val lapsCourse: LiveData<String>
    val ridersInterval: LiveData<String>
    val startTime: LiveData<String>
    val timeTrial: LiveData<TimeTrial?>
    fun positiveFunction(): Boolean
    fun negativeFunction(): Boolean
    fun insertTt(tt: TimeTrial)
}

class SetupConfirmationViewModel (private val ttSetup: SetupViewModel) : ISetupConformationViewModel{

   override val timeTrial = ttSetup.timeTrial
    val timeTrialDefinition = Transformations.map(timeTrial){it?.timeTrialHeader}

    override val title = Transformations.map(timeTrialDefinition){ tt ->
        "Starting ${tt?.ttName}"
    }

    override val lapsCourse = Transformations.map(timeTrial){ tt->
        "${tt?.timeTrialHeader?.laps} laps of ${tt?.course?.courseName}"
    }

   override val ridersInterval = Transformations.map(timeTrial){
       it?.let {tt->
           if(tt.timeTrialHeader.interval == 0){
               return@map "${tt.riderList.count()} riders starting at 0 second intervals, mass start!"
           }else{
               return@map "${tt.riderList.count()} riders starting at ${tt.timeTrialHeader.interval} second intervals"
           }
       }
       return@map "Null"


    }

   override val startTime = Transformations.map(timeTrialDefinition){ tt->
        "First rider starting at ${tt?.let{ConverterUtils.instantToSecondsDisplayString(tt.startTime.toInstant())}}"

    }

    override fun insertTt(tt: TimeTrial) {
        ttSetup.updateTimeTrial(tt)
    }


    override fun positiveFunction(): Boolean{

        timeTrial.value?.let {
            return if(it.timeTrialHeader.startTime.isAfter(OffsetDateTime.now())){
                val newTt = it.updateHeader(it.timeTrialHeader.copy(status = TimeTrialStatus.IN_PROGRESS))
                ttSetup.updateTimeTrial(newTt)
                true
            }else{
                false
            }
        }
        return false
    }

    override fun negativeFunction(): Boolean {
        return true
    }
}

class ResumeOldConfirmationViewModel (private val mainViewModel: TitleViewModel) : ISetupConformationViewModel{


    override val timeTrial = mainViewModel.nonFinishedTimeTrial
    val timeTrialDefinition = Transformations.map(timeTrial){it?.timeTrialHeader}

    override val title = Transformations.map(timeTrialDefinition){ tt ->
        "Resume setting up previous ${tt?.ttName} ?"
    }

    override val lapsCourse = Transformations.map(timeTrial){ tt->
        "${tt?.timeTrialHeader?.laps} laps of ${tt?.course?.courseName}"
    }

    override val ridersInterval = Transformations.map(timeTrial){ tt->
        if(tt!= null) {
            if(tt.timeTrialHeader.interval == 0){
                "${tt.riderList.size} riders starting at 0 second intervals, mass start!"
            }else{
                "${tt.riderList.size} riders starting at ${tt.timeTrialHeader.interval} second intervals"
            }
        }else{
            "null"
        }


    }

    override fun insertTt(tt: TimeTrial) {
    }

    override val startTime = Transformations.map(timeTrialDefinition){ tt->
        "First rider starting at ${tt?.let{ConverterUtils.instantToSecondsDisplayString(tt.startTime.toInstant())}}"

    }

    override fun positiveFunction(): Boolean{

        return true
    }

    override fun negativeFunction(): Boolean {
        timeTrial.value?.let { mainViewModel.deleteTimeTrial(it) }
        return true
    }


}