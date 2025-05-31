
package com.jaredlinden.timingtrials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.databinding.ListItemCourseBinding
import com.jaredlinden.timingtrials.ui.SelectableCourseViewModel
import com.jaredlinden.timingtrials.ui.SelectableCourseData
import com.jaredlinden.timingtrials.util.LengthConverter

class CourseListAdapter internal constructor(val context: Context): RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>()  {

    private var selectedId : Long? = 0
    private var mCourses: List<SelectableCourseViewModel> = listOf()
    private val layoutInflater = LayoutInflater.from(context)

    var courseLongPress = { course:Course -> Unit}
    var courseSelected = {course:Course -> Unit}

    inner class CourseViewHolder(binding: ListItemCourseBinding): RecyclerView.ViewHolder(binding.root) {
        private val _binding = binding

        var longPress = {_: Course -> Unit}

        fun bind(courseWrapper: SelectableCourseViewModel){

            _binding.apply{
                courseVm = courseWrapper
                courseLayout.setOnLongClickListener { longPress(courseWrapper.course)
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


    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        mCourses[position].let { course ->
            with(holder){
                itemView.tag = course.course.id
                holder.longPress = courseLongPress
                bind(course)
            }
        }
    }

    fun setCourses(data: SelectableCourseData, unitConverter: LengthConverter){
        if(data.selectedId != selectedId || data.courses != mCourses){
            selectedId = data.selectedId
            mCourses = data.courses.map { SelectableCourseViewModel(it, unitConverter) }
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