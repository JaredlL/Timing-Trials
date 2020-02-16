package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.ui.SelectableCourseViewModel
import com.android.jared.linden.timingtrials.util.ConverterUtils
import com.android.jared.linden.timingtrials.util.Event
import com.android.jared.linden.timingtrials.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class ListViewModel @Inject constructor(private val courseRepository: ICourseRepository,
                                        private val riderRepository: IRiderRepository,
                                        val timeTrialRepository: ITimeTrialRepository) : ViewModel() {


    val filterString: MutableLiveData<String> = MutableLiveData("")

    fun setFilterString(fString: String){
        filterString.value = fString
    }

    val allCourses: LiveData<List<SelectableCourseViewModel>> = Transformations.map(courseRepository.allCoursesLight){c -> c.map { SelectableCourseViewModel(it)}}
    val allRiders = riderRepository.allRidersLight
    val allTimeTrials = Transformations.map(timeTrialRepository.allTimeTrialsHeader){tt->
        tt.sortedByDescending { it.startTime }
    }

    val filteredAllCourse = Transformations.switchMap(filterString){filterStringVal->
        if(filterStringVal.isNullOrBlank()){
            allCourses
        }else{
            Transformations.map(allCourses){res->
                res?.let {
                    res.filter {courseVm->
                        courseVm.course.courseName.contains(filterStringVal, ignoreCase = true) || courseVm.course.cttName.contains(filterStringVal, ignoreCase = true) ||  courseVm.course.length.toString().contains(filterStringVal, ignoreCase = true)
                    }
                }?: res
            }
        }
    }

    val filteredAllRiders = Transformations.switchMap(filterString){filterStringVal->
        if(filterStringVal.isNullOrBlank()){
            allRiders
        }else{
            Transformations.map(allRiders){res->
                res?.filter { riderVm->
                    riderVm.fullName().contains(filterStringVal, ignoreCase = true) || riderVm.club.contains(filterStringVal, ignoreCase = true)
                } ?: res

            }
        }
    }

    val filteredAllTimeTrials = Transformations.switchMap(filterString){filterStringVal->
        if(filterStringVal.isNullOrBlank()){
            allTimeTrials
        }else{
            Transformations.map(allTimeTrials){res->
                res?.let {
                    res.filter {ttVm->
                        ttVm.ttName.contains(filterStringVal, ignoreCase = true) || ConverterUtils.dateToDisplay(ttVm.startTime).contains(filterStringVal, ignoreCase = true)
                    }
                }?: res
            }
        }
    }

    private val _mTimeTrialInsertedEvent: MutableLiveData<Event<Long>> = MutableLiveData()

    val timeTrialInsertedEvent: LiveData<Event<Long>> = _mTimeTrialInsertedEvent


    fun insertNewTimeTrial(){
        val newHeader = TimeTrialHeader.createBlank()
        viewModelScope.launch(Dispatchers.IO) {
            val newId = timeTrialRepository.insertNewHeader(newHeader)
            _mTimeTrialInsertedEvent.postValue(Event(newId))
        }
    }

}

