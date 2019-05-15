package com.android.jared.linden.timingtrials

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject

class TestViewModel@Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository
) : ViewModel(){


    val  medTimeTrial = MediatorLiveData<TimeTrial>()
    val  timeTrial = TimeTrial.createBlank().copy(timeTrialHeader = TimeTrialHeader.createBlank()
            .copy(ttName = "Testing Timetrial",
                    startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
                    interval = 10))

    init {

        medTimeTrial.value = timeTrial
        medTimeTrial.addSource(riderRepository.allRiders){res->
            res?.let {ri->
                medTimeTrial.value?.let {
                    //val copy = it.copy(riderList = ri.filterIndexed { index, _ -> index%1 == 0 }.mapIndexed { index, rider -> TimeTrialRider(rider, it.timeTrialHeader.id, index+1,(60 + index * it.timeTrialHeader.interval).toLong()) })
                    val new = it.helper.addRidersAsTimeTrialRiders(ri.filterIndexed{index, _ -> index%7 == 0})
                    medTimeTrial.value = new
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

    fun insertTimingTt(){
        viewModelScope.launch(Dispatchers.IO) {
           if(timeTrialRepository.getTimeTrialByName("Testing Timetrial") == null){
               medTimeTrial.value?.let {
                   val newTt = it.copy(timeTrialHeader = it.timeTrialHeader.copy(
                           startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1), ZoneId.systemDefault()),
                           firstRiderStartOffset = 0,
                           interval = 2,
                           status = TimeTrialStatus.IN_PROGRESS
                   ))
                   timeTrialRepository.insertOrUpdate(newTt)
                   //callback()
               }

           }
        }
    }

    fun insertSetupTt(){
        viewModelScope.launch(Dispatchers.IO) {
            if(timeTrialRepository.getTimeTrialByName("Testing Timetrial") == null){
                medTimeTrial.value?.let {
                    val newTt = it.copy(timeTrialHeader = it.timeTrialHeader.copy(
                            startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(60), ZoneId.systemDefault()),
                            firstRiderStartOffset = 0,
                            interval = 2,
                            status = TimeTrialStatus.SETTING_UP
                    ))
                    timeTrialRepository.insertOrUpdate(newTt)
                    //callback()
                }

            }
        }
    }

    fun insertFinishedTt(){
        viewModelScope.launch(Dispatchers.IO) {
            if(timeTrialRepository.getTimeTrialByName("Testing Timetrial") == null){
                medTimeTrial.value?.let {
                    val newTt = it.copy(timeTrialHeader = it.timeTrialHeader.copy(
                            startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).minusSeconds(180), ZoneId.systemDefault()),
                            firstRiderStartOffset = 0,
                            interval = 2,
                            status = TimeTrialStatus.FINISHED
                    ))
                    timeTrialRepository.insertOrUpdate(newTt)
                    //callback()
                }

            }
        }
    }

}