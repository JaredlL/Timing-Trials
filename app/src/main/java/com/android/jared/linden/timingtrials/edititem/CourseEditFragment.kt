package com.android.jared.linden.timingtrials.edititem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentCourseBinding
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

const val COURSE_ID_EXTRA = "course_id"

class CourseEditFragment : Fragment() {

    companion object {
        fun newInstance(courseId: Long): CourseEditFragment {
            val args = Bundle().apply { putLong(COURSE_ID_EXTRA, courseId) }
            return CourseEditFragment().apply { arguments = args }
        }
    }

    private val courseId by argument<Long>(COURSE_ID_EXTRA)
    private lateinit var courseViewModel: CourseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        courseViewModel = getViewModel { injector.courseViewModel() }.apply { initialise(courseId) }
        val binding = DataBindingUtil.inflate<FragmentCourseBinding>(inflater, R.layout.fragment_course, container, false).apply {
            viewModel = courseViewModel
            lifecycleOwner = (this@CourseEditFragment)
            fab.setOnClickListener {
                courseViewModel.addOrUpdate()
                activity?.finish()
            }

        }

        return binding.root


    }


}
