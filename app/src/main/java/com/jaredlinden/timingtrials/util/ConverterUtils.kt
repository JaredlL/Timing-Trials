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

    fun fromTenthsDisplayString(tenthsString: String): Long?{
        val splitAtPoint = tenthsString.split(".")
        if(splitAtPoint.size > 1){

            val ms = splitAtPoint.last().let { ".$it" }.toDoubleOrNull()?.let { it * 1000}?.toInt()?:0

            val splits = splitAtPoint.first().split(":").reversed()
            val sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
            val min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
            val hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0

            val sum = (hour + min + sec + ms).toLong()
            val fval = if(sum > 0) sum else null

            return fval

        }else{
            val splits = splitAtPoint.first().split(":").reversed()
            var ms = 0
            var sec = 0
            var min = 0
            var hour = 0

            if(splits.firstOrNull()?.length == 1){
                ms = splits.getOrNull(0)?.let { ".$it" }?.toDoubleOrNull()?.let { it * 1000}?.toInt()?:0
                sec = splits.getOrNull(1)?.toIntOrNull()?.times(1000)?:0
                min = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60)?:0
                hour = splits.getOrNull(3)?.toIntOrNull()?.times(1000 * 60 * 60)?:0
            }else{
                sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
                min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
                hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0
            }
            val sum = (hour + min + sec + ms).toLong()
            val fval = if(sum > 0) sum else null

            return fval
        }

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
