package com.android.jared.linden.timingtrials.viewdata
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.databinding.ListItemCourseBinding
import com.android.jared.linden.timingtrials.databinding.ListItemRiderBinding
import com.android.jared.linden.timingtrials.databinding.ListItemTimetrialBinding
import com.android.jared.linden.timingtrials.ui.SelectableCourseViewModel
import com.android.jared.linden.timingtrials.util.ConverterUtils
import com.android.jared.linden.timingtrials.util.EventObserver


class RiderViewHolder(binding: ListItemRiderBinding): GenericBaseHolder<Rider, ListItemRiderBinding>(binding) {

    private val _binding = binding
    //override var onLongPress : (id:Long) -> Unit = {}
    override fun bind(data: Rider){

        _binding.apply{
            rider = data
            riderLayout.setOnLongClickListener {

                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragment2ToEditRiderFragment(data.id ?: 0)
                Navigation.findNavController(_binding.root).navigate(action)
                true
            }

            riderLayout.setOnClickListener {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToGlobalResultFragment(data.id?:0, data.javaClass.simpleName)
                Navigation.findNavController(_binding.root).navigate(action)
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
            val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragment2ToEditRiderFragment( 0)
            Navigation.findNavController(fab).navigate(action)
        }
    }
}

class CourseListViewHolder(binding: ListItemCourseBinding): GenericBaseHolder<SelectableCourseViewModel, ListItemCourseBinding>(binding) {
    private val _binding = binding

    //override var onLongPress = {course: CourseListViewWrapper -> Unit}

    override fun bind(data: SelectableCourseViewModel){

        _binding.apply{
            courseVm = data
            checkBox.visibility = View.GONE

            courseLayout.setOnLongClickListener {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToEditCourseFragment(data.id ?: 0, root.context.getString(R.string.edit_course))
                Navigation.findNavController(_binding.root).navigate(action)
                true
            }

            courseLayout.setOnClickListener {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToGlobalResultFragment(data.id?:0, Course::class.java.simpleName)
                Navigation.findNavController(_binding.root).navigate(action)
            }


            executePendingBindings()
        }
    }
}

class CourseViewHolderFactory(private val unitString: String): GenericViewHolderFactory<SelectableCourseViewModel>() {
    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        val binding = DataBindingUtil.inflate<ListItemCourseBinding>(layoutInflator, R.layout.list_item_course, parent, false).apply {
            val cName =layoutInflator.context.resources.getString(R.string.course_name)
            val dist = "Distance ($unitString)"
            val cttName = layoutInflator.context.resources.getString(R.string.cttname)
            courseVm = SelectableCourseViewModel(cName, dist, cttName)
            checkBox.visibility = View.GONE

        }
        return binding.root

    }

    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: SelectableCourseViewModel): View {
        val binding = DataBindingUtil.inflate<ListItemCourseBinding>(layoutInflator, R.layout.list_item_course, parent, false).apply {
            courseVm = data
            checkBox.visibility = View.GONE
        }
        return binding.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<SelectableCourseViewModel, ListItemCourseBinding> {
        val binding = DataBindingUtil.inflate<ListItemCourseBinding>(layoutInflator, R.layout.list_item_course, parent, false)
        return CourseListViewHolder(binding)
    }

    override fun performFabAction(fab: View) {
        fab.setOnClickListener {
            val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToEditCourseFragment(0, fab.context.getString(R.string.new_course))
            Navigation.findNavController(fab).navigate(action)
        }

    }
}


class TimeTrialListViewHolder(binding: ListItemTimetrialBinding): GenericBaseHolder<TimeTrialHeader, ListItemTimetrialBinding>(binding) {
    private val _binding = binding

    override fun bind(data: TimeTrialHeader){
        _binding.apply{
            viewModel = TimeTrialListItem(data)
            timetrialLayout.setOnClickListener {

                    if(data.status == TimeTrialStatus.FINISHED){
                        val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToResultFragment(data.id?:0)
                        Navigation.findNavController(_binding.root).navigate(action)
                    }else{
                        val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSetupViewPagerFragment(data.id?:0)
                        Navigation.findNavController(_binding.root).navigate(action)
                    }



            }
            executePendingBindings()

        }
    }
}

data class TimeTrialListItem(val timeTrialHeader: TimeTrialHeader){
    val nameString: String = timeTrialHeader.ttName
    val dateString: String = if(timeTrialHeader.status == TimeTrialStatus.SETTING_UP){
        "Setting Up"
    }else{
        ConverterUtils.dateToDisplay(timeTrialHeader.startTime)
    }
}

class TimeTrialViewHolderFactory(): GenericViewHolderFactory<TimeTrialHeader>() {
    override fun performFabAction(fab: View) {
//        fab.setOnClickListener {
//            viewModel.timeTrialInsertedEvent.observe(lifeCyleOwner, EventObserver{
//                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSelectCourseFragment2(it)
//                Navigation.findNavController(fab).navigate(action)
//            })
//            viewModel.insertNewTimeTrial()
//
//        }
    }

    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        val binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false)
        return binding.root
    }

    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: TimeTrialHeader): View {
        val binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false).apply { viewModel = TimeTrialListItem(data) }
        return binding.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<TimeTrialHeader, ListItemTimetrialBinding> {
        val binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false)
        return TimeTrialListViewHolder(binding)
    }
}







