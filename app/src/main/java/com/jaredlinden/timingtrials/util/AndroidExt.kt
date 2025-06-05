package com.jaredlinden.timingtrials.util

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager


fun Fragment.getLengthConverter():LengthConverter{
    val unitString = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getString("units", "km")?:"km"
    return LengthConverter(unitString)
}

fun Fragment.hideKeyboard(){
    val imm: InputMethodManager = context!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view!!.windowToken, 0)
}

fun Fragment.showKeyboard(){
    val imm: InputMethodManager = context!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun <T> MutableLiveData<T>.setIfNotEqual(newVal:T){
    if(value != newVal){
        value = newVal
    }
}

fun <T,U> MediatorLiveData<T>.changeValIfNotEqual(obs: LiveData<U>, getVal: (T) -> U, setVal: (U,T) -> T){
    this.addSource(obs){res->
        res?.let {u->
            this.value?.let { t->
                if(getVal(t) != u){
                    val newVal = setVal(u,t)
                    this.value = newVal
                }
            }
        }
    }
}