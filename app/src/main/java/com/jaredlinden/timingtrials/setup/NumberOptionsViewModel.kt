package com.jaredlinden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.jaredlinden.timingtrials.data.IndexNumberRules
import com.jaredlinden.timingtrials.data.NumberMode
import com.jaredlinden.timingtrials.data.NumberRules
import com.jaredlinden.timingtrials.data.NumbersDirection
import com.jaredlinden.timingtrials.util.setIfNotEqual
import dagger.hilt.android.lifecycle.HiltViewModel

class NumberOptionsViewModel(val setupViewModel: SetupViewModel) {


    val numberRulesMediator : MediatorLiveData<NumberRules> = MediatorLiveData()

    val numberDirection: MutableLiveData<NumbersDirection> = MutableLiveData()
    val startNumber: MutableLiveData<String> = MutableLiveData()
//    val exclusions: MutableLiveData<String> = MutableLiveData()
    val selectedNumberOptionType: MutableLiveData<Int> = MutableLiveData(0)
    val modeList = listOf(NumberMode.INDEX, NumberMode.MAP)
    val mode: MutableLiveData<NumberMode> = MutableLiveData()

    init {
        numberRulesMediator.addSource(setupViewModel.timeTrial) {
            it?.timeTrialHeader?.numberRules?.let { nr ->
                mode.setIfNotEqual(nr.mode)
                selectedNumberOptionType.setIfNotEqual(modeList.indexOf(nr.mode))
                startNumber.setIfNotEqual(nr.indexRules.startNumber.toString())
                numberDirection.setIfNotEqual(nr.indexRules.direction)

            }
        }

        numberRulesMediator.addSource(startNumber) { newSs ->
            newSs.toIntOrNull()?.let {newSn->
                val currentTt = setupViewModel.timeTrial.value
                val currentSn = setupViewModel.timeTrial.value?.timeTrialHeader?.numberRules?.indexRules?.startNumber
                if (currentSn != null && currentTt != null) {
                    if (currentSn != newSn) {
                        val newNumRules = currentTt.timeTrialHeader.numberRules.copy( indexRules = currentTt.timeTrialHeader.numberRules.indexRules.copy( startNumber = newSn))
                        setupViewModel.updateTimeTrial(currentTt.copy(timeTrialHeader = currentTt.timeTrialHeader.copy(numberRules = newNumRules)))
                    }
                }
            }
        }

        numberRulesMediator.addSource(numberDirection){newDir->
            val currentTt = setupViewModel.timeTrial.value
            val currentDir = setupViewModel.timeTrial.value?.timeTrialHeader?.numberRules?.indexRules?.direction
            if(newDir != null && currentDir!= null && currentTt != null){
                if(currentDir != newDir){
                    val newNumRules = currentTt.timeTrialHeader.numberRules.copy(indexRules = currentTt.timeTrialHeader.numberRules.indexRules.copy(direction = newDir))
                    setupViewModel.updateTimeTrial(currentTt.copy(timeTrialHeader = currentTt.timeTrialHeader.copy(numberRules = newNumRules)))
                }
            }
        }

        numberRulesMediator.addSource(selectedNumberOptionType){newIndex->
            newIndex?.let {
                val newMode = modeList[newIndex]
                val currentTt = setupViewModel.timeTrial.value
                val currentMode = setupViewModel.timeTrial.value?.timeTrialHeader?.numberRules?.mode
                if(currentMode!= null && currentTt != null){
                    if(currentMode != newMode){
                        val newNumRules = currentTt.timeTrialHeader.numberRules.copy(mode = newMode)
                        setupViewModel.updateTimeTrial(currentTt.copy(timeTrialHeader = currentTt.timeTrialHeader.copy(numberRules = newNumRules)))
                    }
                }
            }
        }
    }

}