package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.RiderLight

interface IOrderRidersViewModel{
    fun getOrderableRiders(): LiveData<List<RiderLight>>
    fun moveItem(fromPosition: Int, toPosition:Int)
}