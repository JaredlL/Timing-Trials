package com.android.jared.linden.timingtrials.data

enum class Gender{
    UNKNOWN{
        override fun gendarString(): String { return ""}
    },
    MALE{
        override fun gendarString(): String { return "M"}
    },
    FEMALE{
        override fun gendarString(): String { return "F"}
    },
    OTHER{
        override fun gendarString(): String { return "O"}
    };

    abstract fun gendarString(): String

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