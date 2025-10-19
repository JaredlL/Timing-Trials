package com.jaredlinden.timingtrials.timetrialresults

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.data.roomrepo.ICourseRepository
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.util.ConverterUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeTrialViewModel @Inject constructor (val timeTrialRepository: ITimeTrialRepository) : ViewModel(){

    private val idLiveData: MutableLiveData<Long?> = MutableLiveData()

    val timeTrial = idLiveData.switchMap{
        it?.let { id->
            timeTrialRepository.getResultTimeTrialById(id)
        }
    }
}

@HiltViewModel
class ResultViewModel @Inject constructor(
    val timeTrialRepository: ITimeTrialRepository,
    val riderRepository: IRiderRepository,
    val courseRepository: ICourseRepository) : ViewModel() {

    private val idLiveData: MutableLiveData<Long?> = MutableLiveData()

    fun changeTimeTrial(newId: Long){
        if(idLiveData.value != newId){
            idLiveData.postValue(newId)
        }
    }

    val timeTrial = idLiveData.switchMap{
        it?.let { id->
            timeTrialRepository.getResultTimeTrialById(id)
        }
    }

    val results = timeTrial.map{tt->
        if(tt != null && tt.timeTrialHeader.status == TimeTrialStatus.FINISHED) {
            val displaySplits = anySplitHasValue(tt)
            (sequenceOf(getHeading(tt)) + tt.helper.results.asSequence().map {
                    res-> ResultRowViewModel(res, res.timeTrialData.id, tt, displaySplits)
            }).toList()
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

    fun clearNotesColumn(){
        timeTrial.value?.let{
            val newRes = it.riderList.map { it.copy(timeTrialRiderData = it.timeTrialRiderData.copy(notes = "")) }
            viewModelScope.launch(Dispatchers.IO) { timeTrialRepository.updateFull(it.copy(riderList = newRes)) }
        }
    }

    fun updateDescription(newDescription: String){
        timeTrial.value?.timeTrialHeader?.let {
            viewModelScope.launch(Dispatchers.IO) { timeTrialRepository.update(it.copy(description = newDescription)) }

        }
    }

    private fun getHeading(tt: TimeTrial): ResultRowViewModel{
        val mutList: MutableList<String> = mutableListOf()

        mutList.add("Rider Name")
        mutList.add("Total Time")
        mutList.add("Club")
        mutList.add("Gender")

        if (hasAnyCategory(tt)){
            mutList.add("Category")
        }
        if (hasAnyNotes(tt)){
            mutList.add("Notes")
        }

        if(tt.timeTrialHeader.laps > 1 && anySplitHasValue(tt)){
            for(i in 1..tt.timeTrialHeader.laps){
                mutList.add("Split $i")
            }
        }

        return ResultRowViewModel(mutList, null)
    }

    private fun hasAnyCategory(tt: TimeTrial): Boolean{
        return tt.riderList.any { x -> x.timeTrialRiderData.category.any() }
    }

    private fun hasAnyNotes(tt: TimeTrial): Boolean{
        return tt.riderList.any { x -> x.timeTrialRiderData.notes.any() }
    }

    private fun anySplitHasValue(tt: TimeTrial): Boolean{
        return tt.riderList.flatMap { x -> x.timeTrialRiderData.splits }.any()
    }
}

class ResultRowViewModel{
    val row: MutableList<ResultCell> = mutableListOf()

    constructor(strings: List<String>, resId: Long?){
        val id = resId?:0L
        strings.forEach { row.add(ResultCell(id,MutableLiveData(it))) }
    }

    constructor(result: IResult, resId: Long?, tt: TimeTrial,  addSplits: Boolean)
     {
         val laps = tt.timeTrialHeader.laps
         row.add(ResultCell(resId, MutableLiveData("${result.rider.firstName} ${result.rider.lastName}")))
         row.add(ResultCell(resId,MutableLiveData(ConverterUtils.toTenthsDisplayString(result.resultTime))))
         row.add(ResultCell(resId,MutableLiveData(result.riderClub)))
         row.add(ResultCell(resId,MutableLiveData(result.gender.fullString())))

         if(tt.riderList.any { x -> x.timeTrialRiderData.category.any() }){
             row.add(ResultCell(resId,MutableLiveData(result.category)))
         }

         if(tt.riderList.any { x -> x.timeTrialRiderData.notes.any() }){
             row.add(ResultCell(resId,MutableLiveData(result.notes)))
         }

         if(laps > 1 && addSplits){
             for (i in 0 until laps){
                 val splitVal = result.splits.getOrNull(i)
                 val splitString = if(splitVal != null) ConverterUtils.toTenthsDisplayString(splitVal) else ""
                 row.add(ResultCell(resId,MutableLiveData(splitString)))
             }
         }
    }
}

class ResultCell(val resultId: Long?, val content:MutableLiveData<String>)
