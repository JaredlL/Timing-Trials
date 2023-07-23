package com.jaredlinden.timingtrials.domain

import com.jaredlinden.timingtrials.data.TimeTrial
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

        val output = TimingTrialsExport(timeTrialList.map { TimeTrialIO(it) })
        val zip = ZipOutputStream(BufferedOutputStream(filePath))
        val writer = OutputStreamWriter(zip)

        zip.putNextEntry(ZipEntry("TimingTrials.json"))
        GsonBuilder().addSerializationExclusionStrategy(exclusionStrategy).create().toJson(output,writer)
        writer.close()
        zip.close()
    }
}
