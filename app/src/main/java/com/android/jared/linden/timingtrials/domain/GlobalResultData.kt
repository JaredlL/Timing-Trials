package com.android.jared.linden.timingtrials.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.IResult
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IGlobalResultRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.ui.IGenericListItem
import com.android.jared.linden.timingtrials.util.ConverterUtils

data class GlobalResultData(
    val title: String = "",
    val resultHeading: IGenericListItem = SimpleListItem(),
    val resultsList: List<IGenericListItem> = listOf())

interface IResultDataFactory{

    fun isValidForData(itemType: String): Boolean
    fun getTitle(itemId: Long): LiveData<String>
    fun getHeading(): IGenericListItem
    fun getResultList(itemId: Long): LiveData<List<IGenericListItem>>

}

data class SimpleListItem(val item1: String = "", val item2: String = "", val item3: String = ""): IGenericListItem{
    override val itemText1: String
        get() = item1

    override val itemText2: String
        get() = item2

    override val itemText3: String
        get() = item3
}

class ResultDataSource(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository, private val courseRepository: ICourseRepository, private val globalResultRepository: IGlobalResultRepository){

    private val riderResultFact = RiderResultDataFactory(riderRepository, globalResultRepository)
    private val courseResultfact = CourseResultDataFactory(courseRepository, globalResultRepository)

    private val factList: List<IResultDataFactory> = listOf(riderResultFact, courseResultfact)


    fun getResultData(typeIdData: Pair<String, Long>): LiveData<GlobalResultData>{
        val resultData = MediatorLiveData<GlobalResultData>()
        val id = typeIdData.second
        factList.firstOrNull { it.isValidForData(typeIdData.first) }?.let {fact->
            resultData.value = GlobalResultData(resultHeading = fact.getHeading())
            resultData.addSource(fact.getTitle(id)){res->
                res?.let {str->
                    resultData.value = resultData.value?.copy(title = str)
                }
            }
            resultData.addSource(fact.getResultList(id)){res->
                res?.let {lst->
                    resultData.value = resultData.value?.copy(resultsList = lst)
                }
            }
        }
        return  resultData

    }

}

class RiderResultDataFactory(private val riderRepository: IRiderRepository, private val globalResultRepository: IGlobalResultRepository): IResultDataFactory{

    override fun isValidForData(itemType: String): Boolean {
        return itemType == Rider::class.java.simpleName
    }

    override fun getTitle(itemId: Long): LiveData<String> {
        return Transformations.map(riderRepository.getRider(itemId)){res->
            res?.let {
                "${it.firstName} ${it.lastName} Results"
            }
        }
    }
    override fun getHeading():IGenericListItem{
        return  SimpleListItem("Course", "Date", "Time")
    }

    override fun getResultList(itemId: Long): LiveData<List<IGenericListItem>> {
        return Transformations.map(globalResultRepository.getRiderResults(itemId)){res->
            res?.let{ list ->
                list.map { listItemForRiderResult(it) }
            }
        }
    }

    private fun listItemForRiderResult(result: IResult):IGenericListItem{
        return  SimpleListItem(result.course.courseName,
                result.dateSet?.let { ds-> ConverterUtils.dateToDisplay(ds) } ?: "",
                ConverterUtils.toSecondsDisplayString(result.resultTime))

    }
}

class CourseResultDataFactory(private val courseRepository: ICourseRepository, private val globalResultRepository: IGlobalResultRepository): IResultDataFactory{

    override fun isValidForData(itemType: String): Boolean {
        return itemType == Course::class.java.simpleName
    }

    //EG Lydbrook 10 Results
    override fun getTitle(itemId: Long): LiveData<String> {
        return Transformations.map(courseRepository.getCourse(itemId)){res->
            res?.let {
                "${it.courseName} Results"
            }
        }
    }

    //EG Rider, Date, Time
    override fun getHeading():IGenericListItem{
        return  SimpleListItem("Rider", "Date", "Time")
    }

    override fun getResultList(itemId: Long): LiveData<List<IGenericListItem>> {
        return Transformations.map(globalResultRepository.getCourseResults(itemId)){res->
            res?.let{ list ->
                list.map { listItemForCourseResult(it) }
            }
        }
    }

    //RiderName, Date, Time
    private fun listItemForCourseResult(result: IResult):IGenericListItem{
        return SimpleListItem(result.rider.fullName(),
                result.dateSet?.let { ConverterUtils.dateToDisplay(it) } ?: "",
                ConverterUtils.toSecondsDisplayString(result.resultTime))
    }
}




