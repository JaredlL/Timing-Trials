package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.roomrepo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

class ImportViewModel @Inject constructor(private val riderRespository: IRiderRepository,
                                          private val courseRepository: ICourseRepository,
                                          private val timeTrialRepository: ITimeTrialRepository,
                                          private val  resultRepository: TimeTrialRiderRepository): ViewModel() {




    fun readInput(title: String?, inputStream: InputStream){
        viewModelScope.launch(Dispatchers.IO) {
            readInputIntoDb(inputStream)
        }
    }

    suspend fun readInputIntoDb(inputStream: InputStream){
        val reader = BufferedReader(InputStreamReader(inputStream))
        val firstLine = reader.readLine()

    }



}