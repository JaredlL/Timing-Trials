package com.android.jared.linden.timingtrials.util

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.TimingTrialsApplication

@Suppress("UNCHECKED_CAST")
fun <T : Any> Fragment.argument(key: String) =
        lazy { arguments?.get(key) as? T ?: error("Intent Argument $key is missing") }

@Suppress("UNCHECKED_CAST")
fun <T : Any> AppCompatActivity.argument(key: String) =
        lazy { intent?.extras?.get(key) as? T ?: error("Intent Argument $key is missing") }



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

val Activity.injector get() = (application as TimingTrialsApplication).component
val Fragment.injector get() = (requireActivity().application as TimingTrialsApplication).component