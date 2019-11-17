package com.android.jared.linden.timingtrials

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.source.TimingTrialsDatabase
import kotlinx.coroutines.*
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

class TestViewModel@Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository,
        val timingTrialsDatabase: TimingTrialsDatabase
) : ViewModel(){





    // region Test methods




    val nonFinishedTt = timeTrialRepository.nonFinishedTimeTrial

    fun insertTimingTt(){

        timeTrialRepository.nonFinishedTimeTrial.observeForever{tt->
            tt?.let {dbTt->
                viewModelScope.launch(Dispatchers.IO) {

                        val rList = riderRepository.allRidersLightSuspend()
                        val courses = courseRepository.getAllCoursesSuspend()

                        val timeTrial = dbTt.copy(timeTrialHeader = dbTt.timeTrialHeader.copy(
                                ttName = "Timing Timetrial",
                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(5), ZoneId.systemDefault()),
                                firstRiderStartOffset = 0,
                                interval = 2,
                                course = courses[2],
                                laps = 2,
                                status = TimeTrialStatus.IN_PROGRESS))


                        val mod = timeTrial.helper.addRidersAsTimeTrialRiders(rList.filterIndexed { index, _ -> index % 10 == 0 })
                        timeTrialRepository.update(mod)
                    launch(Dispatchers.Main){
                        blobs()
                    }
                    }

                }
            }

        }




    private val _inserted: MutableLiveData<Boolean> = MutableLiveData(false)
    val  inserted: LiveData<Boolean> = _inserted


    fun onInsertionAction(){
        _inserted.value = false
        timeTrialRepository.nonFinishedTimeTrial.removeObserver(obs)

    }

    fun blobs(){
        timeTrialRepository.nonFinishedTimeTrial.removeObserver(obs)
    }

    fun insertSetupTt(){

        timingTrialsDatabase.mDbIsPopulated.observeForever{
            if(it){
                timeTrialRepository.nonFinishedTimeTrial.observeForever(obs)
            }
        }


        }

    val obs = object :Observer<TimeTrial?>{
        override fun onChanged(t: TimeTrial?) {
            t?.let {exist->
                viewModelScope.launch(Dispatchers.IO) {

                    launch(Dispatchers.Main){
                        blobs()
                    }
                    if( timingTrialsDatabase.mDbIsPopulated.value == true){
                        System.out.println("JAREDMSG - Get riders for test VM")

                        val rList = riderRepository.allRidersLightSuspend()
                        val courses = courseRepository.getAllCoursesSuspend()
                        System.out.println("JAREDMSG - Test VM Rider Count = ${rList.count()}")
                        val timeTrial = exist.copy(timeTrialHeader = exist.timeTrialHeader
                                .copy(ttName = "Setup Timetrial",
                                        startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
                                        firstRiderStartOffset = 0,
                                        interval = 2,
                                        course = courses[3],
                                        laps = 2,
                                        status = TimeTrialStatus.SETTING_UP))
                        val newTt = timeTrial.helper.addRidersAsTimeTrialRiders(rList)
                        withContext(Dispatchers.IO){
                            timeTrialRepository.update(newTt)
                            _inserted.postValue(true)

                        }

                    }

                }
            }

        }
    }



//    fun insertFinishedTt(){
//
//        viewModelScope.launch(Dispatchers.IO) {
//
//            val exist = timeTrialRepository.getNonFinishedTimeTrialSuspend()
//            if(exist != null){
//                val rList = riderRepository.allRidersLightSuspend()
//                val courses = courseRepository.getAllCoursesSuspend()
//
//                val _mTimeTrial =exist.copy(timeTrialHeader = exist.timeTrialHeader
//                        .copy(ttName = "Result Timetrial",
//                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
//                                firstRiderStartOffset = 0,
//                                interval = 2,
//                                course = courses[2],
//                                laps = 4,
//                                status = TimeTrialStatus.FINISHED))
//
//
//                val newTt = _mTimeTrial.helper.addRidersAsTimeTrialRiders(rList)
//                val withEvents = addFakeEvents(newTt)
//                timeTrialRepository.update(withEvents)
//            }
//
//
//        }
//    }

//    fun insertFinishedTt2(){
//        viewModelScope.launch(Dispatchers.IO) {
//
//            val exist = timeTrialRepository.getTimeTrialByName("Test Result TT 1")
//            if(exist == null) {
//                val rList = riderRepository.allRidersLightSuspend()
//                val courses = courseRepository.getAllCoursesSuspend()
//
//                val _mTimeTrial = TimeTrial.createBlank().copy(timeTrialHeader = TimeTrialHeader.createBlank()
//                        .copy(ttName = "Test Result TT 1",
//                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
//                                firstRiderStartOffset = 0,
//                                interval = 2,
//                                course = courses[3],
//                                laps = 4,
//                                status = TimeTrialStatus.FINISHED))
//
//
//                val newTt = _mTimeTrial.helper.addRidersAsTimeTrialRiders(rList)
//                val withEvents = addFakeEvents2(newTt)
//                val i = timeTrialRepository.insertOrUpdate(withEvents)
//                newId.postValue(i)
//            }else{
//                newId.postValue(exist.timeTrialHeader.id)
//            }
//        }
//    }



    fun  addFakeEvents2(timeTrial: TimeTrial): TimeTrial{

        val events = mutableListOf<RiderPassedEvent>()
        timeTrial.helper.riderStartTimes.forEach {
            for (i in 1..timeTrial.timeTrialHeader.laps) {
                events.add(RiderPassedEvent(timeTrial.timeTrialHeader.id
                        ?: 0, it.value.rider.id, it.key + 500 + i * 3000))
            }
        }

        val newEvents = timeTrial.helper.riderStartTimes.asSequence().mapIndexed { index, entry ->
            RiderPassedEvent(timeTrial.timeTrialHeader.id ?: 0, entry.value.rider.id, entry.key + 500 + index * 3000)
        }
        return timeTrial.copy(eventList = newEvents.toList())
    }

    fun addFakeEvents(timeTrial: TimeTrial): TimeTrial{
        val events = mutableListOf<RiderPassedEvent>()
        timeTrial.helper.riderStartTimes.forEach {
            //events.add(RiderPassedEvent(_mTimeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key))


            var prev = it.key
            for(i in 1..timeTrial.timeTrialHeader.laps){
                prev += prev + Random.nextLong(5000, 10000)
                events.add(RiderPassedEvent(timeTrial.timeTrialHeader.id?:0, it.value.rider.id, prev))
            }


//                var i = 0
//                while(i < setupTimeTrial.timeTrialHeader.laps){
//                    events.add(RiderPassedEvent(setupTimeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key + 500 + i * 333, EventType.RIDER_PASSED))
//                    i++
//                }

        }
        return timeTrial.copy(eventList = events)
    }

    // endregion

}