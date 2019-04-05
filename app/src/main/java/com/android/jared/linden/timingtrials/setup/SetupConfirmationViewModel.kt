package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.android.jared.linden.timingtrials.util.ConverterUtils
import java.util.*

interface ISetupConformationViewModel{
    val title: LiveData<String>
    val lapsCourse: LiveData<String>
    val ridersInterval: LiveData<String>
    val startTime: LiveData<String>
    fun confirmationFunction(): Boolean
}

class SetupConfirmationViewModel (private val ttSetup: TimeTrialSetupViewModel) : ISetupConformationViewModel{

    val timeTrial = ttSetup.timeTrial

    override val title = Transformations.map(timeTrial){tt ->
        "Starting ${tt.ttName}"
    }

    override val lapsCourse = Transformations.map(timeTrial){tt->
        "${tt.laps} laps of ${tt.course?.courseName}"
    }

   override val ridersInterval = Transformations.map(timeTrial){tt->
        if(tt.interval == 0){
            "${tt.riders.count()} riders starting at 0 second intervals, mass start!"
        }else{
            "${tt.riders.count()} riders starting at ${tt.interval} second intervals"
        }

    }

   override val startTime = Transformations.map(timeTrial){tt->
        "First rider starting at ${ConverterUtils.dateToTimeDisplayString(tt.startTime)}"

    }


    override fun confirmationFunction(): Boolean{

        timeTrial.value?.let {
            return if(it.startTime.after(Calendar.getInstance().time)){

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
}

class ResumeOldConfirmationViewModel (private val ttSetup: TimeTrialSetupViewModel) : ISetupConformationViewModel{

    val timeTrial = ttSetup.originalTimeTrial

    override val title = Transformations.map(timeTrial){tt ->
        "Resume setting up previous ${tt.ttName} ?"
    }

    override val lapsCourse = Transformations.map(timeTrial){tt->
        "${tt.laps} laps of ${tt.course?.courseName}"
    }

    override val ridersInterval = Transformations.map(timeTrial){tt->
        if(tt.interval == 0){
            "${tt.riders.count()} riders starting at 0 second intervals, mass start!"
        }else{
            "${tt.riders.count()} riders starting at ${tt.interval} second intervals"
        }

    }

    override val startTime = Transformations.map(timeTrial){tt->
        "First rider starting at ${ConverterUtils.dateToTimeDisplayString(tt.startTime)}"

    }


    override fun confirmationFunction(): Boolean{

        timeTrial.value?.let { ttSetup.timeTrial.postValue(it) }
        return true
    }
}