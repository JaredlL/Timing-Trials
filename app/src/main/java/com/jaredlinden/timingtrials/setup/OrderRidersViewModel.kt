package com.jaredlinden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.util.Event

interface IOrderRidersViewModel{
    fun getOrderableRiderData(): LiveData<TimeTrial?>
    fun moveItem(fromPosition: Int, toPosition:Int)
    fun setRiderNumber(newNumber: Int, riderToChange: FilledTimeTrialRider)
    val setNumberMessage: LiveData<Event<String>>
}

class OrderRidersViewModel(val setupViewModel: SetupViewModel) : IOrderRidersViewModel{

    val timeTrial = setupViewModel.timeTrial

    override val setNumberMessage: LiveData<Event<String>> = MutableLiveData()

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        timeTrial.value?.let { currentTimeTrial ->
            val mutList = currentTimeTrial.riderList.toMutableList()

            if (fromPosition > toPosition) {
                mutList.add(toPosition, mutList[fromPosition])
                mutList.removeAt(fromPosition + 1)
            } else {
                mutList.add(toPosition + 1, mutList[fromPosition])
                mutList.removeAt(fromPosition)
            }
            val updateList = mutList.mapIndexed { i, r -> r.copy(timeTrialRiderData = r.timeTrialRiderData.copy(index = i)) }.sortedBy { it.timeTrialRiderData.index }
            setupViewModel.updateTimeTrial(currentTimeTrial.updateRiderList(updateList))
        }
    }


    override fun setRiderNumber(newNumber: Int, riderToChange: FilledTimeTrialRider) {
        timeTrial.value?.let { tt->
            val otherRider = tt.riderList.filterNot { it.riderData.id == riderToChange.riderData.id }.firstOrNull { it.timeTrialRiderData.assignedNumber == newNumber }
            if(otherRider != null){
                val oldNumber = riderToChange.timeTrialRiderData.assignedNumber
                val newRiderList = tt.riderList.map {
                    when(it.riderData.id){
                        riderToChange.riderData.id -> riderToChange.copy(timeTrialRiderData = riderToChange.timeTrialRiderData.copy(assignedNumber = newNumber))
                        otherRider.riderData.id-> otherRider.copy(timeTrialRiderData = otherRider.timeTrialRiderData.copy(assignedNumber = oldNumber))
                        else -> it
                    }
                }
                setupViewModel.updateTimeTrial(tt.updateRiderList(newRiderList))
            }else{
                setupViewModel.updateTimeTrial(tt.updateRiderList(tt.riderList.map { if(it.riderData.id == riderToChange.riderData.id) riderToChange.copy(timeTrialRiderData = riderToChange.timeTrialRiderData.copy(assignedNumber = newNumber)) else it }))
            }
        }
    }

    override fun getOrderableRiderData(): LiveData<TimeTrial?> {
        return timeTrial
    }
}