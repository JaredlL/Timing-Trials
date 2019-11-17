package com.android.jared.linden.timingtrials.setup

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.adapters.CourseListAdapter
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.databinding.FragmentCourseListBinding
import com.android.jared.linden.timingtrials.ui.SelectableCourseViewModel
import com.android.jared.linden.timingtrials.util.*

class SelectCourseFragment : Fragment() {


    private lateinit var viewModel: ISelectCourseViewModel
    private lateinit var adapter: CourseListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.selectCourseViewModel
        viewManager = LinearLayoutManager(context)
        adapter = CourseListAdapter(requireContext())
        adapter.editCourse = ::editCourse
        viewModel.getAllCourses().observe(viewLifecycleOwner, Observer { courses ->
            courses?.let{adapter.setCourses(it)}
        })
        adapter.courseSelected = { blobs ->
            findNavController().popBackStack()
            viewModel.setSelectedCourse(blobs)

        }

        adapter.setHasStableIds(true)

        val heading: SelectableCourseViewModel = object: SelectableCourseViewModel(Course("Course Name", 0.0, "CTT Name")){

            override var convertedLengthString = "Distance"
        }

        val binding = DataBindingUtil.inflate<FragmentCourseListBinding>(inflater, R.layout.fragment_course_list, container, false).apply{
            lifecycleOwner = (this@SelectCourseFragment)
            courseHeading.courseVm = heading
            courseHeading.checkBox.visibility = View.INVISIBLE
            courseRecyclerView.adapter = adapter
            courseRecyclerView.layoutManager = viewManager
            courseListFab.setOnClickListener {
                val action = SelectCourseFragmentDirections.actionSelectCourseFragmentToEditCourseFragment()
                findNavController().navigate(action)

                //editCourse(0)
               // dismiss()
            }
        }


        return binding.root
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        // The only reason you might override this method when using onCreateView() is
//        // to modify any dialog characteristics. For example, the dialog includes a
//        // title by default, but your custom layout might not need it. So here you can
//        // remove the dialog title, but you must call the superclass to get the Dialog.
//        val dialog = super.onCreateDialog(savedInstanceState)
//        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//
//        val root: RelativeLayout = RelativeLayout(activity).apply {
//            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        }
//        dialog.setContentView(root)
//
//        //dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        return dialog
//    }

    private fun editCourse(courseId: Long){
        val action = SelectCourseFragmentDirections.actionSelectCourseFragmentToEditCourseFragment(courseId)
        findNavController().navigate(action)
    }


    companion object {
        fun newInstance(): SelectCourseFragment {
            return SelectCourseFragment()
        }
    }
}