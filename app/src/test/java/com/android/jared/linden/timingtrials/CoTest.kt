package com.android.jared.linden.timingtrials

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Test

import org.junit.Assert.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.system.measureTimeMillis

class CoTest {

    var procList: MutableList<String> = mutableListOf()
    var queue = ConcurrentLinkedQueue<String>()

    @Test
    fun coTest() {
        procList.add("Jared")
        procList.add("Blobston")
        procList.add("Chunaks")
        procList.add("Jeffrey")
        procList.add("Marl")
        procList.add("Craxton")
        GlobalScope.launch {
            main()
        }
        println("Hello")
        Thread.sleep(3000)
        for (p in procList){
            println("List Contains $p")
        }
        println("Ending")
    }



   suspend fun main() = withContext(Dispatchers.Default) {
        val time = measureTimeMillis {
            val one = async { doSomethingUsefulOne() }
            val two = async { doSomethingUsefulTwo() }
            val three = async { doSomethingUsefulThree() }
            println("The answer is ${one.await() + two.await() + three.await()}")
        }
        println("Completed in $time ms")
    }

    val mutex = Mutex()

    suspend fun doRemove(){
        mutex.withLock {
            println("Removing ${procList[0]}")
            procList.removeAt(0)
            println(System.currentTimeMillis())
        }
    }

    suspend fun doSomethingUsefulOne(): Int {
        delay(1000L)
        doRemove()
        return 13
    }

    suspend fun doSomethingUsefulTwo(): Int {
        delay(1000L)
        doRemove()
        return 29
    }

    suspend fun doSomethingUsefulThree(): Int {
        delay(1000L)
        doRemove()
        return 8
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}