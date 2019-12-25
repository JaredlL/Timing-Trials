package com.android.jared.linden.timingtrials.domain.csv

import com.android.jared.linden.timingtrials.data.Course

class LineToCourseConverter: ILineToObjectConverter<Course> {
    override fun isHeading(line: String): Boolean {
        return line.splitToSequence(",", ignoreCase = true).any{it.contains("course", true)}
    }

    val defaultConversion = 1000.0
    var nameIndex:Int? = 0
    var distanceIndex:Int? = 2
    var cttNameIndex:Int? = 1
    var conversion: Double = defaultConversion

    val conversions = mapOf("km" to 1000.0, "miles" to 1609.34, "mi" to 1609.34, "meters" to 1.0)


    override fun setHeading(headingLine: String) {
        val splitLine = headingLine.splitToSequence(",", ignoreCase = true)

        nameIndex = splitLine.withIndex().firstOrNull { it.value.contains("name", true) }?.index
        distanceIndex= splitLine.withIndex().firstOrNull { it.value.contains("distance", true) || it.value.contains("length", true) }?.index
        val distanceString = distanceIndex?.let { splitLine.elementAtOrNull(it)}
        conversion = distanceString?.let {ds -> conversions.filter{ds.contains(it.key, ignoreCase =  true)}.values.firstOrNull() } ?:defaultConversion
        cttNameIndex= splitLine.withIndex().firstOrNull { it.value.contains("ctt", true) }?.index
    }

    override fun importLine(dataLine: String): Course? {
        try{
            val dataList = dataLine.split(",", ignoreCase =  true)
            val courseName = nameIndex?.let { dataList.getOrNull(it)}
            val distance = distanceIndex?.let { dataList.getOrNull(it)?.toIntOrNull()?.times(conversion)}
            val cctName = cttNameIndex?.let { dataList.getOrNull(it) }

            return if(courseName.isNullOrBlank()){
                null
            }else{
                Course(courseName, distance?:0.0, cctName?:"")
            }


        }catch (e:Exception){
            throw Exception("Error reading course data", e)
        }
    }


}