package com.jaredlinden.timingtrials.domain

import com.jaredlinden.timingtrials.data.TimeTrial

data class RiderAssignmentResult(val succeeded: Boolean, val message: String, val tt:TimeTrial)

