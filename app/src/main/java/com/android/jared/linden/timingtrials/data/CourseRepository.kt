package com.android.jared.linden.timingtrials.data

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class CourseRepository(private val courseDao: CourseDao) {
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allCourses: LiveData<List<Course>> = courseDao.getAllCourses()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(course: Course) {
        courseDao.insert(course)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(course: Course) {
        courseDao.update(course)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(course: Course) {
        courseDao.delete(course)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertOrUpdate(course: Course){
        val id = course.Id ?: 0
        if(id != 0L){
            courseDao.update(course)
        }else{
            courseDao.insert(course)
        }

    }
}