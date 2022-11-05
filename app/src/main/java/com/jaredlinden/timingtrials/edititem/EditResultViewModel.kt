package com.jaredlinden.timingtrials.edititem

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.setup.*
import com.jaredlinden.timingtrials.util.ConverterUtils
import com.jaredlinden.timingtrials.util.Event
import com.jaredlinden.timingtrials.util.Utils
import com.jaredlinden.timingtrials.util.setIfNotEqual
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class EditResultViewModel @Inject constructor(val resultRepository: TimeTrialRiderRepository, val riderRepository: IRiderRepository) : ViewModel(){

    private val resultId: MutableLiveData<Long> = MutableLiveData()
    private val timeTrialId: MutableLiveData<Long> = MutableLiveData()

    var originalRiderId: Long? = null
    val result: MediatorLiveData<TimeTrialRider?> = MediatorLiveData()

    val excludedRiderIds  =Transformations.switchMap(timeTrialId){
           Transformations.map(resultRepository.getRidersForTimeTrial(it)){
               it.map { it.riderData.id }
           }
    }

    val rider = Transformations.switchMap(result){
        it?.riderId?.let {
           Transformations.map(riderRepository.getRider(it)){it?.fullName()}
        }?:MutableLiveData("Select Rider...")
    }

    //val gender: MutableLiveData<Gender> = MutableLiveData()
    val club = MutableLiveData("")
    val category = MutableLiveData("")
    val note = MutableLiveData("")
    val splits: MutableLiveData<List<String>> = MutableLiveData(listOf())
    val resultTime = MutableLiveData("")
    val selectedGenderPosition = MutableLiveData(2)
    val genders = Gender.values().map { it.fullString() }
    val resultSaved: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val changeRider: MutableLiveData<Event<Boolean>> = MutableLiveData()

    fun changeRider(){
        changeRider.value = Event(true)
    }

    fun setResult(resId: Long, ttId: Long){
        if(resId != 0L && resultId.value != resId){
            timeTrialId.value = ttId
            resultId.value = resId
        }else if(ttId != 0L && result.value?.timeTrialId != ttId){
            timeTrialId.value = ttId
            changeRider.value = Event(true)
            result.value = null
        }

    }

    val availibleRiders = Transformations.switchMap(excludedRiderIds){exclusions->
        Transformations.map(riderRepository.allRiders){
            it?.let {
                it.filter { !exclusions.contains(it.id) || result.value?.riderId == it.id || it.id == originalRiderId }
            }
        }
    }

    val selectRiderVm: ISelectRidersViewModel = SelectSingleRiderViewModel(
            availibleRiders,
            resultRepository,
            Transformations.map(result){ it?.riderId?.let { listOf(it) }?: listOf() },
            ::changeRider) {Unit}


    private fun changeRider(newRider: Rider){

            val resultTimeTrialId = timeTrialId.value
            if(newRider.id != null && resultTimeTrialId != null){
                val currentResVal = result.value
                if(currentResVal?.riderId != newRider.id){

                    viewModelScope.launch(Dispatchers.IO) {

                        val tt = resultRepository.getRidersForTimeTrialSuspend(resultTimeTrialId)
                        if(originalRiderId != newRider.id && tt.mapNotNull { it.riderData.id }.any { it == newRider.id } ){
                            selectRiderVm.showMessage.postValue(Event("This rider is already in the list of results for this timetrial!"))
                            result.postValue(result.value)
                        }else{
                            result.postValue(currentResVal?.copy(riderId = newRider.id, club = newRider.club, category = newRider.category, gender = newRider.gender)?:TimeTrialRider.fromRiderAndTimeTrial(newRider, resultTimeTrialId))
                            club.postValue(newRider.club)
                            category.postValue(newRider.category)
                            val genInt = Gender.values().indexOf(newRider.gender)
                            if(genInt != selectedGenderPosition.value){
                                selectedGenderPosition.postValue(genInt)
                            }
                            selectRiderVm.close.postValue(Event(true))
                        }
                    }
                }
            }
        }





    init {
        result.addSource(Transformations.switchMap(resultId){it?.let { resultRepository.getResultById(it) }}){ttResult->
            ttResult?.let {

                originalRiderId = it.rider.id
                category.setIfNotEqual(ttResult.category)
                club.setIfNotEqual(ttResult.riderClub)
                note.setIfNotEqual(ttResult.notes)
                resultTime.setIfNotEqual(ttResult.resultTime?.let { ConverterUtils.toTenthsDisplayString(it) }?:"")
                splits.setIfNotEqual(ttResult.splits.map { ConverterUtils.toTenthsDisplayString(it) })

                val genInt = Gender.values().indexOf(ttResult.gender)

                selectedGenderPosition.value = genInt


            }
            result.value = ttResult?.timeTrialData

        }
    }

    fun save(){
        result.value?.let {ttr->
            viewModelScope.launch(Dispatchers.IO) {
                val new = ttr.copy(
                        club = club.value?:"",
                        category = category.value?:"",
                        notes = note.value?:"",
                        splits = splits.value?.mapNotNull { ConverterUtils.fromTenthsDisplayString(it) } ?:listOf(),
                        finishCode = resultTime.value?.let { ConverterUtils.fromTenthsDisplayString(it) }?: FinishCode.DNF.type,
                        gender = selectedGenderPosition.value?.let { Gender.values()[it] }?:Gender.UNKNOWN

                )
                if(new != ttr || originalRiderId != ttr.riderId){
                    if(new.id == null){
                        resultRepository.insert(new)
                    }else{
                        resultRepository.update(new)
                    }

                }

                resultSaved.postValue(Event(true))
                selectRiderVm.riderFilter.postValue("")
            }

        }

    }


    val deleted: MutableLiveData<Event<Boolean>> = MutableLiveData()
    fun delete(){
        viewModelScope.launch(Dispatchers.IO) {
            result.value?.let { resultRepository.delete(it) }
            result.postValue(null)
            deleted.postValue(Event(true))
        }
    }

}

