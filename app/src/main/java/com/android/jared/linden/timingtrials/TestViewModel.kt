package com.android.jared.linden.timingtrials

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.ICourseRepository
import com.android.jared.linden.timingtrials.data.IRiderRepository
import com.android.jared.linden.timingtrials.data.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.TimeTrial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class TestViewModel@Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository
) : ViewModel(){


    val  medTimeTrial = MediatorLiveData<TimeTrial>()
    val  timeTrial = TimeTrial.createBlank().apply { ttName = "Testing Timetrial" }

    init {


        medTimeTrial.addSource(riderRepository.allRiders){res->
            res?.let {
                timeTrial.riders = res.filterIndexed { index, _ -> index%3 == 0 }
                if(timeTrial.course != null){ medTimeTrial.value = timeTrial}
            }
        }

        medTimeTrial.addSource(courseRepository.allCourses){res->
            res?.let {
                timeTrial.course = res.firstOrNull()
                if(timeTrial.riders.count() > 0){ medTimeTrial.value = timeTrial}
            }
            }
    }

    fun insertTt(){
        viewModelScope.launch(Dispatchers.IO) {
           if(timeTrialRepository.getTimeTrialByName("Testing Timetrial") == null){
               medTimeTrial.value?.let { timeTrialRepository.insertOrUpdate(it) }
           }

        }
    }

}