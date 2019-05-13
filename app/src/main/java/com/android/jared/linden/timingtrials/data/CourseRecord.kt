package com.android.jared.linden.timingtrials.data

import com.android.jared.linden.timingtrials.domain.RiderCategoryStandard
import org.threeten.bp.OffsetDateTime

data class CourseRecord(val riderId: Long?,  val riderName: String, val timeTrialId: Long?, val club: String, val category: RiderCategoryStandard, val timeMillis: Long, val dateTime: OffsetDateTime? = null)