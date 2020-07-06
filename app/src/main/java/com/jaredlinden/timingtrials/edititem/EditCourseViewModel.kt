package com.jaredlinden.timingtrials.edititem

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.data.roomrepo.ICourseRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.util.Event
import com.jaredlinden.timingtrials.util.LengthConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


class EditCourseViewModel @Inject constructor(private val repository: ICourseRepository, private val results: TimeTrialRiderRepository) :ViewModel() {

    val mutableCourse: MediatorLiveData<Course> = MediatorLiveData()
    val mutableLengthString: MutableLiveData<String> = MutableLiveData()
    val courseName = MutableLiveData<String>("")
    val cttName = MutableLiveData<String>("")

    val liveLengthConverter: MutableLiveData<LengthConverter> = MutableLiveData()

    fun setLengthConverter(value: LengthConverter){
        liveLengthConverter.value = value
        selectedItemPosition = lengthUnits.indexOf(value.getUnitName())
    }

    val statsString = Transformations.switchMap(mutableCourse){
        it?.id?.let {
            results.getCourseResults(it)
        }?:MutableLiveData(listOf())
    }.map {res->
        if(res!= null && res.isNotEmpty()){
            "Rides: ${res.size}"
        }else{
            ""
        }
    }

    val doJumpToCourseResults: MutableLiveData<Event<Long>> = MutableLiveData()
    fun jumpToCourseResults(){
        mutableCourse.value?.id?.let { doJumpToCourseResults.value = Event(it) }
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



    val lengthUnits = LengthConverter.unitList.map { it.name }

    private fun updateLengthString(newLength: Double){
        if(newLength > 0){
            mutableLengthString.value = "%2.4f".format(liveLengthConverter.value?.convert(newLength))
        }else{
            mutableLengthString.value = "0.000"
        }
    }




    var selectedItemPosition = 0
    val converstions = LengthConverter.unitList.map { it.conversion }

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

                val existing = repository.getCoursesByName(trimmed.courseName)
                if(existing.isNotEmpty() && existing.first().id != trimmed.id){
                    message.postValue(Event(R.string.error_course_exists_with_name))
                }else{
                    if(trimmed.id?:0L == 0L){
                        repository.insert(trimmed)
                    }else{
                        repository.update(trimmed)
                    }
                    updateSuccess.postValue(Event(true))
                }

                //repository.insertOrUpdate(trimmed)
            }
        }
    }

    val message : MutableLiveData<Event<Int>> = MutableLiveData()
    val updateSuccess : MutableLiveData<Event<Boolean>> = MutableLiveData()

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    inner class DistanceViewModel(val name: String, val conversion: Double)
}