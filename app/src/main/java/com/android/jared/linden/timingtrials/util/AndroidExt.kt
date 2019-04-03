package com.android.jared.linden.timingtrials.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

fun <T : Any> Fragment.argument(key: String) =
        kotlin.lazy { arguments?.get(key) as? T ?: kotlin.error("Intent Argument $key is missing") }

fun <T : Any> AppCompatActivity.argument(key: String) =
        kotlin.lazy { intent.extras[key] as? T ?: kotlin.error("Intent Argument $key is missing") }



inline fun <reified T: ViewModel> Fragment.createViewModel(crossinline factory: () -> T): T = T::class.java.let { clazz ->
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