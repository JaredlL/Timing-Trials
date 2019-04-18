package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.jared.linden.timingtrials.data.ITimeTrialEventRepository
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialEvent
import java.util.*
import javax.inject.Inject

class TimingViewModel  @Inject constructor(val timeTrialEventRepository: ITimeTrialEventRepository) : ViewModel() {

    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()
    val events: MutableLiveData<List<TimeTrialEvent>>  = MutableLiveData(listOf())
    val timeString: LiveData<String> = MutableLiveData()

    private var timer: Timer = Timer()
    private val TIMER_PERIOD_MS = 50L

    fun initialise(timeTrialId: Long) {
        if (timeTrial.value == null && timeTrialId != 0L) {
            timeTrial.addSource(timeTrialEventRepository.getTimeTrialWithEvents(timeTrialId)) { tt ->
                tt?.let {
                    timeTrial.value =  tt.timeTrial
                    events.value = tt.eventList
                    timer = Timer()
                    val task = object : TimerTask(){
                        override fun run() {
                            updateLoop()
                        }
                    }
                    timer.scheduleAtFixedRate(task, 0L, TIMER_PERIOD_MS)
                }
            }
        }
    }

    fun updateLoop(){

    }
}