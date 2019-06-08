package com.android.jared.linden.timingtrials.viewdata
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import com.android.jared.linden.timingtrials.BR
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.RiderLight
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.databinding.ListItemCourseBinding
import com.android.jared.linden.timingtrials.databinding.ListItemRiderBinding
import com.android.jared.linden.timingtrials.databinding.ListItemTimetrialBinding
import com.android.jared.linden.timingtrials.edititem.EditItemActivity
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper
import com.android.jared.linden.timingtrials.util.ITEM_COURSE
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.ITEM_RIDER
import com.android.jared.linden.timingtrials.util.ITEM_TYPE_EXTRA
import kotlinx.android.synthetic.main.list_item_rider.view.*


class RiderViewHolder(binding: ListItemRiderBinding): GenericBaseHolder<RiderLight, ListItemRiderBinding>(binding) {

    private val _binding = binding
    //override var onLongPress : (id:Long) -> Unit = {}
    override fun bind(data: RiderLight){

        _binding.apply{
            rider = data
            riderLayout.setOnLongClickListener { onLongPress(data.id?:0L)
                true
            }

            executePendingBindings()
        }
    }
}

class RiderViewHolderFactory: GenericViewHolderFactory<RiderLight>() {
    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        val b = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false)
        return b.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<RiderLight, ListItemRiderBinding> {
        val binding = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false)
        return RiderViewHolder(binding)
    }
    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: RiderLight): View {
        val binding = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false).apply { rider = data }
        return binding.root
    }

    override fun performFabAction(fab: View) {
        fab.setOnClickListener {
            val intent = Intent(fab.context, EditItemActivity::class.java).apply {
                putExtra(ITEM_TYPE_EXTRA, ITEM_RIDER)
                putExtra(ITEM_ID_EXTRA, 0L)
            }
            fab.context.startActivity(intent)
        }
    }
}

class CourseListViewHolder(binding: ListItemCourseBinding): GenericBaseHolder<CourseListViewWrapper, ListItemCourseBinding>(binding) {
    private val _binding = binding

    //override var onLongPress = {course: CourseListViewWrapper -> Unit}

    override fun bind(data: CourseListViewWrapper){

        _binding.apply{
            courseVm = data
            checkBox.visibility = View.GONE
            courseLayout.setOnLongClickListener { onLongPress(data.course.id?:0L)
                true
            }


            executePendingBindings()
        }
    }
}

class CourseViewHolderFactory: GenericViewHolderFactory<CourseListViewWrapper>() {
    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        val binding = DataBindingUtil.inflate<ListItemCourseBinding>(layoutInflator, R.layout.list_item_course, parent, false).apply { checkBox.visibility = View.GONE }
        return binding.root
//        return createView(layoutInflator, parent, data = object: CourseListViewWrapper(Course("Course Name", 0.0, "CTT Name")){
//
//            override var convertedLengthString = "Distance"
//        })
    }

    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: CourseListViewWrapper): View {
        val binding = DataBindingUtil.inflate<ListItemCourseBinding>(layoutInflator, R.layout.list_item_course, parent, false).apply { courseVm = data; checkBox.visibility = View.GONE }
        return binding.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<CourseListViewWrapper, ListItemCourseBinding> {
        val binding = DataBindingUtil.inflate<ListItemCourseBinding>(layoutInflator, R.layout.list_item_course, parent, false)
        return CourseListViewHolder(binding)
    }

    override fun performFabAction(fab: View) {
        fab.setOnClickListener {
            val intent = Intent(fab.context, EditItemActivity::class.java).apply {
                putExtra(ITEM_TYPE_EXTRA, ITEM_COURSE)
                putExtra(ITEM_ID_EXTRA, 0L)
            }
            fab.context.startActivity(intent)
        }

    }
}


class TimeTrialListViewHolder(binding: ListItemTimetrialBinding): GenericBaseHolder<TimeTrialHeader, ListItemTimetrialBinding>(binding) {
    private val _binding = binding
    //override var onLongPress = {(rider):TimeTrialHeader -> Unit}

    override fun bind(data: TimeTrialHeader){
        _binding.apply{
            viewModel = data
            executePendingBindings()
        }
    }
}

class TimeTrialViewHolderFactory: GenericViewHolderFactory<TimeTrialHeader>() {
    override fun performFabAction(fab: View) {
        fab.visibility = View.GONE
    }

    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        val binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false)
        return binding.root
    }

    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: TimeTrialHeader): View {
        val binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false).apply { viewModel = data }
        return binding.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<TimeTrialHeader, ListItemTimetrialBinding> {
        val binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false)
        return TimeTrialListViewHolder(binding)
    }
}







