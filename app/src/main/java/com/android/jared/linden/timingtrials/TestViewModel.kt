package com.android.jared.linden.timingtrials

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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
import kotlin.random.Random

class TestViewModel@Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository
) : ViewModel(){


    val timeTt = timeTrialRepository.getTimingTimeTrial()

    val newId = MutableLiveData<Long>()

    fun insertTimingTt(){
        viewModelScope.launch(Dispatchers.IO) {

            val exist = timeTrialRepository.getTimeTrialByName("Timing Timetrial")
            if(exist == null){
                val rList = riderRepository.allRidersLightSuspend()
                val courses = courseRepository.getAllCoursesSuspend()

                val timeTrial = TimeTrial.createBlank().copy(timeTrialHeader = TimeTrialHeader.createBlank()
                        .copy(ttName = "Timing Timetrial",
                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
                                firstRiderStartOffset = 0,
                                interval = 2,
                                course = courses[2],
                                laps = 2,
                                status = TimeTrialStatus.IN_PROGRESS))



                val newTt = timeTrial.helper.addRidersAsTimeTrialRiders(rList)

                timeTrialRepository.insertOrUpdate(newTt)
            }


        }
    }

    fun insertSetupTt(){
        viewModelScope.launch(Dispatchers.IO) {

            val exist = timeTrialRepository.getTimeTrialByName("Setup Timetrial")
            if(exist == null) {
                val rList = riderRepository.allRidersLightSuspend()
                val courses = courseRepository.getAllCoursesSuspend()

                val timeTrial = TimeTrial.createBlank().copy(timeTrialHeader = TimeTrialHeader.createBlank()
                        .copy(ttName = "Setup Timetrial",
                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
                                firstRiderStartOffset = 0,
                                interval = 2,
                                course = courses[2],
                                laps = 2,
                                status = TimeTrialStatus.SETTING_UP))
                val newTt = timeTrial.helper.addRidersAsTimeTrialRiders(rList)
                val i = timeTrialRepository.insertOrUpdate(newTt)
                newId.postValue(i)
            }

                }


        }




    fun insertFinishedTt(){
        viewModelScope.launch(Dispatchers.IO) {

            val exist = timeTrialRepository.getTimeTrialByName("Result Timetrial")
            if(exist == null) {
                val rList = riderRepository.allRidersLightSuspend()
                val courses = courseRepository.getAllCoursesSuspend()

                val timeTrial = TimeTrial.createBlank().copy(timeTrialHeader = TimeTrialHeader.createBlank()
                        .copy(ttName = "Result Timetrial",
                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
                                firstRiderStartOffset = 0,
                                interval = 2,
                                course = courses[2],
                                laps = 3,
                                status = TimeTrialStatus.FINISHED))


                val newTt = timeTrial.helper.addRidersAsTimeTrialRiders(rList)
                val withEvents = addFakeEvents(newTt)
                val i = timeTrialRepository.insertOrUpdate(withEvents)
                newId.postValue(i)
            }



        }
    }

    fun addFakeEvents(timeTrial: TimeTrial): TimeTrial{
        val events = mutableListOf<RiderPassedEvent>()
        timeTrial.helper.riderStartTimes.forEach {
            //events.add(RiderPassedEvent(timeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key))


            for(i in 1..timeTrial.timeTrialHeader.laps){
                events.add(RiderPassedEvent(timeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key + 5000 * i * Random.nextLong(5, 10)))
            }


//                var i = 0
//                while(i < setupTimeTrial.timeTrialHeader.laps){
//                    events.add(RiderPassedEvent(setupTimeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key + 500 + i * 333, EventType.RIDER_PASSED))
//                    i++
//                }

        }
        return timeTrial.copy(eventList = events)
    }

}