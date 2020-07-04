package com.jaredlinden.timingtrials.util

import android.graphics.ColorFilter
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.jaredlinden.timingtrials.R

object Utils {

    val reservedChars = """"|\\?*<\":>+[]/'""""

    fun createFileName(name: String): String{
        val fileName = reservedChars.foldRight(name, {c, s -> s.replace(c, ".".single(), ignoreCase = true)})
        return fileName
    }

    fun colorDrawable(mColor: Int, d: Drawable?){
        val red: Int = (mColor and 0xFF0000) / 0xFFFF
        val green: Int = (mColor and 0xFF00) / 0xFF
        val blue: Int = mColor and 0xFF

        val matrix = floatArrayOf(0f, 0f, 0f, 0f, red.toFloat(), 0f, 0f, 0f, 0f, green.toFloat(), 0f, 0f, 0f, 0f, blue.toFloat(), 0f, 0f, 0f, 1f, 0f)

        val colorFilter: ColorFilter = ColorMatrixColorFilter(matrix)

        d?.colorFilter = PorterDuffColorFilter(mColor, PorterDuff.Mode.MULTIPLY)
    }

}