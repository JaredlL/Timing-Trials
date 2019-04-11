package com.android.jared.linden.timingtrials.viewdata


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.adapters.CourseListAdapter
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.databinding.FragmentCourseListBinding
import com.android.jared.linden.timingtrials.edititem.EditItemActivity
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector


/**
 * A fragment representing a list of Courses.
 * Activities containing this fragment MUST implement the
 */
class GenericListFragment : Fragment() {


    private lateinit var courseViewModel: CourseListViewModel
    private lateinit var adapter: CourseListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        courseViewModel = getViewModel { injector.courseListViewModel() }

        viewManager = LinearLayoutManager(context)
        adapter = CourseListAdapter(requireContext())
        adapter.editCourse = ::editCourse
        courseViewModel.getAllCourses().observe(viewLifecycleOwner, Observer { courses ->
            courses?.let {adapter.setCourses(it)}
        })

        val heading: CourseListViewWrapper = object: CourseListViewWrapper(Course("Course Name", 0.0, "CTT Name")){

            override var convertedLengthString = "Distance"
        }

        val binding = DataBindingUtil.inflate<FragmentCourseListBinding>(inflater, R.layout.fragment_course_list, container, false).apply{
            lifecycleOwner = (this@GenericListFragment)
            courseHeading.courseVm = heading
            courseHeading.checkBox.visibility = View.INVISIBLE
            courseRecyclerView.adapter = adapter
            courseRecyclerView.layoutManager = viewManager
            courseListFab.setOnClickListener {
                editCourse(Course.createBlank())
            }
        }

        return binding.root
    }


    private fun editCourse(course: Course){
        val intent = Intent(context, EditItemActivity::class.java).apply {
            putExtra(ITEM_TYPE_EXTRA, ITEM_COURSE)
            putExtra(ITEM_ID_EXTRA, course.id)
        }
        startActivity(intent)
    }


    companion object {
        fun newInstance(): GenericListFragment {
            return GenericListFragment()
        }
    }
}
