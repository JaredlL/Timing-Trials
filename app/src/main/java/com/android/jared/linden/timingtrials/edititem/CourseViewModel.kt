package com.android.jared.linden.timingtrials.edititem

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.ICourseRepository
import com.android.jared.linden.timingtrials.util.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject


class CourseViewModel @Inject constructor(private val repository: ICourseRepository, courseId: Long) : ObservableViewModel() {
    val course: LiveData<Course> = repository.getCourse(courseId)

    val mutableCourse: MediatorLiveData<Course> = MediatorLiveData()
    val mutableLengthString: MediatorLiveData<String> = MediatorLiveData()

    private val distances = listOf(
            DistanceViewModel("Miles", (1 / 1609.34)),
            DistanceViewModel("KM", (1 / 1000.0))
    )

    private fun updateLengthString(course: Course){
        if(course.length > 0){
            mutableLengthString.value = BigDecimal((course.length * distances[selectedItemPosition].conversion )).setScale(3, RoundingMode.HALF_EVEN).toString()
        }else{
            mutableLengthString.value = ""
        }
    }

    val lengthUnits = distances.map { d -> d.name }

    var selectedItemPosition = 0
    set(value){
        field = value
        mutableCourse.value?.let { updateLengthString(it)}
    }

    init{
        mutableCourse.addSource(course){result: Course -> result.let { mutableCourse.value = result }}
        mutableLengthString.addSource(mutableCourse){result -> result.let { updateLengthString(result) }}
    }


    fun addOrUpdate(){
        viewModelScope.launch(Dispatchers.IO) {

            mutableCourse.value?.let {
                mutableLengthString.value?.toDoubleOrNull()?.apply {
                    it.length = this / distances[selectedItemPosition].conversion
                }
                repository.insertOrUpdate(it)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    inner class DistanceViewModel(val name: String, val conversion: Double)
}