package com.android.jared.linden.timingtrials.globalresults

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.ui.IGenericListItem
import com.android.jared.linden.timingtrials.util.ConverterUtils
import javax.inject.Inject

class GlobalResultViewModel @Inject constructor(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository,private val courseRepository: ICourseRepository) : ViewModel() {



    private val typeIdLiveData:MutableLiveData<Pair<String, Long>>  = MutableLiveData()

    val resultsToDisplay: LiveData<List<IGenericListItem>> = Transformations.switchMap(typeIdLiveData){listInfo ->

        var retList: LiveData<List<IGenericListItem>> = MutableLiveData()
        listInfo?.let {li->
            when(li.first){
                ITEM_RIDER->{
                    retList = Transformations.map(riderRepository.getRider(li.second)){ri ->
                        ri?.let {
                            it.personalBests.map {pb -> listItemFromPb(pb) }
                        }}
                }
                ITEM_COURSE->{
                    retList = Transformations.map(courseRepository.getCourse(li.second)){c ->
                        c?.let {
                            c.courseRecords.map {cr -> listItemFromCourseRecord(cr) }
                        }}
                }
            }
        }
        retList
    }


    fun init(itemType: String, id: Long){
        typeIdLiveData.value = Pair(itemType, id)
    }

    private fun listItemFromPb(pb: PersonalBest):IGenericListItem{
        return object :IGenericListItem{
            override val itemText1: String
                get() = pb.courseName

            override val itemText2: String
                get() = pb.dateTime?.let { ConverterUtils.dateToDisplay(pb.dateTime) } ?: ""

            override val itemText3: String
                get() = ConverterUtils.toSecondsDisplayString(pb.millisTime)
        }
    }

    private fun listItemFromCourseRecord(cr: CourseRecord):IGenericListItem{
        return object :IGenericListItem{
            override val itemText1: String
                get() = cr.riderName

            override val itemText2: String
                get() = cr.category.readableName()

            override val itemText3: String
                get() = ConverterUtils.toSecondsDisplayString(cr.timeMillis)
        }
    }
}