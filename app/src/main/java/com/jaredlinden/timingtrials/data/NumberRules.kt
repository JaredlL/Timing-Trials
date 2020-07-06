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

//data class MapNumberRules(val numbersMap: Map<Int,Int> = mutableMapOf()){
//
//    fun numberFromIndex(index: Int, totalCount: Int): Int{
//        numbersMap[index]?.let {
//            return it
//        }
//        val max = numbersMap.values.max()?:1
//        numbersMap[index] = max
//        return max
//
//    }
//
//    fun trySetNumberAtIndex(index: Int, number: Int){
//
//    }
//
//}

data class NumberRules(val mode: NumberMode = NumberMode.INDEX,
                       val indexRules: IndexNumberRules = IndexNumberRules()) {

    fun isDefault(): Boolean {
        return mode == NumberMode.INDEX && indexRules.isDefault()
    }

    fun numberFromIndex(index: Int, totalCount: Int): Int {
         return indexRules.numberFromIndex(index, totalCount)
        //NumberMode.MAP -> mapRules.numberFromIndex(index, totalCount)

    }


//    fun exlusionsString(): String{
//        val sb = StringBuilder()
//        for(e in exclusions){
//            sb.append(e)
//            sb.append(",")
//        }
//        return sb.toString()
//    }





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

//        fun fromString(str: String): NumberRules?{
//            if(str.isBlank()){
//                return NumberRules()
//            }else{
//                var term = 1
//                var isStart = true
//                var direction = NumbersDirection.ASCEND
//                val exc = mutableListOf<Int>()
//                for(s in str.splitToSequence(",").withIndex()){
//                   s.value.toIntOrNull()?.let {
//                        when(s.index){
//                            0 -> term = it
//                            1-> isStart = it == 1
//                            2 -> direction = if(it == 1) NumbersDirection.DESCEND else NumbersDirection.ASCEND
//                            else-> exc.add((it))
//                        }
//                    }
//
//                }
//                return  NumberRules(term, isStart, direction, exc)
//            }
//        }
//
//        fun stringToExclusions(excString : String): List<Int>{
//            return excString.splitToSequence(",").mapNotNull { it.toIntOrNull() }.toList()
//        }
//
//        fun toString(rules: NumberRules): String{
//            return if(rules.isDefault()){
//                ""
//            }else{
//                val sb = StringBuilder()
//                sb.append(rules.terminus)
//                sb.append(",")
//                sb.append(if(rules.isStart) 1 else 0)
//                sb.append(",")
//                sb.append(rules.direction.ordinal)
//                for(x in rules.exclusions){
//                    sb.append(",")
//                    sb.append(x)
//
//                }
//                sb.toString()
//            }
//
//        }
//    }
//
//}