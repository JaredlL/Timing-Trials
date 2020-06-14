package com.jaredlinden.timingtrials.data

import java.lang.Exception

enum class NumbersDirection {ASCEND, DESCEND}

data class NumberRules(val terminus: Int = 1, val isStart: Boolean = true, val direction: NumbersDirection = NumbersDirection.ASCEND, val exclusions: List<Int> = listOf()) {

    fun isDefault(): Boolean{
        return terminus == 1 && direction == NumbersDirection.ASCEND && exclusions.isEmpty() && isStart
    }

    fun exlusionsString(): String{
        val sb = StringBuilder()
        for(e in exclusions){
            sb.append(e)
            sb.append(",")
        }
        return sb.toString()
    }



    fun numberFromIndex(index: Int, totalCount: Int): Int{

        if(index >= totalCount){
            throw Exception("Error getting rider number - index is equal or greater than count")
        }

        val dxToUse = if(isStart) index else totalCount - index

        val dir = if(direction == NumbersDirection.ASCEND) 1 else -1
        val num = (terminus) + dxToUse * dir
        val range = terminus..num
        val count = exclusions.count { range.contains(it) }
        var retnumnum = num + count
        while (exclusions.contains(retnumnum)){
            retnumnum+=1
        }
        return retnumnum

    }

    companion object{

        fun fromString(str: String): NumberRules?{
            if(str.isBlank()){
                return NumberRules()
            }else{
                var term = 1
                var isStart = true
                var direction = NumbersDirection.ASCEND
                val exc = mutableListOf<Int>()
                for(s in str.splitToSequence(",").withIndex()){
                   s.value.toIntOrNull()?.let {
                        when(s.index){
                            0 -> term = it
                            1-> isStart = it == 1
                            2 -> direction = if(it == 1) NumbersDirection.DESCEND else NumbersDirection.ASCEND
                            else-> exc.add((it))
                        }
                    }

                }
                return  NumberRules(term, isStart, direction, exc)
            }
        }

        fun stringToExclusions(excString : String): List<Int>{
            return excString.splitToSequence(",").mapNotNull { it.toIntOrNull() }.toList()
        }

        fun toString(rules: NumberRules): String{
            return if(rules.isDefault()){
                ""
            }else{
                val sb = StringBuilder()
                sb.append(rules.terminus)
                sb.append(",")
                sb.append(if(rules.isStart) 1 else 0)
                sb.append(",")
                sb.append(rules.direction.ordinal)
                for(x in rules.exclusions){
                    sb.append(",")
                    sb.append(x)

                }
                sb.toString()
            }

        }
    }

}