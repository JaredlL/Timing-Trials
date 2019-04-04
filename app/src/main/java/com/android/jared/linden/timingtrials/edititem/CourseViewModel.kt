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


class CourseViewModel @Inject constructor(private val repository: ICourseRepository) : ObservableViewModel() {

    val mutableCourse: MediatorLiveData<Course> = MediatorLiveData()
    val mutableLengthString: MutableLiveData<String> = MutableLiveData()

    private val distances = listOf(
            DistanceViewModel("Miles", (1 / 1609.34)),
            DistanceViewModel("KM", (1 / 1000.0))
    )

    private fun updateLengthString(newLength: Double){
        if(newLength > 0){
            mutableLengthString.value = BigDecimal((newLength * distances[selectedItemPosition].conversion )).setScale(3, RoundingMode.HALF_EVEN).toString()
        }else{
            mutableLengthString.value = "0.000"
        }
    }

    val lengthUnits = distances.map { d -> d.name }

    var selectedItemPosition = 0
    set(value){
        val oldLength = mutableLengthString.value?.toDoubleOrNull()?.let { it / distances[selectedItemPosition].conversion}
        field = value
        oldLength?.let { updateLengthString(it) }
    }

    fun initialise(courseId: Long){
        if(mutableCourse.value == null){
            mutableCourse.addSource(repository.getCourse(courseId)){result: Course ->
                result.let {
                    mutableCourse.value = result
                    updateLengthString(result.length)
                }
            }
        }

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