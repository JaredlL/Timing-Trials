package com.android.jared.linden.timingtrials.viewdata.listfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.databinding.FragmentListGenericBinding
import com.android.jared.linden.timingtrials.databinding.ListItemCourseBinding
import com.android.jared.linden.timingtrials.ui.SelectableCourseViewModel
import com.android.jared.linden.timingtrials.util.getLengthConverter
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.android.jared.linden.timingtrials.viewdata.*

class CourseListFragment : Fragment() {

    private lateinit var listViewModel: ListViewModel
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var adapter: GenericListAdapter<SelectableCourseViewModel>
    private lateinit var viewFactory: GenericViewHolderFactory<SelectableCourseViewModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }

        val converter = getLengthConverter()
        viewFactory = CourseViewHolderFactory(converter.unitString)
        adapter = GenericListAdapter(requireContext(), viewFactory)
        listViewModel.filteredAllCourse.observe(viewLifecycleOwner, Observer{res->
            res?.let {adapter.setItems(it.map {
                SelectableCourseViewModel(it, converter)
            }.filter {cvm->
                cvm.distString.contains(listViewModel.liveFilter.value?.filterString?:"", ignoreCase = true)
            })}
        })

        viewManager = LinearLayoutManager(context)

        val binding = DataBindingUtil.inflate<FragmentListGenericBinding>(inflater, R.layout.fragment_list_generic, container, false).apply{
            lifecycleOwner = (this@CourseListFragment)
            listHeading.addView(viewFactory.createTitle(inflater, container), 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            genericRecyclerView.adapter = adapter
            genericRecyclerView.layoutManager = viewManager

        }

        return binding.root

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