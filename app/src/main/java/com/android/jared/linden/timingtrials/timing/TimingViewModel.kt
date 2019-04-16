package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.android.jared.linden.timingtrials.data.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.TimeTrial
import java.util.*
import javax.inject.Inject

class TimingViewModel  @Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel() {

    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()
    val timeTrialState = TimeTrialState(this)
    val timingVm = TimingVM(this)
    var timer: Timer = Timer()

    private val TIMER_PERIOD_MS = 50L

    fun initialise(timeTrialId: Long) {
        if (timeTrial.value == null && timeTrialId != 0L) {
            timeTrial.addSource(timeTrialRepository.getTimeTrialById(timeTrialId)) { tt ->
                tt?.let {
                    timeTrial.value =  tt
                    timer = Timer()
                    val task = object : TimerTask(){
                        override fun run() {

                        }
                    }
                    timer.scheduleAtFixedRate(task, 0L, TIMER_PERIOD_MS)
                }
            }
        }
    }
}