package com.android.jared.linden.timingtrials.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CourseDao {

    @Insert
    fun insert(course: Course)

    @Update
    fun update(course: Course)

    @Delete
    fun delete(course: Course)

    @Query("DELETE FROM course_table") fun deleteAll()

    @Query("SELECT * from course_table ORDER BY name ASC") fun getAllCourses(): LiveData<List<Course>>

    @Query("SELECT * FROM course_table WHERE Id = :courseId LIMIT 1") fun getCourseById(courseId: Long): Course
}