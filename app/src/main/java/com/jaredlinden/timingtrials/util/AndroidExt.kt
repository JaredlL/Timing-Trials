package com.jaredlinden.timingtrials.util

import android.app.Activity
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

fun Fragment.hideKeyboard(){
    val imm: InputMethodManager = context!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view!!.windowToken, 0)
}

//fun Fragment.haveOrRequestFilePermission(requestCode: Int): Boolean{
//    return if(ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
////            if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
////                Toast.makeText(requireActivity(), "Show Rational", Toast.LENGTH_SHORT).show()
////            }else{
//        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
//        false
//        // }
//    }else{
//        true
//    }
//}


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

val Activity.injector get() = (application as TimingTrialsApplication).component
val Fragment.injector get() = (requireActivity().application as TimingTrialsApplication).component