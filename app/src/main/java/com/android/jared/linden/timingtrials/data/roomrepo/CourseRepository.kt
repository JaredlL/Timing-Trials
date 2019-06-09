package com.android.jared.linden.timingtrials.data.roomrepo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.CourseLight
import com.android.jared.linden.timingtrials.data.source.CourseDao
import javax.inject.Inject
import javax.inject.Singleton

interface ICourseRepository {

    val allCoursesLight: LiveData<List<CourseLight>>

    suspend fun getAllCoursesSuspend(): List<Course>

    suspend fun getCourseSuspend(courseId: Long): Course

    fun getCourse(courseId: Long) : LiveData<Course>

    fun getFirst() : LiveData<Course>

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(course: Course)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(course: Course)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(course: Course)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertOrUpdate(course: Course)
}

@Singleton
class RoomCourseRepository @Inject constructor(private val courseDao: CourseDao) : ICourseRepository {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    override val allCoursesLight: LiveData<List<CourseLight>> = courseDao.getAllCoursesLight()

    override fun getCourse(courseId: Long) : LiveData<Course> {
        return when(courseId){
            0L ->  MutableLiveData<Course>(Course.createBlank())
            else ->  courseDao.getCourseById(courseId)
        }
    }

    override suspend fun getCourseSuspend(courseId: Long): Course {
        return courseDao.getCourseSuspend(courseId)
    }

    override suspend fun getAllCoursesSuspend(): List<Course> {
        return courseDao.getAllCoursesSuspend()
    }

    override fun getFirst() : LiveData<Course> {
       return  courseDao.getFirst()
    }

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(course: Course) {
        courseDao.insert(course)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(course: Course) {
        courseDao.update(course)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun delete(course: Course) {
        courseDao.delete(course)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insertOrUpdate(course: Course){
        val id = course.id ?: 0
        if(id != 0L){
            courseDao.update(course)
        }else{
            courseDao.insert(course)
        }

    }
}