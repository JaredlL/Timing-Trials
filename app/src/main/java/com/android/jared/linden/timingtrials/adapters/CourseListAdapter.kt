
package com.android.jared.linden.timingtrials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.CourseLight
import com.android.jared.linden.timingtrials.databinding.ListItemCourseBinding
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper

class CourseListAdapter internal constructor(val context: Context): RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>()  {

    inner class CourseViewHolder(binding: ListItemCourseBinding): RecyclerView.ViewHolder(binding.root) {
        private val _binding = binding

        var longPress = {(course): CourseLight -> Unit}

        fun bind(courseWrapper: CourseListViewWrapper){

            _binding.apply{
                courseVm = courseWrapper
                val isSel = courseWrapper.getCourseIsSelected()
                courseLayout.setOnLongClickListener { longPress(courseWrapper.course)
                    true
                }
                executePendingBindings()
            }
        }

    }

    private var mCourses: List<CourseListViewWrapper> = listOf()
    val layoutInflater = LayoutInflater.from(context)
    var editCourse = {(course):CourseLight -> Unit}


    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        mCourses.get(position).let { course ->
            with(holder){
                itemView.tag = course
                holder.longPress = editCourse
                bind(course)

            }
        }
    }




    fun setCourses(newCourses: List<CourseListViewWrapper>){
        mCourses = newCourses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {

        val binding: ListItemCourseBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_course, parent, false)
        return CourseViewHolder(binding)


    }

    override fun getItemCount(): Int{ return mCourses.count() }


}