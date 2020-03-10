package com.jaredlinden.timingtrials

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Test

import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.system.measureTimeMillis

class CoTest2 {

    var procList: MutableList<String> = mutableListOf()
    var queue = ConcurrentLinkedQueue<String>()
    val mutex = Mutex()

    //val da:MutableData = MutableData()

    @Test
    fun coTest() {
        procList.add("Jared")
        procList.add("Blobston")
        procList.add("Chunaks")
        procList.add("Jeffrey")
        procList.add("Marl")
        procList.add("Craxton")

        procList.forEach { queue.add(it) }

        val im = procList.toList()
        im.forEachIndexed{i,x ->
            if(i < im.size - 2){
                runBlocking {
                    doRemove2()
                }
                GlobalScope.launch {
                    doAdd2("$x $i")
                }
            }

         }

        println("Hello")
        Thread.sleep(3000)
        for (p in queue){
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


    fun doRemove2(){
        println("Removing ${queue.poll()}")
    }

    fun doAdd2(s: String){
        println("Add $s")
        queue.add(s)
    }

    suspend fun doRemove(){
       mutex.withLock {
            println("Removing ${procList[0]}")
            procList.removeAt(0)
            //println(System.currentTimeMillis())
       }
    }

    suspend fun doAdd(s: String){
        mutex.withLock {
            println("Adding $s")
            procList.add(s)
            //println(System.currentTimeMillis())
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
}