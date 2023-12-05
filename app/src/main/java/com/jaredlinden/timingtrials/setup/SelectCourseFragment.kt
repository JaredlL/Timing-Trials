package com.jaredlinden.timingtrials.setup

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.adapters.CourseListAdapter
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.databinding.FragmentCourseListBinding
import com.jaredlinden.timingtrials.ui.SelectableCourseViewModel
import com.jaredlinden.timingtrials.util.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectCourseFragment : Fragment() {

    private val setupViewModel: SetupViewModel by activityViewModels()

    private val args: SelectCourseFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        if(args.timeTrialId != -1L){
            setupViewModel.changeTimeTrial(args.timeTrialId)
        }

        (requireActivity() as IFabCallbacks).apply {
            setFabVisibility(View.VISIBLE)
            setFabImage(R.drawable.ic_add_white_24dp)
            fabClickEvent.observe(viewLifecycleOwner, EventObserver {
                if(it){
                    val action = SelectCourseFragmentDirections.actionSelectCourseFragmentToEditCourseFragment(context?.getString(R.string.new_course)?:"",0)
                    findNavController().navigate(action)
                }
            })
        }

        val viewModel = setupViewModel.selectCourseViewModel
        val viewManager = LinearLayoutManager(context)
        val adapter = CourseListAdapter(requireContext())
        adapter.editCourse = ::editCourse

        viewModel.getAllCourses().observe(viewLifecycleOwner) { courses ->
            courses?.let{adapter.setCourses(it, getLengthConverter())}
        }

        adapter.courseSelected = { blobs ->
            viewModel.setSelectedCourse(blobs)
            val action = SelectCourseFragmentDirections.actionSelectCourseFragmentToSetupViewPagerFragment()
            findNavController().navigate(action)
        }

        adapter.setHasStableIds(true)

        val unitString = getLengthConverter().unitDef.miniString

        val heading = SelectableCourseViewModel("Course Name", "Distance ($unitString)", "CTT Name")

        val binding = FragmentCourseListBinding.inflate(inflater, container, false).apply{
            lifecycleOwner = viewLifecycleOwner
            courseHeading.courseVm = heading
            courseHeading.checkBox.visibility = View.INVISIBLE
            courseRecyclerView.adapter = adapter
            courseRecyclerView.layoutManager = viewManager
        }
        return binding.root
    }


    private fun editCourse(course: Course){
        val action = SelectCourseFragmentDirections.actionSelectCourseFragmentToEditCourseFragment(resources.getString(R.string.edit_course), course.id?:0)
        findNavController().navigate(action)
    }

    companion object {
        fun newInstance(): SelectCourseFragment {
            return SelectCourseFragment()
        }
    }
}