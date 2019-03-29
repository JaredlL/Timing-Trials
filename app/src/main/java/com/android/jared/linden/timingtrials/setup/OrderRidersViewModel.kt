package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.domain.TimeTrialSetup

class OrderRidersViewModel(private val timeTrialSetup: TimeTrialSetup) : ViewModel() {


    fun getOrderableRiders(): LiveData<List<Rider>>{
        return timeTrialSetup.selectedOrderedRiders
    }

    fun moveItem(fromPosition: Int, toPosition:Int){

        val newList = timeTrialSetup.selectedOrderedRiders.value?.toMutableList() ?: arrayListOf()

        if(fromPosition > toPosition){
            newList.add(toPosition, newList[fromPosition])
            newList.removeAt(fromPosition + 1)
        }else{
            newList.add(toPosition + 1, newList[fromPosition])
            newList.removeAt(fromPosition)
        }


        timeTrialSetup.selectedOrderedRiders.postValue(newList)
    }

}