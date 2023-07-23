package com.jaredlinden.timingtrials.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.jaredlinden.timingtrials.R

class ErrorDialog {
    companion object{
        fun display(context: Context, title: String, e: Exception){
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setTitle(title)
            alertDialog.setMessage((e.localizedMessage ?: e.message))
            alertDialog.setButton(
                AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.close)
            ) { dialog, which -> dialog.dismiss() }
            alertDialog.show()
        }
    }
}