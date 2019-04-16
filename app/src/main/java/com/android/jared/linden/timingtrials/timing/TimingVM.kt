package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.LiveData

interface ITimingVM{
    val timeString: LiveData<String>
}

class TimingVM(val viewModel: TimingViewModel): ITimingVM{
    override val timeString: LiveData<String> =


}