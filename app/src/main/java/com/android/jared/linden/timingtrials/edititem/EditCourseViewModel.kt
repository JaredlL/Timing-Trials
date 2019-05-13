package com.android.jared.linden.timingtrials.edititem

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.util.createLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject


class EditCourseViewModel @Inject constructor(private val repository: ICourseRepository) :ViewModel() {

    val mutableCourse: MediatorLiveData<Course> = MediatorLiveData()
    val mutableLengthString: MutableLiveData<String> = MutableLiveData()



    private val distances = listOf(
            DistanceViewModel("Miles", (1 / 1609.34)),
            DistanceViewModel("KM", (1 / 1000.0))
    )

    val lengthUnits = distances.map { d -> d.name }

    private fun updateLengthString(newLength: Double){
        if(newLength > 0){
            mutableLengthString.value = BigDecimal((newLength * distances[selectedItemPosition].conversion )).setScale(3, RoundingMode.HALF_EVEN).toString()
        }else{
            mutableLengthString.value = "0.000"
        }
    }

    val courseName = MutableLiveData<String>("").createLink(
            mutableCourse,
            {new -> mutableCourse.value?.let { Pair(it.courseName, it.copy(courseName = new)) }},
            {mutableCourse.value?.courseName?:"" })

    val cttName = MutableLiveData<String>("").createLink(
            mutableCourse,
            {new -> mutableCourse.value?.let { Pair(it.cttName, it.copy(cttName = new)) }},
            {mutableCourse.value?.cttName?:"" })


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
                }
            }
        }

    }





    fun addOrUpdate(){
        viewModelScope.launch(Dispatchers.IO) {

            mutableCourse.value?.let {

                val len = mutableLengthString.value?.toDoubleOrNull()?.apply {
                    this / distances[selectedItemPosition].conversion
                }?:0.0

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