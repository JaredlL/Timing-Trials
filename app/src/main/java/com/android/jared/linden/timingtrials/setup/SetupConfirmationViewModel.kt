package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.android.jared.linden.timingtrials.MainViewModel
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.util.ConverterUtils
import org.threeten.bp.Instant

interface ISetupConformationViewModel{
    val title: LiveData<String>
    val lapsCourse: LiveData<String>
    val ridersInterval: LiveData<String>
    val startTime: LiveData<String>
    val timeTrial: LiveData<TimeTrial>
    fun positiveFunction(): Boolean
    fun negativeFunction(): Boolean
}

class SetupConfirmationViewModel (private val ttSetup: SetupViewModel) : ISetupConformationViewModel{

   override val timeTrial = ttSetup.timeTrial

    override val title = Transformations.map(timeTrial){tt ->
        "Starting ${tt?.ttName}"
    }

    override val lapsCourse = Transformations.map(timeTrial){tt->
        "${tt?.laps} laps of ${tt.course?.courseName}"
    }

   override val ridersInterval = Transformations.map(timeTrial){tt->
        if(tt.interval.isZero){
            "${tt?.riders?.count()} riders starting at 0 second intervals, mass start!"
        }else{
            "${tt?.riders?.count()} riders starting at ${tt?.interval} second intervals"
        }

    }

   override val startTime = Transformations.map(timeTrial){tt->
        "First rider starting at ${tt?.let{ConverterUtils.instantToSecondsDisplayString(tt.startTime)}}"

    }


    override fun positiveFunction(): Boolean{

        timeTrial.value?.let {
            return if(it.startTime.isAfter(Instant.now())){

                it.isSetup = true
                timeTrial.value = it
                ttSetup.insertTt()

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

class ResumeOldConfirmationViewModel (private val mainViewModel: MainViewModel) : ISetupConformationViewModel{


    override val timeTrial = mainViewModel.timeTrial

    override val title = Transformations.map(timeTrial){tt ->
        "Resume setting up previous ${tt?.ttName} ?"
    }

    override val lapsCourse = Transformations.map(timeTrial){tt->
        "${tt?.laps} laps of ${tt?.course?.courseName}"
    }

    override val ridersInterval = Transformations.map(timeTrial){tt->
        if(tt!= null) {
            if(tt.interval.isZero){
                "${tt.riders.count()} riders starting at 0 second intervals, mass start!"
            }else{
                "${tt.riders.count()} riders starting at ${tt.interval} second intervals"
            }
        }else{
            "null"
        }


    }

    override val startTime = Transformations.map(timeTrial){tt->
        "First rider starting at ${tt?.let{ConverterUtils.instantToSecondsDisplayString(tt?.startTime)}}"

    }

    override fun positiveFunction(): Boolean{

        return true
    }

    override fun negativeFunction(): Boolean {
        timeTrial.value?.let { mainViewModel.deleteTimeTrial(it) }
        return true
    }


}