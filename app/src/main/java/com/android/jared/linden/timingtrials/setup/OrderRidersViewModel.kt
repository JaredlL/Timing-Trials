package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import com.android.jared.linden.timingtrials.data.Rider

interface IOrderRidersViewModel{
    fun getOrderableRiders(): LiveData<List<Rider>>
    fun moveItem(fromPosition: Int, toPosition:Int)
}