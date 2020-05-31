package com.jaredlinden.timingtrials.resultexplorer

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrialRiderResult
import com.jaredlinden.timingtrials.data.roomrepo.*
import com.jaredlinden.timingtrials.domain.GlobalResultData
import com.jaredlinden.timingtrials.domain.ResultDataSource
import com.jaredlinden.timingtrials.ui.GenericListItemNext
import javax.inject.Inject

class GlobalResultViewModel @Inject constructor(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository,private val courseRepository: ICourseRepository, private val timeTrialRiderRepository: TimeTrialRiderRepository) : ViewModel() {



    val typeIdLiveData:MutableLiveData<GenericListItemNext>  = MutableLiveData()
    private val dataSource = ResultDataSource(timeTrialRepository, riderRepository, courseRepository, timeTrialRiderRepository)


    fun getRiderResultList(itemId: Long, itemTypeId: String): LiveData<List<TimeTrialRiderResult>> {
       return if(itemTypeId == Rider::class.java.simpleName){
            timeTrialRiderRepository.getRiderResults(itemId)
        }else{
            timeTrialRiderRepository.getCourseResults(itemId)
        }

    }


    fun getItemName(itemId: Long, itemTypeId: String) : LiveData<String?>{
        return if(itemTypeId == Rider::class.java.simpleName){
            Transformations.map(riderRepository.getRider(itemId)){it?.fullName()}
        }else{
           Transformations.map(courseRepository.getCourse(itemId)){ it?.courseName}
        }
    }

    fun setTypeIdData(next: GenericListItemNext){
        resMed.value = GlobalResultData("", dataSource.getHeading(next), listOf())
        typeIdLiveData.value = next
    }

    val resMed = MediatorLiveData<GlobalResultData>().apply {

    }

    init {
        resMed.addSource( Transformations.switchMap(typeIdLiveData){dataSource.getResultList(it)}){
           resMed.value = resMed.value?.copy(resultsList =  it)
        }
        resMed.addSource( Transformations.switchMap(typeIdLiveData){dataSource.getResutTitle(it)}){
            resMed.value = resMed.value?.copy(title =  it)
        }
    }



}