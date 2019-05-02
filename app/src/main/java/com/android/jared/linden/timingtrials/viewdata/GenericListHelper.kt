package com.android.jared.linden.timingtrials.viewdata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.databinding.ListItemCourseBinding
import com.android.jared.linden.timingtrials.databinding.ListItemRiderBinding
import com.android.jared.linden.timingtrials.databinding.ListItemTimetrialBinding
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper


class RiderViewHolder(binding: ListItemRiderBinding): GenericBaseHolder<Rider, ListItemRiderBinding>(binding) {

    private val _binding = binding
    var longPress = {(rider):Rider -> Unit}
    override fun bind(data: Rider){

        _binding.apply{
            rider = data
            riderLayout.setOnLongClickListener { longPress(data)
                true
            }

            executePendingBindings()
        }
    }
}

class RiderViewHolderFactory: GenericViewHolderFactory<Rider>() {
    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        return createView(layoutInflator, parent, Rider("Name", "", "Club", 0))
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<Rider, ListItemRiderBinding> {
        var binding = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false)
        return RiderViewHolder(binding)
    }
    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: Rider): View {
        var binding = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false).apply { rider = data }
        return binding.root
    }
}

class CourseListViewHolder(binding: ListItemCourseBinding): GenericBaseHolder<CourseListViewWrapper, ListItemCourseBinding>(binding) {
    private val _binding = binding

    var longPress = {(course): Course -> Unit}

    override fun bind(data: CourseListViewWrapper){

        _binding.apply{
            courseVm = data
            courseLayout.setOnLongClickListener { longPress(data.course)
                true
            }

            executePendingBindings()
        }
    }
}

class CourseViewHolderFactory: GenericViewHolderFactory<CourseListViewWrapper>() {
    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        return createView(layoutInflator, parent, data = object: CourseListViewWrapper(Course("Course Name", 0.0, "CTT Name")){

            override var convertedLengthString = "Distance"
        })
    }

    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: CourseListViewWrapper): View {
        var binding = DataBindingUtil.inflate<ListItemCourseBinding>(layoutInflator, R.layout.list_item_course, parent, false).apply { courseVm = data }
        return binding.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<CourseListViewWrapper, ListItemCourseBinding> {
        var binding = DataBindingUtil.inflate<ListItemCourseBinding>(layoutInflator, R.layout.list_item_course, parent, false)
        return CourseListViewHolder(binding)
    }
}


class TimeTrialListViewHolder(binding: ListItemTimetrialBinding): GenericBaseHolder<TimeTrialHeader, ListItemTimetrialBinding>(binding) {
    private val _binding = binding

    override fun bind(data: TimeTrialHeader){
        _binding.apply{
            viewModel = data
            executePendingBindings()
        }
    }
}

class TimeTrialViewHolderFactory: GenericViewHolderFactory<TimeTrialHeader>() {
    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        return createView(layoutInflator, parent, TimeTrialHeader.createBlank().apply {
            ttName = "Time Trial Name"
            course = Course.createBlank().apply { courseName = "Course Name" }
        })
    }

    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: TimeTrialHeader): View {
        var binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false).apply { viewModel = data }
        return binding.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<TimeTrialHeader, ListItemTimetrialBinding> {
        var binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false)
        return TimeTrialListViewHolder(binding)
    }
}







