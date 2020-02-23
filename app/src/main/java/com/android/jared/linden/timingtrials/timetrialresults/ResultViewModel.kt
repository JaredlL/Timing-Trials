package com.android.jared.linden.timingtrials.timetrialresults

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.*
import com.android.jared.linden.timingtrials.util.ConverterUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ResultViewModel @Inject constructor(val timeTrialRepository: ITimeTrialRepository, val riderRepository: IRiderRepository, val courseRepository: ICourseRepository, val resultRepository: TimeTrialRiderRepository) : ViewModel() {


    private val idLiveData: MutableLiveData<Long?> = MutableLiveData()

    fun changeTimeTrial(newId: Long){
        if(idLiveData.value != newId){
            idLiveData.postValue(newId)
        }
    }

    val timeTrial = Transformations.switchMap(idLiveData){
        it?.let { id->
            timeTrialRepository.getResultTimeTrialById(id)
        }
    }


    val results = Transformations.map(timeTrial){tt->
        if(tt != null && tt.timeTrialHeader.status == TimeTrialStatus.FINISHED) {
            (sequenceOf(getHeading(tt)) + tt.helper.results.asSequence().map { res-> ResultRowViewModel(res, tt.timeTrialHeader.laps) }).toList()
        }else{
            null
        }

    }




    fun delete(){
        timeTrial.value?.let {
            viewModelScope.launch(Dispatchers.IO) {
                timeTrialRepository.delete(it)
            }

        }

    }

    fun getHeading(tt: TimeTrial): ResultRowViewModel{
        val mutList: MutableList<String> = mutableListOf()

        mutList.add("Rider Name")
        mutList.add("Total Time")
        mutList.add("Club")
        mutList.add("Category")
        mutList.add("Notes")
        if(tt.timeTrialHeader.laps > 1){
            for(i in 1..tt.timeTrialHeader.laps){
                mutList.add("Split $i")
            }
        }

        return ResultRowViewModel(mutList)
    }

}

class ResultRowViewModel{
    val row: MutableList<ResultCell> = mutableListOf()

    constructor(strings: List<String>){
        strings.forEach { row.add(ResultCell(MutableLiveData(it))) }
    }

    constructor(result: IResult, laps:Int)
     {
        row.add(ResultCell(MutableLiveData("${result.rider.firstName} ${result.rider.lastName}")))
         row.add(ResultCell(MutableLiveData(ConverterUtils.toTenthsDisplayString(result.resultTime))))
         row.add(ResultCell(MutableLiveData(result.riderClub)))
         row.add(ResultCell(MutableLiveData(result.category)))
         row.add(ResultCell(MutableLiveData(result.notes)))
         if(laps > 1){
             for (i in 0 until laps){
                 val splitVal = result.splits.getOrNull(i)
                 val splitString = if(splitVal != null) ConverterUtils.toTenthsDisplayString(splitVal) else ""
                 row.add(ResultCell(MutableLiveData(splitString)))
             }
         }



    }
}
class ResultCell(val content:LiveData<String>)
