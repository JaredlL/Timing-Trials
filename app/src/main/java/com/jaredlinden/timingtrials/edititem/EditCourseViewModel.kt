package com.jaredlinden.timingtrials.edititem

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.data.roomrepo.ICourseRepository
import com.jaredlinden.timingtrials.util.LengthConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


class EditCourseViewModel @Inject constructor(private val repository: ICourseRepository) :ViewModel() {

    val mutableCourse: MediatorLiveData<Course> = MediatorLiveData()
    val mutableLengthString: MutableLiveData<String> = MutableLiveData()
    val courseName = MutableLiveData<String>("")
    val cttName = MutableLiveData<String>("")

    val liveLengthConverter: MutableLiveData<LengthConverter> = MutableLiveData()

    fun setLengthConverter(value: LengthConverter){
        liveLengthConverter.value = value
        selectedItemPosition = lengthUnits.indexOf(value.getUnitName())
    }

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



    val lengthUnits = LengthConverter.unitMap.values.map { it.first }

    private fun updateLengthString(newLength: Double){
        if(newLength > 0){
            mutableLengthString.value = "%2.4f".format(liveLengthConverter.value?.convert(newLength))
        }else{
            mutableLengthString.value = "0.000"
        }
    }




    var selectedItemPosition = 0
    val converstions = LengthConverter.unitMap.values.map { it.second }

    fun changeCourse(courseId: Long){
        if(currentId.value != courseId){
            currentId.value = courseId
        }
    }



    fun deleteCourse(){
        viewModelScope.launch(Dispatchers.IO) {

            mutableCourse.value?.let {
                repository.delete(it)
            }
        }
    }


    fun addOrUpdate(){
        viewModelScope.launch(Dispatchers.IO) {

            mutableCourse.value?.let {course->

                val len = mutableLengthString.value?.toDoubleOrNull()?.let {
                    it * converstions[selectedItemPosition]
                }?:0.0

                val trimmed = course.copy(
                        courseName = course.courseName.trim(),
                        length = len,
                        cttName = course.cttName.trim()
                )
                repository.insertOrUpdate(trimmed)
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