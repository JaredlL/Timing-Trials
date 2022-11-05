package com.jaredlinden.timingtrials.viewdata.listfragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.databinding.FragmentListGenericBinding
import com.jaredlinden.timingtrials.databinding.ListItemCourseBinding
import com.jaredlinden.timingtrials.ui.SelectableCourseViewModel
import com.jaredlinden.timingtrials.util.getLengthConverter
import com.jaredlinden.timingtrials.viewdata.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CourseListFragment : Fragment() {

    private val listViewModel: ListViewModel by viewModels()
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var adapter: GenericListAdapter<SelectableCourseViewModel>
    private lateinit var viewFactory: GenericViewHolderFactory<SelectableCourseViewModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        Timber.d("Create")

        val converter = getLengthConverter()
        viewFactory = CourseViewHolderFactory(converter.unitDef.key)
        adapter = GenericListAdapter(requireContext(), viewFactory)

        viewManager = LinearLayoutManager(context)

        val binding = DataBindingUtil.inflate<FragmentListGenericBinding>(inflater, R.layout.fragment_list_generic, container, false).apply{
            lifecycleOwner = (this@CourseListFragment)
            listHeading.addView(
                viewFactory.createTitle(inflater, container),
                0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT))
            genericRecyclerView.adapter = adapter
            genericRecyclerView.layoutManager = viewManager

        }

        listViewModel.filteredAllCourse.observe(viewLifecycleOwner, Observer{res->
            res?.let {adapter.setItems(it.map {
                SelectableCourseViewModel(it, converter)
            }.filter {cvm->
                cvm.distString.contains(listViewModel.liveFilter.value?.filterString?:"", ignoreCase = true)
            })}
        })

        return binding.root

    }

    override fun onDetach() {
        Timber.d("Detach")
        super.onDetach()
    }

}

class CourseListViewHolder(binding: ListItemCourseBinding): GenericBaseHolder<SelectableCourseViewModel, ListItemCourseBinding>(binding) {
    private val _binding = binding

    override fun bind(data: SelectableCourseViewModel){

        _binding.apply{
            courseVm = data
            checkBox.visibility = View.GONE

            courseLayout.setOnLongClickListener {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSheetFragment(Course::class.java.simpleName, data.id?:0)
                Navigation.findNavController(_binding.root).navigate(action)
                true
            }

            courseLayout.setOnClickListener {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToEditCourseFragment( root.context.getString(R.string.edit_course),data.id ?: 0)
                Navigation.findNavController(_binding.root).navigate(action)
            }


            executePendingBindings()
        }
    }
}

class CourseViewHolderFactory(private val unitString: String): GenericViewHolderFactory<SelectableCourseViewModel>() {
    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        val binding = DataBindingUtil.inflate<ListItemCourseBinding>(layoutInflator, R.layout.list_item_course, parent, false).apply {
            val cName =layoutInflator.context.resources.getString(R.string.name)
            val dist = "Distance ($unitString)"
            val cttName = layoutInflator.context.resources.getString(R.string.ctt_name)
            courseVm = SelectableCourseViewModel(cName, dist, cttName)
            checkBox.visibility = View.GONE
            genericTextView1.typeface = Typeface.DEFAULT_BOLD
            genericTextView2.typeface = Typeface.DEFAULT_BOLD
            genericTextView3.typeface = Typeface.DEFAULT_BOLD

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

}