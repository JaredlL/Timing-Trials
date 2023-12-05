package com.jaredlinden.timingtrials.util

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.jaredlinden.timingtrials.TimingTrialsApplication

@Suppress("UNCHECKED_CAST")
fun <T : Any> Fragment.argument(key: String) =
        lazy { arguments?.get(key) as? T ?: error("Intent Argument $key is missing") }

@Suppress("UNCHECKED_CAST")
fun <T : Any> AppCompatActivity.argument(key: String) =
        lazy { intent?.extras?.get(key) as? T ?: error("Intent Argument $key is missing") }


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