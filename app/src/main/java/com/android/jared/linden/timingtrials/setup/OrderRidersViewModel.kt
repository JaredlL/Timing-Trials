package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.jared.linden.timingtrials.data.*

interface IOrderRidersViewModel{
    fun getOrderableRiderData(): LiveData<TimeTrial?>
    fun moveItem(fromPosition: Int, toPosition:Int)
    val startNumber: MutableLiveData<Int>
    val exclusions: MutableLiveData<String>
    fun getNumberDirection(): LiveData<NumbersDirection>
    fun setNumberDirection(mode: NumbersDirection)


}