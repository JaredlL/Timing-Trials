package com.android.jared.linden.timingtrials.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun <T : Any> Fragment.argument(key: String) =
        kotlin.lazy { arguments?.get(key) as? T ?: kotlin.error("Intent Argument $key is missing") }

fun <T : Any> AppCompatActivity.argument(key: String) =
        kotlin.lazy { intent.extras[key] as? T ?: kotlin.error("Intent Argument $key is missing") }