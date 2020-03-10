package com.jaredlinden.timingtrials.globalresults

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.roomrepo.*
import com.jaredlinden.timingtrials.domain.GlobalResultData
import com.jaredlinden.timingtrials.domain.ResultDataSource
import com.jaredlinden.timingtrials.ui.GenericListItemNext
import javax.inject.Inject

class GlobalResultViewModel @Inject constructor(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository,private val courseRepository: ICourseRepository, private val timeTrialRiderRepository: TimeTrialRiderRepository) : ViewModel() {



    val typeIdLiveData:MutableLiveData<GenericListItemNext>  = MutableLiveData()
    private val dataSource = ResultDataSource(timeTrialRepository, riderRepository, courseRepository, timeTrialRiderRepository)





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