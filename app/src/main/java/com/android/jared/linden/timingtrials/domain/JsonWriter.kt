package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.domain.RiderResultIO
import com.android.jared.linden.timingtrials.domain.TimeTrialIO
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import java.io.OutputStream
import java.io.PrintWriter

class JsonResultsWriter{

    fun writeToPath(filePath: OutputStream, timeTrial: TimeTrial){
        writeToPath(filePath, listOf(timeTrial))
    }

    fun writeToPath(filePath: OutputStream, timeTrialList: List<TimeTrial>){
        val exclusionStrategy = object : ExclusionStrategy{
            override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                return false
            }

            override fun shouldSkipField(f: FieldAttributes?): Boolean {
                return f?.name == "id" || f?.name == "courseId"
            }

        }

        val writer = PrintWriter(filePath)
        val output = TimingTrialsExport(timeTrialList.map { TimeTrialIO(it) })
        GsonBuilder().addSerializationExclusionStrategy(exclusionStrategy).create().toJson(output,writer)
        writer.flush()
        writer.close()
    }

}
