package com.android.jared.linden.timingtrials.globalresults

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.*
import com.android.jared.linden.timingtrials.domain.GlobalResultData
import com.android.jared.linden.timingtrials.domain.ResultDataSource
import com.android.jared.linden.timingtrials.ui.IGenericListItem
import com.android.jared.linden.timingtrials.util.ConverterUtils
import javax.inject.Inject

class GlobalResultViewModel @Inject constructor(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository,private val courseRepository: ICourseRepository, private val globalResultRepository: IGlobalResultRepository) : ViewModel() {



    private val typeIdLiveData:MutableLiveData<Pair<String, Long>>  = MutableLiveData()
    private val dataSource = ResultDataSource(timeTrialRepository, riderRepository, courseRepository, globalResultRepository)


    fun getResutData(itemType: String, id: Long): LiveData<GlobalResultData>{
        return dataSource.getResultData(Pair(itemType, id))
    }




//    val titleString: LiveData<String> = Transformations.switchMap(typeIdLiveData){a ->
//
//
//
//        a?.let { li->
//            when(li.first){
//                Rider::class.java.simpleName->{
//                    return@let Transformations.map(riderRepository.getRider(li.second)){
//                        "${it.firstName} ${it.lastName} Results"
//                    }
//                }
//                Course::class.java.simpleName->{
//                    return@let Transformations.map(courseRepository.getCourse(li.second)){
//                        "${it.courseName} Results"}
//                    }
//                else -> return@let MutableLiveData("")
//
//                }
//
//
//        }
//    }
//
//
//    val resultsToDisplay: LiveData<List<IGenericListItem>> = Transformations.switchMap(typeIdLiveData) { listInfo ->
//
//        var retList: LiveData<List<IGenericListItem>> = MutableLiveData()
//
//        if (listInfo != null) {
//            when (listInfo.first) {
//                Rider::class.java.simpleName -> {
//                    retList = Transformations.map(globalResultRepository.getRiderResults(listInfo.second)) { rl ->
//                        rl?.let { list -> list.map { listItemForRiderResult(it) } }
//                    }
//                }
//                Course::class.java.simpleName -> {
//                    retList = Transformations.map(globalResultRepository.getCourseResults(listInfo.second)) { rl ->
//                        rl?.let { list -> list.map { listItemForCourseResult(it) } }
//                    }
//                }
//                else -> {
//
//                }
//            }
//        }
//        retList
//    }



//
//    private fun listItemForRiderResult(result: IResult):IGenericListItem{
//        return object :IGenericListItem{
//            override val itemText1: String
//                get() = result.course.courseName
//
//            override val itemText2: String
//                get() = result.dateSet?.let { ds-> ConverterUtils.dateToDisplay(ds) } ?: ""
//
//            override val itemText3: String
//                get() = ConverterUtils.toSecondsDisplayString(result.resultTime)
//        }
//    }
//
//    private fun listItemForCourseResult(result: IResult):IGenericListItem{
//        return object :IGenericListItem{
//            override val itemText1: String
//                get() = result.rider.fullName()
//
//            override val itemText2: String
//                get() = "${result.dateSet?.let { ConverterUtils.dateToDisplay(it) } ?: ""} ${result.categoryString}"
//
//            override val itemText3: String
//                get() = ConverterUtils.toSecondsDisplayString(result.resultTime)
//        }
//    }
//
//    private fun listItemForRider(cr: CourseRecord):IGenericListItem{
//        return object :IGenericListItem{
//            override val itemText1: String
//                get() = cr.riderName
//
//            override val itemText2: String
//                get() = cr.category.readableName()
//
//            override val itemText3: String
//                get() = ConverterUtils.toSecondsDisplayString(cr.timeMillis)
//        }
//    }
}