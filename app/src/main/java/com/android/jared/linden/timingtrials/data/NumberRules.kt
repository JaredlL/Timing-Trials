package com.android.jared.linden.timingtrials.data

enum class NumbersDirection {ASCEND, DESCEND}

class NumberRules(val terminus: Int = 1, val isStart: Boolean = true, val direction: NumbersDirection = NumbersDirection.ASCEND, val exclusions: List<Int> = listOf()) {

    fun isDefault(): Boolean{
        return terminus == 1 && direction == NumbersDirection.ASCEND && exclusions.isEmpty() && isStart
    }

    fun numberFromIndex(index: Int, totalCount: Int): Int{

        val dxToUse = if(isStart) index else totalCount - index

            val dir = if(direction == NumbersDirection.ASCEND) 1 else -1
            val num = (dxToUse + 1) * dxToUse * dir
            val range = terminus..num
            val count = exclusions.count { range.contains(it) }
            return num + count

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
                    val intVal = s.value.toInt()
                    when(s.index){
                        1 -> term = intVal
                        2-> isStart = intVal == 1
                        3 -> if(intVal == 1) direction = NumbersDirection.DESCEND
                        else-> exc.add((intVal))
                    }
                }
                return  NumberRules(term, isStart, direction, exc)
            }
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
                sb.append(",")
                for(x in rules.exclusions){
                    sb.append(x)
                    sb.append(",")
                }
                sb.toString()
            }

        }
    }

}