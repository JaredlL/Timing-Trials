package com.android.jared.linden.timingtrials.viewdata
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.databinding.ListItemCourseBinding
import com.android.jared.linden.timingtrials.databinding.ListItemRiderBinding
import com.android.jared.linden.timingtrials.databinding.ListItemTimetrialBinding
import com.android.jared.linden.timingtrials.edititem.EditItemActivity
import com.android.jared.linden.timingtrials.globalresults.GlobalResultActivity
import com.android.jared.linden.timingtrials.timetrialresults.ResultActivity
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper


class RiderViewHolder(binding: ListItemRiderBinding): GenericBaseHolder<Rider, ListItemRiderBinding>(binding) {

    private val _binding = binding
    //override var onLongPress : (id:Long) -> Unit = {}
    override fun bind(data: Rider){

        _binding.apply{
            rider = data
            riderLayout.setOnLongClickListener {

                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragment2ToEditRiderFragment(data.id ?: 0)
                Navigation.findNavController(_binding.root).navigate(action)

//                val intent = Intent(root.context, EditItemActivity::class.java).apply {
//                    putExtra(ITEM_ID_EXTRA, data.id)
//                    putExtra(ITEM_TYPE_EXTRA, ITEM_RIDER)
//                }
//                root.context.startActivity(intent)
                true
            }

            riderLayout.setOnClickListener {
                val intent = Intent(root.context, GlobalResultActivity::class.java).apply {
                    putExtra(ITEM_ID_EXTRA, data.id)
                    putExtra(ITEM_TYPE_EXTRA, ITEM_RIDER)
                }
                root.context.startActivity(intent)
            }

            executePendingBindings()
        }
    }
}

class RiderViewHolderFactory: GenericViewHolderFactory<Rider>() {
    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        val b = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false)
        return b.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<Rider, ListItemRiderBinding> {
        val binding = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false)
        return RiderViewHolder(binding)
    }
    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: Rider): View {
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

            courseLayout.setOnLongClickListener {
                val intent = Intent(root.context, EditItemActivity::class.java).apply {
                    putExtra(ITEM_ID_EXTRA, data.course.id)
                    putExtra(ITEM_TYPE_EXTRA, ITEM_COURSE)
                }
                root.context.startActivity(intent)
                true
            }

            courseLayout.setOnClickListener {
                val intent = Intent(root.context, GlobalResultActivity::class.java).apply {
                    putExtra(ITEM_ID_EXTRA, data.course.id)
                    putExtra(ITEM_TYPE_EXTRA, ITEM_COURSE)
                }
                root.context.startActivity(intent)
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
            timetrialLayout.setOnClickListener {
                val intent = Intent(this.root.context, ResultActivity::class.java).apply {
                    putExtra(ITEM_ID_EXTRA, data.id)
                }
                this.root.context.startActivity(intent)
            }
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







