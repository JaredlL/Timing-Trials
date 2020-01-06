package com.android.jared.linden.timingtrials.util

object Utils {

    val reservedChars = """"|\\?*<\":>+[]/'""""

    fun createFileName(name: String): String{
        val fileName = reservedChars.foldRight(name, {c, s -> s.replace(c, ".".single(), ignoreCase = true)})
        return fileName
    }

}