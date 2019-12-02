package com.android.jared.linden.timingtrials.edititem

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
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
    val courseName = MutableLiveData<String>("")
    val cttName = MutableLiveData<String>("")

    private val currentId = MutableLiveData<Long>(0L)

    init {
        mutableCourse.addSource(mutableCourse){
            it?.let { course->
                if(courseName.value != course.courseName){
                    courseName.value = course.courseName
                }
                if(cttName.value != course.cttName){
                    cttName.value = course.cttName
                }
            }
        }
        mutableCourse.addSource(courseName){res->
            res?.let { str->
                mutableCourse.value?.let { course->
                    if(course.courseName != str){
                        mutableCourse.value = course.copy(courseName = str)
                    }
                }
            }
        }
        mutableCourse.addSource(cttName){res->
            res?.let { str->
                mutableCourse.value?.let { course->
                    if(course.cttName != str){
                        mutableCourse.value = course.copy(cttName = str)
                    }
                }
            }
        }

        mutableCourse.addSource(Transformations.switchMap(currentId){
            if(it != mutableCourse.value?.id){
                repository.getCourse(it)
            }else{
                null
            }

        }){res->
            res?.let { course->
                if(mutableCourse.value != course){
                    mutableCourse.value = course
                    updateLengthString(course.length)
                }
            }
        }


    }


    private val distances = listOf(
            DistanceViewModel("Miles", (1 / 1609.34)),
            DistanceViewModel("KM", (1 / 1000.0))
    )

    val lengthUnits = distances.map { d -> d.name }

    private fun updateLengthString(newLength: Double){
        if(newLength > 0){
            mutableLengthString.value = BigDecimal((newLength * distances[selectedItemPosition].conversion)).setScale(3, RoundingMode.HALF_EVEN).toString()
        }else{
            mutableLengthString.value = "0.000"
        }
    }




    var selectedItemPosition = 0

    fun changeCourse(courseId: Long){
        if(currentId.value != courseId){
            currentId.value = courseId
        }
    }





    fun addOrUpdate(){
        viewModelScope.launch(Dispatchers.IO) {

            mutableCourse.value?.let {

                val len = mutableLengthString.value?.toDoubleOrNull()?.run {
                    this / distances[selectedItemPosition].conversion
                }?:0.0

                repository.insertOrUpdate(it.copy(length = len))
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