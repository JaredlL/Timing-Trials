package com.jaredlinden.timingtrials.data

import org.threeten.bp.OffsetDateTime

interface IResult{

    val rider:Rider
    val course: Course
    val riderClub: String
    val category: String
    val gender: Gender
    val laps: Int
    val resultTime: Long?
    val splits: List<Long>
    val dateSet: OffsetDateTime?
    val timeTrial: TimeTrialHeader?
    val notes:String
}