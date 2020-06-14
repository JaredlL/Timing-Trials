package com.jaredlinden.timingtrials.util

import androidx.databinding.BindingConversion
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import kotlin.math.*

object ConverterUtils{

    val lengthDisplayUnitConversion = 1 / 1609.34

    fun toLengthDisplayUnit(length: Double): Double {
        return length * lengthDisplayUnitConversion
    }

    fun instantToSecondsDisplayString(instant: Instant): String{
        val  f:DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
        return (f.format(instant))
    }

    fun offsetToHmsDisplayString(ofs: OffsetDateTime): String{
        return ofs.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    @BindingConversion
    @JvmStatic
    fun dateToDisplay(dateTime: OffsetDateTime): String{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return dateTime.format(formatter)
    }


    fun toSecondsDisplayString(milliseconds: Long?): String{
        return milliseconds?.let {
            val millis = abs(milliseconds)
            val secs =  (millis/1000)
            String.format("%d:%02d:%02d", secs / 3600, (secs % 3600) / 60, (secs % 60))
        }?:""

    }

    fun toTenthsDisplayString(milliseconds: Long?): String{
        return milliseconds?.let {
            val milis = abs(milliseconds)
            val secs =  (milis/1000)
            return String.format("%d:%02d:%02d.%1d", secs / 3600, (secs % 3600) / 60, (secs % 60),  (milis % 1000) / 100)
        }?:""

    }

    fun toSecondMinuteHour(milliseconds: Long): String{
        return when {
            milliseconds < 999 -> "${sigDigRounder((milliseconds.toDouble()/1000))} sec"
            milliseconds in (1000..59999) -> "${milliseconds/1000}.${(milliseconds % 1000) / 100} sec".replace(".0", "")
            else -> String.format("%d min %d sec", milliseconds / 60000, (milliseconds/1000) % 60)
        }
    }

    fun sigDigRounder(value: Double, nSigDig: Int = 1, dir: Int = -1): Double {
        var intermediate = value / 10.0.pow(floor(log10(abs(value))) - (nSigDig - 1))
        intermediate = if (dir > 0) ceil(intermediate) else if (dir < 0) floor(intermediate) else intermediate.roundToInt().toDouble()
        return intermediate * 10.0.pow(floor(log10(abs(value))) - (nSigDig - 1))
    }


}

data class LengthConverter(val unitKey: String){

    val unitDef: LengthDef = unitList.firstOrNull { it.key == unitKey }?: unitList.first()

    fun lengthToDisplay(length: Double): String{
        return "%2.2f".format(length / unitDef.conversion)
    }

    fun convert(length: Double): Double{
        return  length / unitDef.conversion
    }

    fun convertBack(lengthString: String): Double?{
        val lenDouble = lengthString.toDoubleOrNull()
        return lenDouble?.times(unitDef.conversion)
    }

    fun getUnitName(): String{
        return unitDef.name
    }

    fun getUnitMiniNam(): String{
        return unitDef.miniString
    }

    companion object Table{

        val unitList = listOf(
                LengthDef("km", "Kilometers", "km", 1000.0),
                LengthDef("miles", "Miles", "mi", 1609.34),
                LengthDef("meters", "Meters", "m", 1.0)

        )

        val default: LengthConverter = LengthConverter("km")
    }

}
data class LengthDef(val key:String, val name:String, val miniString: String, val conversion: Double)
