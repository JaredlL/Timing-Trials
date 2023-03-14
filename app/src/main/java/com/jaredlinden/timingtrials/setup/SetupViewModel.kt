package com.jaredlinden.timingtrials.setup

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.roomrepo.ICourseRepository
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.util.ConverterUtils
import com.jaredlinden.timingtrials.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

interface ITimeTrialSetupViewModel {
    val orderRidersViewModel: IOrderRidersViewModel
    val selectCourseViewModel: ISelectCourseViewModel
    val selectRidersViewModel: ISelectRidersViewModel
    val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel
    val numberOptionsViewModel: NumberOptionsViewModel
}


@HiltViewModel
class SetupViewModel @Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository,
        val timeTrialRiderRepository: TimeTrialRiderRepository
) : ViewModel(), ITimeTrialSetupViewModel {


    private val _mTimeTrial = MediatorLiveData<TimeTrial?>()
    val timeTrial: LiveData<TimeTrial?> = _mTimeTrial.map{
        it
    }

    var currentPage = 0

    private val currentId: MutableLiveData<Long?> = MutableLiveData()

    private val idSwitcher = currentId.switchMap{
        it?.let {ttId->
            timeTrialRepository.getSetupTimeTrialById(ttId)
        }
    }

    fun changeTimeTrial(timeTrialId: Long){
        if(currentId.value != timeTrialId){
            currentId.value = timeTrialId
        }
    }

    override val numberOptionsViewModel: NumberOptionsViewModel = NumberOptionsViewModel(this)

    init {
        _mTimeTrial.addSource(idSwitcher) { res ->
            res?.let { tt ->
                val current = _mTimeTrial.value
                val ordered = tt.copy(riderList = tt.riderList.sortedBy { it.timeTrialData.index })
                if (!isCarolineAlive.get() && ordered != current) {
                    _mTimeTrial.value = ordered
                }
            }
        }
    }

    private val queue = ConcurrentLinkedQueue<TimeTrial>()
    private var isCarolineAlive = AtomicBoolean()

    fun updateTimeTrial(newTimeTrial: TimeTrial) {

        val previousTimeTrial = _mTimeTrial.value
        _mTimeTrial.value = newTimeTrial
        if (previousTimeTrial != null) {
            _mTimeTrial.value = newTimeTrial

            if (!isCarolineAlive.get()) {
                queue.add(newTimeTrial)
                viewModelScope.launch(Dispatchers.IO) {
                    isCarolineAlive.set(true)
                    while (queue.peek() != null) {
                        var ttToInsert = queue.peek()
                        while (queue.peek() != null) {
                            ttToInsert = queue.poll()
                        }
                        ttToInsert?.let { timeTrialRepository.updateFull(it) }

                    }
                    isCarolineAlive.set(false)
                }
            } else {
                queue.add(newTimeTrial)
            }
        }
    }

    fun seedRiders(){
        viewModelScope.launch(Dispatchers.IO) {
            _mTimeTrial.value?.let { tt->
                val allResult = timeTrialRiderRepository.getAllResultsSuspend()
                val courseLaps = allResult.mapNotNull { getCourseLap(it) }.distinct()

                val courseLapMeanSds = courseLaps.map {cl->
                    val resTimes = allResult.filter { getCourseLap(it) == cl }.mapNotNull { it.resultTime }
                    Triple(cl,resTimes.average(),calculateSD(resTimes))
                }

                val riderScores = tt.riderList.mapNotNull {ttr->
                  ttr.riderId()?.let {rId->
                      Pair(
                              ttr.riderData.firstName,
                              courseLapMeanSds.mapNotNull {meanSd->
                                  val riderMeanForCourse = getAverageCourseTime(rId, meanSd.first, allResult)

                                  if(riderMeanForCourse != null){
                                      (riderMeanForCourse - meanSd.second) / meanSd.third
                                  }else{
                                      null
                                  }
                      }.filter { !it.isNaN() }.average())
                  }
                }

                val sortedByScore = riderScores.sortedByDescending { it.second }.map { it.first }
                val newRiderList = tt.riderList.sortedBy { it.riderData.firstName.let { sortedByScore.indexOf(it) } }

                val newTt = tt.updateRiderList(newRiderList)
                _mTimeTrial.postValue(newTt)
            }
        }
    }

    fun calculateSD(numArray: List<Long>): Double {
        val mean = numArray.average()
        val meanSquareDifsAverage = numArray.map { (it - mean).pow(2) }.average()
        return sqrt(meanSquareDifsAverage)
    }

    fun getCourseLap(timeTrialRider: TimeTrialRiderResult): CourseLap?{
        return timeTrialRider.course.id?.let { CourseLap(it,timeTrialRider.course.courseName, timeTrialRider.laps) }
    }

    fun getAverageCourseTime(riderId: Long?, courseLap: CourseLap, allResults: List<TimeTrialRiderResult>):Double?{
        return if(riderId != null){
            allResults.asSequence().filter{it.riderData.id == riderId && courseLap == getCourseLap(it) && it.resultTime != null}.sortedByDescending { it.dateSet }.take(5).mapNotNull { it.resultTime }.average()
        }else{
            null
        }
    }

    override val orderRidersViewModel: IOrderRidersViewModel = OrderRidersViewModel(this)
    override val selectCourseViewModel: ISelectCourseViewModel = ISelectCourseViewModel.SelectCourseViewModelImpl(this)
    override val selectRidersViewModel: ISelectRidersViewModel = SelectRidersViewModelImpl(this)
    override val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel = TimeTrialPropertiesViewModelImpl(this)


    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        isCarolineAlive.set(false)
        viewModelScope.cancel()
    }
}
data class CourseLap(val courseId: Long, val name: String, val laps:Int)
data class RiderSeedHelper(val riderId: Long, val averagePos: Double)