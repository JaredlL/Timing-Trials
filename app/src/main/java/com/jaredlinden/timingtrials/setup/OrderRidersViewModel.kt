package com.jaredlinden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.jaredlinden.timingtrials.data.*

interface IOrderRidersViewModel{
    fun getOrderableRiderData(): LiveData<TimeTrial?>
    fun moveItem(fromPosition: Int, toPosition:Int)
    val startNumber: MutableLiveData<String>
    val exclusions: MutableLiveData<String>
    val numberDirection: MutableLiveData<NumbersDirection>
    val numberRulesMediator: LiveData<IndexNumberRules>


}

class OrderRidersViewModel(val setupViewModel: SetupViewModel) : IOrderRidersViewModel{

    val _mTimeTrial = setupViewModel.timeTrial

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        _mTimeTrial.value?.let { currentTimeTrial ->
            val mutList = currentTimeTrial.riderList.toMutableList()

            if (fromPosition > toPosition) {
                mutList.add(toPosition, mutList[fromPosition])
                mutList.removeAt(fromPosition + 1)
            } else {
                mutList.add(toPosition + 1, mutList[fromPosition])
                mutList.removeAt(fromPosition)
            }
            val updateList = mutList.mapIndexed { i, r -> r.copy(timeTrialData = r.timeTrialData.copy(index = i)) }
            setupViewModel.updateTimeTrial(currentTimeTrial.updateRiderList(updateList))
        }


    }

    override val numberDirection: MutableLiveData<NumbersDirection> = MutableLiveData()
    override val startNumber: MutableLiveData<String> = MutableLiveData()
    override val exclusions: MutableLiveData<String> = MutableLiveData()
    override val numberRulesMediator : MediatorLiveData<IndexNumberRules> = MediatorLiveData()

    init {
        numberRulesMediator.addSource(_mTimeTrial) {
            it?.timeTrialHeader?.numberRules?.indexRules?.let { nr ->
                val exString = nr.exlusionsString()
                if (startNumber.value != nr.terminus.toString()) startNumber.value = nr.terminus.toString()
                if (exString != exclusions.toString()) exclusions.value = exString
                if(numberDirection.value != nr.direction) numberDirection.value = nr.direction
            }
        }

            numberRulesMediator.addSource(startNumber) { newSs ->
                newSs.toIntOrNull()?.let {newSn->
                    val currentTt = _mTimeTrial.value
                    val currentSn = _mTimeTrial.value?.timeTrialHeader?.numberRules?.indexRules?.terminus
                    if (currentSn != null && currentTt != null) {
                        if (currentSn != newSn) {
                            val newNumRules = currentTt.timeTrialHeader.numberRules.copy( indexRules = currentTt.timeTrialHeader.numberRules.indexRules.copy( terminus = newSn))
                            setupViewModel.updateTimeTrial(currentTt.copy(timeTrialHeader = currentTt.timeTrialHeader.copy(numberRules = newNumRules)))
                        }
                    }
                }
            }

            numberRulesMediator.addSource(numberDirection){newDir->
                val currentTt = _mTimeTrial.value
                val currentDir = _mTimeTrial.value?.timeTrialHeader?.numberRules?.indexRules?.direction
                if(newDir != null && currentDir!= null && currentTt != null){
                    if(currentDir != newDir){
                        val newNumRules = currentTt.timeTrialHeader.numberRules.copy(indexRules = currentTt.timeTrialHeader.numberRules.indexRules.copy(direction = newDir))
                        setupViewModel.updateTimeTrial(currentTt.copy(timeTrialHeader = currentTt.timeTrialHeader.copy(numberRules = newNumRules)))
                    }
                }
            }

            numberRulesMediator.addSource(exclusions){newExc->
                val currentTt = _mTimeTrial.value
                val currentExc = _mTimeTrial.value?.timeTrialHeader?.numberRules?.indexRules?.exclusions
                if(newExc != null && currentExc!= null && currentTt != null){
                    val newList = IndexNumberRules.stringToExclusions(newExc)
                    if(currentExc != newList){
                        val newNumRules = currentTt.timeTrialHeader.numberRules.copy(indexRules = currentTt.timeTrialHeader.numberRules.indexRules.copy(exclusions = newList))
                        setupViewModel.updateTimeTrial(currentTt.copy(timeTrialHeader = currentTt.timeTrialHeader.copy(numberRules = newNumRules)))
                    }
                }
            }
        }





    override fun getOrderableRiderData(): LiveData<TimeTrial?> {
        return _mTimeTrial
    }
}