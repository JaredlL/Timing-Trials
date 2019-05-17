package com.android.jared.linden.timingtrials

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.random.Random

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

        viewModelScope.launch(Dispatchers.IO) {
            timeTrialRepository.insertOrUpdate(timeTrial)
        }

        medTimeTrial.value = timeTrial
        medTimeTrial.addSource(riderRepository.allRiders){res->
            res?.let {ri->
                medTimeTrial.value?.let {
                    //val copy = it.copy(riderList = ri.filterIndexed { index, _ -> index%1 == 0 }.mapIndexed { index, rider -> TimeTrialRider(rider, it.timeTrialHeader.id, index+1,(60 + index * it.timeTrialHeader.interval).toLong()) })
                    val new = it.helper.addRidersAsTimeTrialRiders(ri.filterIndexed{index, _ -> index%1 == 0})
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

        medTimeTrial.addSource(timeTrialRepository.getLiveTimeTrialByName("Testing Timetrial")){
            medTimeTrial.value = it
        }
    }

    fun insertTimingTt(){
        viewModelScope.launch(Dispatchers.IO) {

               medTimeTrial.value?.let {
                   val newTt = it.copy(timeTrialHeader = it.timeTrialHeader.copy(
                           startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1), ZoneId.systemDefault()),
                           firstRiderStartOffset = 0,
                           interval = 3,
                           status = TimeTrialStatus.IN_PROGRESS
                   ))
                   timeTrialRepository.insertOrUpdate(newTt)
                   //callback()
               }


        }
    }

    fun insertSetupTt(){
        viewModelScope.launch(Dispatchers.IO) {

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



    fun insertFinishedTt(){
          viewModelScope.launch (Dispatchers.IO) {

                medTimeTrial.value?.let {
                    val newTt = it.copy(timeTrialHeader = it.timeTrialHeader.copy(
                            startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).minusSeconds(180), ZoneId.systemDefault()),
                            firstRiderStartOffset = 0,
                            interval = 10,
                            laps = 15,
                            status = TimeTrialStatus.FINISHED
                    ))
                    val withEvents = addFakeEvents(newTt)
                    timeTrialRepository.insertOrUpdate(withEvents)

                }

        }
    }

    fun addFakeEvents(timeTrial: TimeTrial): TimeTrial{
        val events = mutableListOf<TimeTrialEvent>()
        timeTrial.helper.riderStartTimes.forEach {
            events.add(TimeTrialEvent(timeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key, EventType.RIDER_STARTED))


            for(i in 1..timeTrial.timeTrialHeader.laps){
                events.add(TimeTrialEvent(timeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key + 5000 * i * Random.nextLong(5, 10), EventType.RIDER_PASSED))
            }


//                var i = 0
//                while(i < timeTrial.timeTrialHeader.laps){
//                    events.add(TimeTrialEvent(timeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key + 500 + i * 333, EventType.RIDER_PASSED))
//                    i++
//                }

        }
        return timeTrial.copy(eventList = events)
    }

}