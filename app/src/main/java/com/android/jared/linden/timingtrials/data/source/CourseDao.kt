package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.CourseLight

@Dao
interface CourseDao {

    @Insert
    fun insert(course: Course)

    @Update
    fun update(course: Course)

    @Delete
    fun delete(course: Course)

    @Query("DELETE FROM course_table") fun deleteAll()

    @Query("SELECT * from course_table ORDER BY courseName COLLATE NOCASE ASC") fun getAllCourses(): LiveData<List<Course>>

    @Query("SELECT courseName,length,cttName,id  from course_table ORDER BY courseName COLLATE NOCASE ASC") fun getAllCoursesLight(): LiveData<List<CourseLight>>

    @Query("SELECT * from course_table ORDER BY courseName COLLATE NOCASE ASC") suspend fun getAllCoursesSuspend(): List<Course>

    @Query("SELECT * from course_table WHERE Id = :courseId LIMIT 1") suspend fun getCourseSuspend(courseId: Long): Course

    @Query("SELECT * FROM course_table WHERE Id = :courseId LIMIT 1") fun getCourseById(courseId: Long): LiveData<Course>

    @Query("SELECT * FROM course_table LIMIT 1") fun getFirst(): LiveData<Course>
}