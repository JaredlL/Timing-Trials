package com.jaredlinden.timingtrials.edititem

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.FinishCode
import com.jaredlinden.timingtrials.data.Gender
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrialRiderResult
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.setup.ISelectRidersViewModel
import com.jaredlinden.timingtrials.setup.SelectedRidersInformation
import com.jaredlinden.timingtrials.util.ConverterUtils
import com.jaredlinden.timingtrials.util.Event
import com.jaredlinden.timingtrials.util.Utils
import com.jaredlinden.timingtrials.util.setIfNotEqual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditResultViewModel @Inject constructor(val resultRepository: TimeTrialRiderRepository, val riderRepository: IRiderRepository) : ViewModel(), ISelectRidersViewModel {



    private val resultId: MutableLiveData<Long> = MutableLiveData()

    val result: MediatorLiveData<TimeTrialRiderResult?> = MediatorLiveData()

    val rider = Transformations.map(result){
        it?.rider
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

    fun setResult(id: Long){
        resultId.value = id
    }

    init {
        result.addSource(Transformations.switchMap(resultId){it?.let { resultRepository.getResultById(it) }}){ttResult->
            ttResult?.let {

                category.setIfNotEqual(ttResult.category)
                club.setIfNotEqual(ttResult.riderClub)
                note.setIfNotEqual(ttResult.notes)
                resultTime.setIfNotEqual(ttResult.resultTime?.let { ConverterUtils.toTenthsDisplayString(it) }?:"")
                splits.setIfNotEqual(ttResult.splits.map { ConverterUtils.toTenthsDisplayString(it) })

                val genInt = Gender.values().indexOf(ttResult.gender)
                if(genInt != selectedGenderPosition.value){
                    selectedGenderPosition.value = genInt
                }

            }
            result.value = ttResult

        }
    }

    fun save(){
        result.value?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val ttr = it.timeTrialData
                val new = ttr.copy(
                        club = club.value?:"",
                        category = category.value?:"",
                        notes = note.value?:"",
                        splits = splits.value?.mapNotNull { ConverterUtils.fromTenthsDisplayString(it) } ?:listOf(),
                        finishCode = resultTime.value?.let { ConverterUtils.fromTenthsDisplayString(it) }?: FinishCode.DNF.type,
                        gender = selectedGenderPosition.value?.let { Gender.values()[it] }?:Gender.UNKNOWN

                )
                if(new != ttr){
                    resultRepository.update(new)
                }

                resultSaved.postValue(Event(true))
            }

        }

    }



    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    override val selectedRidersInformation: LiveData<SelectedRidersInformation> = Transformations.map(riderRepository.allRiders){
        it?.let {

        }
    }

    override fun riderSelected(newSelectedRider: Rider) {
        TODO("Not yet implemented")
    }

    override fun riderUnselected(riderToRemove: Rider) {
        TODO("Not yet implemented")
    }

    override fun setRiderFilter(filterString: String) {
        TODO("Not yet implemented")
    }

    override fun setSortMode(sortMode: Int) {
        TODO("Not yet implemented")
    }

    override val riderFilter: MutableLiveData<String>
        get() = TODO("Not yet implemented")


}