class SelectSingleRiderViewModel(val availibleRiders: LiveData<List<Rider>?>,
                                 val timeTrialRiderRepository: TimeTrialRiderRepository,
                                 val selectedRiders: LiveData<List<Long>?>,
                                 val addToSelection: (Rider) -> Unit,
                                 val removeFromSelection:(Rider) -> Unit ) : ISelectRidersViewModel{



    override val selectedRidersInformation: MediatorLiveData<SelectedRidersInformation> = MediatorLiveData()

    override val close: MutableLiveData<Event<Boolean>> = MutableLiveData()
    override val showMessage: MutableLiveData<Event<String>> = MutableLiveData()

    override fun riderSelected(newSelectedRider: Rider)
    {
        addToSelection(newSelectedRider)
    }

    override fun riderUnselected(riderToRemove: Rider) { removeFromSelection(riderToRemove)}

    override fun setRiderFilter(filterString: String) {
        riderFilter.value = filterString
    }


    val liveSortMode : MutableLiveData<Int> = MutableLiveData(SORT_DEFAULT)
    override fun setSortMode(sortMode: Int) {
        liveSortMode.value = sortMode
    }

    override val riderFilter: MutableLiveData<String> = MutableLiveData("")
    private val lastYear = OffsetDateTime.now().minusYears(1).minusMonths(6)

    private val ridersWithStartTimes = timeTrialRiderRepository.lastTimeTrialRiders()

    private val ridersOrderedByRecentActivity = Transformations.switchMap(availibleRiders){riderList->
        riderList?.let { rList->
            Transformations.map(ridersWithStartTimes){lastTimeTrialList->
                lastTimeTrialList?.let {
                    val startTimeMap = it.asSequence().filter { it.startTime.isAfter(lastYear) }.groupBy { it.riderId }.map { Pair(it.key, it.value.count()) }.toMap()
                    val ordered = rList.sortedByDescending { startTimeMap[it.id]?:0 }
                    ordered
                }?:riderList
            }
        }
    }

    init {
        selectedRidersInformation.addSource(ridersOrderedByRecentActivity){res->
            updateselectedRiderInfo(res, riderFilter.value, selectedRiders.value, liveSortMode.value?:SORT_DEFAULT)
        }
        selectedRidersInformation.addSource(selectedRiders){
            updateselectedRiderInfo(ridersOrderedByRecentActivity.value, riderFilter.value, it, liveSortMode.value?:SORT_DEFAULT)
        }
        selectedRidersInformation.addSource(riderFilter){it
            updateselectedRiderInfo(ridersOrderedByRecentActivity.value, it, selectedRiders.value, liveSortMode.value?:0)
        }
        selectedRidersInformation.addSource(liveSortMode){
            updateselectedRiderInfo(ridersOrderedByRecentActivity.value, riderFilter.value, selectedRiders.value, it)
        }
    }

    fun updateselectedRiderInfo(allRiders: List<Rider>?, filterString: String?, selectedIds: List<Long>?, sortMode: Int){
        if(allRiders != null && selectedIds != null){
            val filteredRiders = if(filterString.isNullOrBlank()){
                if(sortMode == SORT_ALPHABETICAL){
                    allRiders.sortedBy { it.fullName() }
                }else{
                    allRiders
                }

            }else{
                if(sortMode == SORT_ALPHABETICAL){
                    allRiders.asSequence().filter { it.fullName().contains(filterString, ignoreCase = true) }.sortedBy { it.fullName() }.toList()
                }else{
                    allRiders.filter { it.fullName().contains(filterString, ignoreCase = true) }
                }

            }
            selectedRidersInformation.value = SelectedRidersInformation(filteredRiders, selectedIds)
        }
    }

}
