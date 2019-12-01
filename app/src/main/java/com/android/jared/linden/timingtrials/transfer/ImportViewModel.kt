package com.android.jared.linden.timingtrials.transfer

import androidx.lifecycle.ViewModel
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IGlobalResultRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import javax.inject.Inject

class ImportViewModel @Inject constructor(private val riderRespository: IRiderRepository,
                                          private val courseRepository: ICourseRepository,
                                          private val timeTrialRepository: ITimeTrialRepository,
                                          private val  resultRepository: IGlobalResultRepository): ViewModel() {
    // TODO: Implement the ViewModel
}
