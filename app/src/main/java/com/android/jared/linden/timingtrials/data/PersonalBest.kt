package com.android.jared.linden.timingtrials.data

import org.threeten.bp.OffsetDateTime

data class PersonalBest(val courseId: Long, val timeTrialId: Long?, val courseName: String, val distance: Double? = null, val millisTime: Long, val dateTime: OffsetDateTime? = null)