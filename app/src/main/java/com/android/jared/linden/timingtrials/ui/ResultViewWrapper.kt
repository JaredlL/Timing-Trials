package com.android.jared.linden.timingtrials.ui


import com.android.jared.linden.timingtrials.data.TimeTrialResult
import com.android.jared.linden.timingtrials.util.ConverterUtils

data class ResultViewWrapper(val result: TimeTrialResult){

    val resultsRow: List<ResultCell>

    init {
        val mutList = mutableListOf<ResultCell>()
        val first = ResultCell("${result.timeTrialRider.rider.firstName} ${result.timeTrialRider.rider.lastName}")
        mutList.add(first)
        if(result.splits.size > 1){
            mutList.addAll(result.splits.map { ResultCell(ConverterUtils.toTenthsDisplayString(it)) })
        }
        mutList.add(ResultCell(ConverterUtils.toTenthsDisplayString(result.totalTime)))
        resultsRow = mutList
    }

}

data class ResultCell(val contents: String)

