package com.android.jared.linden.timingtrials.data

enum class Gender{
    UNKNOWN{
        override fun fullString(): String { return ""}
    },
    MALE{
        override fun fullString(): String { return "Male"}
    },
    FEMALE{
        override fun fullString(): String { return "Female"}
    },
    OTHER{
        override fun fullString(): String { return "Other"}
    };

    abstract fun fullString():String

    companion object {
        private val map = values().associateBy(Gender::ordinal)
        fun fromInt(type: Int) = map[type]
    }

}

enum class TimeTrialStatus(val type: Int){
    SETTING_UP(0),
    IN_PROGRESS(1),
    FINISHED(2);

    companion object {
        private val map = values().associateBy(TimeTrialStatus::ordinal)
        fun fromInt(type: Int) = map[type]
    }
}

enum class FinishCode(val type: Long){
    INVALID(0),
    DNF(-1),
    DNS(-2)
}