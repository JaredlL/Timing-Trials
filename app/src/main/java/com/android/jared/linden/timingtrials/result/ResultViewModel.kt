package com.android.jared.linden.timingtrials.result

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialResult
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.data.roomrepo.*
import com.android.jared.linden.timingtrials.domain.RecordChecker
import com.android.jared.linden.timingtrials.util.ConverterUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ResultViewModel @Inject constructor(val timeTrialRepository: ITimeTrialRepository, val riderRepository: IRiderRepository, val courseRepository: ICourseRepository) : ViewModel() {
    // TODO: Implement the ViewModel


    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()


    val results = Transformations.map(timeTrial){tt->
        if(tt != null && tt.timeTrialHeader.status == TimeTrialStatus.FINISHED) {

//            val checker = RecordChecker(tt, riderRepository, courseRepository)
//            viewModelScope.launch(Dispatchers.IO) {
//                checker.checkRecords()
//            }
            (sequenceOf(getHeading(tt)) + tt.helper.results.asSequence().sortedBy { it.totalTime }.map { res-> ResultRowViewModel(res) }).toList()
        }else{
            null
        }

    }

    val resultSettings: MutableLiveData<ResultDisplaySettings> = MutableLiveData()

    fun initialise(timeTrialId: Long){
        if(timeTrial.value?.timeTrialHeader?.id != timeTrialId){
            timeTrial.addSource(timeTrialRepository.getTimeTrialById(timeTrialId)){
                timeTrial.value = it
            }
        }

    }

    fun getHeading(tt: TimeTrial): ResultRowViewModel{
        val mutList: MutableList<String> = mutableListOf()

        mutList.add("Rider")
        mutList.add("Category")
        mutList.add("Club")
        mutList.add("Total Time")
        tt.helper.results.firstOrNull()?.let {
            if(it.splits.size > 1) it.splits.forEachIndexed{ index, _ -> if(index - 1 <it.splits.size) mutList.add("Split ${index + 1}") }
        }
       // mutList.add("Notes")
        return ResultRowViewModel(mutList)
    }

}

class ResultRowViewModel{
    val row: MutableList<ResultCell> = mutableListOf()

    constructor(strings: List<String>){
        strings.forEach { row.add(ResultCell(MutableLiveData(it))) }
    }

    constructor(result: TimeTrialResult)
     {
        row.add(ResultCell(MutableLiveData("${result.timeTrialRider.rider.firstName} ${result.timeTrialRider.rider.lastName}")))
        row.add(ResultCell(MutableLiveData(result.category.categoryId())))
        row.add(ResultCell(MutableLiveData(result.timeTrialRider.rider.club)))
        row.add(ResultCell(MutableLiveData(ConverterUtils.toTenthsDisplayString(result.totalTime))))

        if(result.splits.size > 1){
            row.addAll(result.splits.map { ResultCell(MutableLiveData(ConverterUtils.toTenthsDisplayString(it))) })
        }
        // row.add(ResultCell(checker.notesMap[result.timeTrialRider.rider.id]?:MutableLiveData("Was Null")))

    }
}
class ResultCell(val content:LiveData<String>)
