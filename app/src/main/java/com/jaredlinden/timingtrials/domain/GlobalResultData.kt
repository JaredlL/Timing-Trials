package com.jaredlinden.timingtrials.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.roomrepo.*
import com.jaredlinden.timingtrials.ui.GenericListItemField
import com.jaredlinden.timingtrials.ui.GenericListItemNext
import com.jaredlinden.timingtrials.ui.IGenericListItem
import com.jaredlinden.timingtrials.util.ConverterUtils

data class GlobalResultData(
        val title: String,
        val resultHeading: IGenericListItem,
        val resultsList: List<IGenericListItem>)

interface IResultDataFactory{

    fun isValidForData(itemType: String): Boolean
    fun getTitle(itemId: Long): LiveData<String>
    fun getHeading(): IGenericListItem
    fun getResultList(itemId: Long): LiveData<List<IGenericListItem>>

}

data class SimpleListItem(override val item1: GenericListItemField,override val item2: GenericListItemField,override val item3: GenericListItemField): IGenericListItem

class ResultDataSource(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository, private val courseRepository: ICourseRepository, private val timeTrialRiderRepository: TimeTrialRiderRepository){

    private val riderResultFact = RiderResultDataFactory(riderRepository, timeTrialRiderRepository)
    private val courseResultfact = CourseResultDataFactory(courseRepository, timeTrialRiderRepository)

    private val factList: List<IResultDataFactory> = listOf(riderResultFact, courseResultfact)




    fun getResutTitle(typeIdData: GenericListItemNext): LiveData<String>{
         typeIdData.nextId?.let { id->
            factList.firstOrNull { it.isValidForData(typeIdData.itemType) }?.let {fact->
               return fact.getTitle(id)
            }
        }
        return MutableLiveData("")
    }

    fun getResultList(typeIdData: GenericListItemNext): LiveData<List<IGenericListItem>>{
        typeIdData.nextId?.let { id->
            factList.firstOrNull { it.isValidForData(typeIdData.itemType) }?.let {fact->
                return fact.getResultList(id)
            }
        }
        return MutableLiveData(listOf())
    }

    fun getHeading(typeIdData: GenericListItemNext):IGenericListItem{

            factList.firstOrNull { it.isValidForData(typeIdData.itemType) }?.let {fact->
                return fact.getHeading()
            }
        val i1 = GenericListItemField("")
        val i2 = GenericListItemField("")
        val i3 = GenericListItemField("")
        return  SimpleListItem(i1, i2, i3)
    }

}

class RiderResultDataFactory(private val riderRepository: IRiderRepository, private val timeTrialRiderRepository: TimeTrialRiderRepository): IResultDataFactory{

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
        val i1 = GenericListItemField("Course")
        val i2 = GenericListItemField("Date")
        val i3 = GenericListItemField("Time")
        return  SimpleListItem(i1, i2, i3)
    }

    override fun getResultList(itemId: Long): LiveData<List<IGenericListItem>> {
        return Transformations.map(timeTrialRiderRepository.getRiderResults(itemId)){res->
            res?.let{ list ->
                list.mapNotNull {r-> r.resultTime?.let{ listItemForRiderResult(r) } }
            }
        }
    }

    private fun listItemForRiderResult(result: IResult):IGenericListItem{

        val i1 = if(result.laps == 1){
            GenericListItemField(text = result.course.courseName, next = GenericListItemNext(Course::class.java.simpleName, result.course.id))
        }else{
            GenericListItemField(text = "${result.course.courseName} (x${result.laps})", next = GenericListItemNext(Course::class.java.simpleName, result.course.id))
        }
        val i2 = GenericListItemField(text = result.dateSet?.let { ds-> ConverterUtils.dateToDisplay(ds) } ?: "", next = GenericListItemNext(TimeTrial::class.java.simpleName, result.timeTrial?.id))
        val i3 = GenericListItemField(text = ConverterUtils.toSecondsDisplayString(result.resultTime))

        return  SimpleListItem(i1, i2, i3)

    }
}

class CourseResultDataFactory(private val courseRepository: ICourseRepository, private val timeTrialRiderRepository: TimeTrialRiderRepository): IResultDataFactory{

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

        val i1 = GenericListItemField("Rider")
        val i2 = GenericListItemField("Date")
        val i3 = GenericListItemField("Time")
        return  SimpleListItem(i1, i2, i3)
    }

    override fun getResultList(itemId: Long): LiveData<List<IGenericListItem>> {
        return Transformations.map(timeTrialRiderRepository.getCourseResults(itemId)){res->
            res?.let{ list ->
                list.mapNotNull {r-> r.resultTime?.let{ listItemForCourseResult(r) } }
                //list.filter { it.resultTime != null }.map { listItemForCourseResult(it) }

            }
        }
    }

    //RiderName, Date, Time
    private fun listItemForCourseResult(result: IResult):IGenericListItem{

        val i1 = GenericListItemField(text = result.rider.fullName(), next = GenericListItemNext(Rider::class.java.simpleName, result.rider.id))
        val i2 = GenericListItemField(text = result.dateSet?.let { ds-> ConverterUtils.dateToDisplay(ds) } ?: "", next = GenericListItemNext(TimeTrial::class.java.simpleName, result.timeTrial?.id))
        val i3 = GenericListItemField(text = ConverterUtils.toSecondsDisplayString(result.resultTime))

        return  SimpleListItem(i1, i2, i3)
    }
}




