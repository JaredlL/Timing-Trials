package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.domain.Filter
import com.android.jared.linden.timingtrials.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class ListViewModel @Inject constructor(private val courseRepository: ICourseRepository,
                                        private val riderRepository: IRiderRepository,
                                        val timeTrialRepository: ITimeTrialRepository) : ViewModel() {


    val liveFilter: MutableLiveData<Filter> = MutableLiveData()

    fun setFilter(filterVal: Filter){
        liveFilter.value = filterVal
    }

    val allCourses = courseRepository.allCoursesLight
    val allRiders = riderRepository.allRidersLight
    val allTimeTrials = Transformations.map(timeTrialRepository.allTimeTrialsHeader){tt->
        tt.sortedByDescending { it.startTime }
    }

    val filteredAllCourse = Transformations.switchMap(liveFilter){ filterVal->
        if(filterVal == null){
            allCourses
        }else{
            Transformations.map(allCourses){res->
                res?.let {
                    res.filter {courseVm->
                        filterVal.passes(courseVm)
                    }
                }?: res
            }
        }
    }

    val filteredAllRiders = Transformations.switchMap(liveFilter){ filterVal->
        if(filterVal == null){
            allRiders
        }else{
            Transformations.map(allRiders){res->
                res?.filter { riderVm->
                    filterVal.passes(riderVm)
                } ?: res

            }
        }
    }

    val filteredAllTimeTrials = Transformations.switchMap(liveFilter){ filterVal->
        if(filterVal == null){
            allTimeTrials
        }else{
            Transformations.map(allTimeTrials){res->
                res?.let {
                    res.filter {ttVm->
                        filterVal.passes(ttVm)
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

