package com.jaredlinden.timingtrials.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception

enum class NumbersDirection {ASCEND, DESCEND}
enum class NumberMode {INDEX, MAP}

data class IndexNumberRules(val startNumber: Int = 1,
                            val direction: NumbersDirection = NumbersDirection.ASCEND,
                            val exclusions: List<Int> = listOf()){

    fun numberFromIndex(index: Int, totalCount: Int): Int{

        if(index >= totalCount){
            throw Exception("Error getting rider number - index is equal or greater than count")
        }


        val dir = if(direction == NumbersDirection.ASCEND) 1 else -1
        val num = (startNumber) + index * dir
        val range = startNumber..num
        val count = exclusions.count { range.contains(it) }
        var retnumnum = num + count
        while (exclusions.contains(retnumnum)){
            retnumnum+=1
        }
        return retnumnum

    }

        fun exlusionsString(): String{
        val sb = StringBuilder()
        for(e in exclusions){
            sb.append(e)
            sb.append(",")
        }
        return sb.toString()
    }

    fun isDefault(): Boolean{
        return startNumber == 1 && direction == NumbersDirection.ASCEND && exclusions.isEmpty()
    }

    companion object{
        fun stringToExclusions(excString : String): List<Int>{
            return excString.splitToSequence(",").mapNotNull { it.toIntOrNull() }.toList()
        }
    }


}

data class NumberRules(val mode: NumberMode = NumberMode.INDEX,
                       val indexRules: IndexNumberRules = IndexNumberRules()) {

    fun isDefault(): Boolean {
        return mode == NumberMode.INDEX && indexRules.isDefault()
    }

    fun numberFromIndex(index: Int, totalCount: Int): Int {
         return indexRules.numberFromIndex(index, totalCount)

    }

    companion object{

        val gson = Gson()
        val rulesType = object : TypeToken<NumberRules>() {}.type

        fun fromString(str: String): NumberRules?{
            return gson.fromJson<NumberRules>(str, rulesType)
        }

        fun toString(rules: NumberRules): String{
            return gson.toJson(rules)
        }
    }
}