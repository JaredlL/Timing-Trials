package com.jaredlinden.timingtrials.data.roomrepo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.data.source.CourseDao
import javax.inject.Inject
import javax.inject.Singleton

interface ICourseRepository {

    val allCoursesLight: LiveData<List<Course>>

    suspend fun getAllCoursesSuspend(): List<Course>

    suspend fun getCourseSuspend(courseId: Long): Course

    suspend fun getCoursesByName(courseName: String): List<Course>

    fun getCourse(courseId: Long) : LiveData<Course>

    fun getFirst() : LiveData<Course>

    @WorkerThread
    suspend fun insert(course: Course): Long

    @WorkerThread
    suspend fun update(course: Course)

    @WorkerThread
    suspend fun delete(course: Course)


    @WorkerThread
    suspend fun insertOrUpdate(course: Course)
}

@Singleton
class RoomCourseRepository @Inject constructor(private val courseDao: CourseDao) : ICourseRepository {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    override val allCoursesLight: LiveData<List<Course>> = courseDao.getAllCoursesLive()

    override fun getCourse(courseId: Long) : LiveData<Course> {
        return when(courseId){
            0L ->  MutableLiveData<Course>(Course.createBlank())
            else ->  courseDao.getLiveCourseById(courseId)
        }
    }

    override suspend fun getCourseSuspend(courseId: Long): Course {
        return courseDao.getCourseSuspend(courseId)
    }

    override suspend fun getAllCoursesSuspend(): List<Course> {
        return courseDao.getAllCoursesSuspend()
    }

    override suspend fun getCoursesByName(courseName: String): List<Course> {
        return courseDao.getCoursesByName(courseName)
    }


    override fun getFirst() : LiveData<Course> {
       return  courseDao.getFirst()
    }

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @WorkerThread
    override suspend fun insert(course: Course):Long {
       return courseDao.insert(course)
    }

    @WorkerThread
    override suspend fun update(course: Course) {
        courseDao.update(course)
    }

    @WorkerThread
    override suspend fun delete(course: Course) {
        courseDao.delete(course)
    }

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