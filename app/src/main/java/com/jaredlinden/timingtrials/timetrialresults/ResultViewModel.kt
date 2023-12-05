package com.jaredlinden.timingtrials.timetrialresults

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.roomrepo.*
import com.jaredlinden.timingtrials.util.ConverterUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class TimeTrialViewModel @Inject constructor (val timeTrialRepository: ITimeTrialRepository) : ViewModel(){

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

    var queue = ConcurrentLinkedQueue<TimeTrial>()
    private val isCorotineAlive = AtomicBoolean()

    private fun updateTimeTrial(newtt: TimeTrial){
        Timber.d("Update TT, ${newtt.riderList.size} riders")
        if(isCorotineAlive.compareAndSet(false, true)){
            queue.add(newtt)
            viewModelScope.launch(Dispatchers.IO) {
                while (queue.peek() != null){
                    var ttToInsert = queue.peek()
                    while (queue.peek() != null){
                        ttToInsert = queue.poll()
                    }
                    ttToInsert?.let {
                        timeTrialRepository.updateFull(it)
                    }
                }
                isCorotineAlive.set(false)
            }
        }else{
            queue.add(newtt)
        }
    }
}

@HiltViewModel
class ResultViewModel @Inject constructor(
    val timeTrialRepository: ITimeTrialRepository,
    val riderRepository: IRiderRepository,
    val courseRepository: ICourseRepository,
    val resultRepository: TimeTrialRiderRepository) : ViewModel() {

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
            val newRes = it.riderList.map { it.copy(timeTrialData = it.timeTrialData.copy(notes = "")) }
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
        return tt.riderList.any { x -> x.timeTrialData.category.any() }
    }

    private fun hasAnyNotes(tt: TimeTrial): Boolean{
        return tt.riderList.any { x -> x.timeTrialData.notes.any() }
    }

    private fun anySplitHasValue(tt: TimeTrial): Boolean{
        return tt.riderList.flatMap { x -> x.timeTrialData.splits }.any()
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

         if(tt.riderList.any { x -> x.timeTrialData.category.any() }){
             row.add(ResultCell(resId,MutableLiveData(result.category)))
         }

         if(tt.riderList.any { x -> x.timeTrialData.notes.any() }){
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
