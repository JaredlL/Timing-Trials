package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.ui.RiderStatus

data class RiderAssignmentResult(val succeeded: Boolean, val message: String, val tt:TimeTrial)

