package com.jaredlinden.timingtrials.data

import org.threeten.bp.OffsetDateTime

data class CourseRecord(
    val riderId: Long?,
    val riderName: String,
    val timeTrialId: Long?,
    val club: String,
    val gender: Gender,
    val category: String,
    val timeMillis: Long,
    val dateTime: OffsetDateTime? = null)