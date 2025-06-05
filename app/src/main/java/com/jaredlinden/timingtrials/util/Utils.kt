package com.jaredlinden.timingtrials.util

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable

object Utils {

    val reservedChars = """"|\\?*<\":>+[]/'""""

    fun createFileName(name: String): String{
        val fileName = reservedChars.foldRight(name, {c, s -> s.replace(c, ".".single(), ignoreCase = true)})
        return fileName
    }

    fun colorDrawable(mColor: Int, d: Drawable?){
        d?.colorFilter = PorterDuffColorFilter(mColor, PorterDuff.Mode.MULTIPLY)
    }
}