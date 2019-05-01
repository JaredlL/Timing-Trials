package com.android.jared.linden.timingtrials

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject

class TestViewModel@Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository
) : ViewModel(){


    val  medTimeTrial = MediatorLiveData<TimeTrial>()
    val  timeTrial = TimeTrial.createBlank().apply {
        timeTrialDefinition.ttName = "Testing Timetrial"
        timeTrialDefinition.startTime = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15)
        timeTrialDefinition.interval = 10
    }

    init {

        medTimeTrial.value = timeTrial

        medTimeTrial.addSource(riderRepository.allRiders){res->
            res?.let {res->
                medTimeTrial.value?.let {
                    val copy = it.copy(riderList = res.filterIndexed { index, _ -> index%3 == 0 }.map { r-> TimeTrialRider(r, it.timeTrialDefinition.id) })
                    medTimeTrial.value = copy
                }
            }
        }

        medTimeTrial.addSource(courseRepository.allCourses){res->
            res?.let {
                medTimeTrial.value?.let {
                    val defCopy = it.timeTrialDefinition.copy(course = res.firstOrNull())
                    medTimeTrial.value = it.copy(timeTrialDefinition = defCopy)
                }
            }
        }

        medTimeTrial.addSource(timeTrialRepository.getSetupTimeTrial()){res->
            res?.let {
                medTimeTrial.value = res
            }

        }
    }

    fun insertTt(callback: () -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
           if(timeTrialRepository.getTimeTrialByName("Testing Timetrial") == null){
               medTimeTrial.value?.let {
                   val newTt = it.copy(timeTrialDefinition = it.timeTrialDefinition.copy(startTime = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15)))
                   timeTrialRepository.insertOrUpdate(newTt)
                   callback()
               }
           }
        }
    }

}