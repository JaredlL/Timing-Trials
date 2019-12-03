package com.android.jared.linden.timingtrials.timetrialresults

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.*
import com.android.jared.linden.timingtrials.util.ConverterUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.Exception

class ResultViewModel @Inject constructor(val timeTrialRepository: ITimeTrialRepository, val riderRepository: IRiderRepository, val courseRepository: ICourseRepository, val resultRepository: IGlobalResultRepository) : ViewModel() {



    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()


    val results = Transformations.map(timeTrial){tt->
        if(tt != null && tt.timeTrialHeader.status == TimeTrialStatus.FINISHED) {

//            val checker = RecordChecker(tt, riderRepository, courseRepository)
//            viewModelScope.launch(Dispatchers.IO) {
//                checker.checkRecords()
//               checker.courseToUpdate?.let {  courseRepository.update(it)}
//                riderRepository.updateRiders(checker.ridersToUpdate)
//            }
            (sequenceOf(getHeading(tt)) + tt.helper.results3.asSequence().sortedBy { it.resultTime }.map { res-> ResultRowViewModel(res) }).toList()
        }else{
            null
        }

    }

    val resultsAreInserted: LiveData<Boolean> = Transformations.switchMap(timeTrial) {
        resultRepository.timeTrialHasResults(it.timeTrialHeader.id?:0)
    }



    val resultSettings: MutableLiveData<ResultDisplaySettings> = MutableLiveData()

    fun initialise(timeTrialId: Long){
        if(timeTrial.value?.timeTrialHeader?.id != timeTrialId){
            timeTrial.addSource(timeTrialRepository.getTimeTrialById(timeTrialId)){
                timeTrial.value = it
            }
        }

    }

    var isCorotineAlive = AtomicBoolean()
    fun insertResults(){
        if(!isCorotineAlive.get()){
            viewModelScope.launch(Dispatchers.IO) {
                timeTrial.value?.let {tt->
                    tt.timeTrialHeader.id?.let { ttid->

                        try {
                            isCorotineAlive.set(true)

                            val ttRes = resultRepository.getResultsForTimeTrial(ttid)

                            if(ttRes.isEmpty()){
                                resultRepository.insertNewResults(tt.helper.results3)
                            }
                        }catch(e:Exception){
                            throw e
                        }
                        finally {
                            isCorotineAlive.set(false)
                        }
                    }

                }

            }
        }

    }

    fun getHeading(tt: TimeTrial): ResultRowViewModel{
        val mutList: MutableList<String> = mutableListOf()

        mutList.add("Rider")
        mutList.add("Category")
        mutList.add("Club")
        mutList.add("Total Time")
        tt.helper.results3.firstOrNull()?.let {
            if(it.splits.size > 1) it.splits.forEachIndexed{ index, _ -> if(index - 1 <it.splits.size) mutList.add("Split ${index + 1}") }
        }
        mutList.add("Notes")
        return ResultRowViewModel(mutList)
    }

}

class ResultRowViewModel{
    val row: MutableList<ResultCell> = mutableListOf()

    constructor(strings: List<String>){
        strings.forEach { row.add(ResultCell(MutableLiveData(it))) }
    }

    constructor(result: IResult)
     {
        row.add(ResultCell(MutableLiveData("${result.rider.firstName} ${result.rider.lastName}")))
        row.add(ResultCell(MutableLiveData(result.categoryString)))
        row.add(ResultCell(MutableLiveData(result.riderClub)))
        row.add(ResultCell(MutableLiveData(ConverterUtils.toTenthsDisplayString(result.resultTime))))

        if(result.splits.size > 1){
            row.addAll(result.splits.map { ResultCell(MutableLiveData(ConverterUtils.toTenthsDisplayString(it))) })
        }
         row.add(ResultCell(MutableLiveData("")))


    }
}
class ResultCell(val content:LiveData<String>)
