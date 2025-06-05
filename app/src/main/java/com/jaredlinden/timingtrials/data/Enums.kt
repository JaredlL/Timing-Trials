package com.jaredlinden.timingtrials.data

enum class Gender{
    UNKNOWN{
        override fun fullString(): String { return ""}
        override fun smallString(): String {return ""}
    },
    MALE{
        override fun fullString(): String { return "Male"}
        override fun smallString(): String {return "M"}
    },
    FEMALE{
        override fun fullString(): String { return "Female"}
        override fun smallString(): String {return "F"}
    },
    OTHER{
        override fun fullString(): String { return "Other"}
        override fun smallString(): String {return "O"}
    };

    abstract fun fullString():String
    abstract fun smallString():String

    companion object {
        private val map = entries.associateBy(Gender::ordinal)
        fun fromInt(type: Int) = map[type]
    }

}

enum class TimeTrialStatus(val type: Int){
    SETTING_UP(0),
    IN_PROGRESS(1),
    FINISHED(2);

    companion object {
        private val map = entries.associateBy(TimeTrialStatus::ordinal)
        fun fromInt(type: Int) = map[type]
    }
}

enum class FinishCode(val type: Long){
    INVALID(0L),
    DNF(-1L),
    DNS(-2L)
}