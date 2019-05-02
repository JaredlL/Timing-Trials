package com.android.jared.linden.timingtrials

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        timeTrialHeader.ttName = "Testing Timetrial"
        timeTrialHeader.startTime = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15)
        timeTrialHeader.interval = 10
    }

    init {

        medTimeTrial.value = timeTrial

        medTimeTrial.addSource(riderRepository.allRiders){res->
            res?.let {res->
                medTimeTrial.value?.let {
                    val copy = it.copy(riderList = res.filterIndexed { index, _ -> index%3 == 0 }.map { r-> TimeTrialRider(r, it.timeTrialHeader.id) })
                    medTimeTrial.value = copy
                }
            }
        }

        medTimeTrial.addSource(courseRepository.allCourses){res->
            res?.let {
                medTimeTrial.value?.let {
                    val defCopy = it.timeTrialHeader.copy(course = res.firstOrNull())
                    medTimeTrial.value = it.copy(timeTrialHeader = defCopy)
                }
            }
        }

        medTimeTrial.addSource(timeTrialRepository.getSetupTimeTrial()){res->
            res?.let {
                medTimeTrial.value = res
            }

        }
        medTimeTrial.addSource(timeTrialRepository.getTimingTimeTrial()){res->
            res?.let {
                medTimeTrial.value = res
            }

        }
    }

    fun insertTt(){
        viewModelScope.launch(Dispatchers.IO) {
           if(timeTrialRepository.getTimeTrialByName("Testing Timetrial") == null){
               medTimeTrial.value?.let {
                   val newTt = it.copy(timeTrialHeader = it.timeTrialHeader.copy(
                           startTime = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15),
                           isSetup = true
                   ))
                   timeTrialRepository.insertOrUpdate(newTt)
                   //callback()
               }

           }
        }
    }

}