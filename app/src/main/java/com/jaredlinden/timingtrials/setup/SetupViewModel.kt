package com.jaredlinden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialRiderResult
import com.jaredlinden.timingtrials.data.roomrepo.ICourseRepository
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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

    private val timeTrialUpdateChannel = Channel<TimeTrial>(Channel.CONFLATED)

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
                if (ordered != current) {
                    _mTimeTrial.value = ordered
                }
            }
        }

        // Launch a collector for the channel in the ViewModel's scope
        timeTrialUpdateChannel.receiveAsFlow()
            .onEach { ttToUpdate ->
                Timber.d("Processing TT update from channel. ID: ${ttToUpdate.timeTrialHeader.id}, Riders: ${ttToUpdate.riderList.size}")
                try {
                    timeTrialRepository.updateFull(ttToUpdate)
                    Timber.d("Successfully updated TT from channel. ID: ${ttToUpdate.timeTrialHeader.id}")
                } catch (e: Exception) {
                    Timber.e(e, "Error updating TimeTrial (ID: ${ttToUpdate.timeTrialHeader.id}) from channel")
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateTimeTrial(newTimeTrial: TimeTrial) {

        // Update LiveData immediately for UI responsiveness
        val orderedNewTimeTrial = newTimeTrial.copy(riderList = newTimeTrial.riderList.sortedBy { it.timeTrialData.index })
        _mTimeTrial.value = orderedNewTimeTrial

        // Send to the channel for background processing (CONFLATED always accepts).
        val offerResult = timeTrialUpdateChannel.trySend(orderedNewTimeTrial)
        if (offerResult.isSuccess) {
            Timber.d("TT update (ID: ${orderedNewTimeTrial.timeTrialHeader.id}) successfully sent to channel.")
        } else {
            Timber.w("Failed to send TT update (ID: ${orderedNewTimeTrial.timeTrialHeader.id}) to channel. Closed: ${offerResult.isClosed}, Failed: ${offerResult.isFailure}")
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

    private fun calculateSD(numArray: List<Long>): Double {
        val mean = numArray.average()
        val meanSquareDifsAverage = numArray.map { (it - mean).pow(2) }.average()
        return sqrt(meanSquareDifsAverage)
    }

    private fun getCourseLap(timeTrialRider: TimeTrialRiderResult): CourseLap?{
        return CourseLap(timeTrialRider.course.id, timeTrialRider.course.courseName, timeTrialRider.laps)
    }

    private fun getAverageCourseTime(riderId: Long?, courseLap: CourseLap, allResults: List<TimeTrialRiderResult>):Double?{
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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
data class CourseLap(val courseId: Long, val name: String, val laps:Int)