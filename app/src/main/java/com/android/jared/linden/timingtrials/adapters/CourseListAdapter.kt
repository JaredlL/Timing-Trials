
package com.android.jared.linden.timingtrials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.databinding.ListItemCourseBinding
import com.android.jared.linden.timingtrials.ui.SelectableCourseViewModel
import com.android.jared.linden.timingtrials.ui.SelectableCourseData

class CourseListAdapter internal constructor(val context: Context): RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>()  {

    inner class CourseViewHolder(binding: ListItemCourseBinding): RecyclerView.ViewHolder(binding.root) {
        private val _binding = binding

        var longPress = {_: Long -> Unit}


        fun bind(courseWrapper: SelectableCourseViewModel){

            _binding.apply{
                courseVm = courseWrapper
                courseLayout.setOnLongClickListener { longPress(courseWrapper.course.id?:0)
                    true
                }

                checkBox.isChecked = (courseWrapper.course.id == selectedId)


                executePendingBindings()

                checkBox.setOnClickListener {
                    if(checkBox.isChecked != (courseWrapper.course.id == selectedId)){
                        if(checkBox.isChecked){
                            selectedId = courseWrapper.course.id ?: 0
                        }
                        courseSelected(courseWrapper.course)
                    }
                }
            }
        }

    }


    private var selectedId : Long? = 0
    private var mCourses: List<SelectableCourseViewModel> = listOf()

    val layoutInflater = LayoutInflater.from(context)
    var editCourse = {_:Long -> Unit}
    var courseSelected = {_:Course -> Unit}


    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        mCourses.get(position).let { course ->
            with(holder){
                itemView.tag = course.course.id
                holder.longPress = editCourse
                bind(course)
            }
        }
    }




    fun setCourses(data: SelectableCourseData){
        if(data.selectedId != selectedId || data.courses != mCourses){
            selectedId = data.selectedId
            mCourses = data.courses
            notifyDataSetChanged()
        }

    }

    override fun getItemId(position: Int): Long {
        return mCourses[position].course.id?:0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {

        val binding: ListItemCourseBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_course, parent, false)
        return CourseViewHolder(binding)


    }

    override fun getItemCount(): Int{ return mCourses.count() }


}