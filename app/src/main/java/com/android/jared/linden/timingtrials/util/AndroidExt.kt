package com.android.jared.linden.timingtrials.util

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.jared.linden.timingtrials.TimingTrialsApplication

fun <T : Any> Fragment.argument(key: String) =
        kotlin.lazy { arguments?.get(key) as? T ?: kotlin.error("Intent Argument $key is missing") }

fun <T : Any> AppCompatActivity.argument(key: String) =
        kotlin.lazy { intent.extras[key] as? T ?: kotlin.error("Intent Argument $key is missing") }



inline fun <reified T: ViewModel> Fragment.getViewModel(crossinline factory: () -> T): T = T::class.java.let { clazz ->
    ViewModelProviders.of(this, object: ViewModelProvider.Factory {
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
    ViewModelProviders.of(this, object: ViewModelProvider.Factory {
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