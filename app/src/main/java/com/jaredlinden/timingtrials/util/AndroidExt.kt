package com.jaredlinden.timingtrials.util

import android.app.Activity
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
    //val unitString =  requireActivity().getPreferences(Context.MODE_PRIVATE).getString("unit", "km")?:"km"
    val unitString = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getString("units", "km")?:"km"
    return LengthConverter(unitString)
}

inline fun <reified T: ViewModel> Fragment.getViewModel(crossinline factory: () -> T): T = T::class.java.let { clazz ->
    ViewModelProvider(this, object: ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == clazz) {
                @Suppress("UNCHECKED_CAST")
                return factory() as T
            }
            throw IllegalArgumentException("Unexpected argument: $modelClass")
        }
    }).get(clazz)
}

inline fun <reified T: ViewModel> FragmentActivity.getViewModel(crossinline factory: () -> T): T = T::class.java.let { clazz ->
    ViewModelProvider(this, object: ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == clazz) {
                @Suppress("UNCHECKED_CAST")
                return factory() as T
            }
            throw IllegalArgumentException("Unexpected argument: $modelClass")
        }
    }).get(clazz)
}

fun <T> MutableLiveData<T>.setIfNotEqual(newVal:T){
    if(value != newVal){
        value = newVal
    }
}

//fun <T> MediatorLiveData<T>.

val Activity.injector get() = (application as TimingTrialsApplication).component
val Fragment.injector get() = (requireActivity().application as TimingTrialsApplication).component