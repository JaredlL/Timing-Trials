package com.android.jared.linden.timingtrials.setup

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.adapters.CourseListAdapter
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.CourseLight
import com.android.jared.linden.timingtrials.databinding.FragmentCourseListBinding
import com.android.jared.linden.timingtrials.edititem.EditItemActivity
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper
import com.android.jared.linden.timingtrials.util.*

class SelectCourseFragment : DialogFragment() {


    private lateinit var viewModel: ISelectCourseViewModel
    private lateinit var adapter: CourseListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = requireActivity().getViewModel { injector.timeTrialSetupViewModel() }.selectCourseViewModel
        viewManager = LinearLayoutManager(context)
        adapter = CourseListAdapter(requireContext())
        adapter.editCourse = ::editCourse
        viewModel.getAllCourses().observe(viewLifecycleOwner, Observer { courses ->
            courses?.let{adapter.setCourses(it)}
        })




        val heading: CourseListViewWrapper = object: CourseListViewWrapper(CourseLight("Course Name", 0.0, "CTT Name")){

            override var convertedLengthString = "Distance"
        }

        val binding = DataBindingUtil.inflate<FragmentCourseListBinding>(inflater, R.layout.fragment_course_list, container, false).apply{
            lifecycleOwner = (this@SelectCourseFragment)
            courseHeading.courseVm = heading
            courseHeading.checkBox.visibility = View.INVISIBLE
            courseRecyclerView.adapter = adapter
            courseRecyclerView.layoutManager = viewManager
            courseListFab.setOnClickListener {
                editCourse(Course.createBlank().toCourseLight())
            }
        }


        viewModel.courseSelected ={ dismiss()}


        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        val dialog = super.onCreateDialog(savedInstanceState)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val root: RelativeLayout = RelativeLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        dialog.setContentView(root)

        //dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return dialog
    }

    private fun editCourse(course: CourseLight){
        val intent = Intent(context, EditItemActivity::class.java).apply {
            putExtra(ITEM_TYPE_EXTRA, ITEM_COURSE)
            putExtra(ITEM_ID_EXTRA, course.id)
        }
        startActivity(intent)
    }


    companion object {
        fun newInstance(): SelectCourseFragment {
            return SelectCourseFragment()
        }
    }
}