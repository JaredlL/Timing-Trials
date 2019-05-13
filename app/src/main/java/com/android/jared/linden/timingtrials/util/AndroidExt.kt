package com.android.jared.linden.timingtrials.util

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
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

//inline fun <reified T1: Any, reified T2:Any> MediatorLiveData<T1>.createLink(mutableValue: MutableLiveData<T1>, observedObject: MediatorLiveData<T2>, crossinline onUpdateObject:(T1) -> Unit, crossinline onObjectUpdated:() -> T1){
//    this.addSource(mutableValue){
//        it?.let { res-> onUpdateObject(res) }
//    }
//    this.addSource(observedObject){obj->
//        obj?.let {
//            if(mutableValue.value != it)
//                mutableValue.value = onObjectUpdated()
//        }
//    }
//}

inline fun <reified T1: Any, reified T2:Any> MutableLiveData<T1>.createLink(observedObject: MediatorLiveData<T2>, crossinline updateObject:(T1) -> Pair<T1?, T2?>?, crossinline onObjectUpdated:() -> T1): MutableLiveData<T1>{
    this.let { rec->
        val valMed: MediatorLiveData<T1> = MediatorLiveData<T1>().apply {
            addSource(rec){
                val new = updateObject(it)
                 if(new?.first != it){
                     observedObject.value = new?.second
                 }
            }
            addSource(observedObject){obj->
                obj?.let {
                    if(rec.value != it)
                        rec.value = onObjectUpdated()
                }
            }
        }.also { it.observeForever {  } }
        return rec
    }


}

val Activity.injector get() = (application as TimingTrialsApplication).component
val Fragment.injector get() = (requireActivity().application as TimingTrialsApplication).component