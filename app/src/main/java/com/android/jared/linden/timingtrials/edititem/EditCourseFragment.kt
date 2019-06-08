package com.android.jared.linden.timingtrials.edititem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentCourseBinding
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector


class EditCourseFragment : Fragment() {

    companion object {
        fun newInstance(courseId: Long): EditCourseFragment {
            val args = Bundle().apply { putLong(ITEM_ID_EXTRA, courseId) }
            return EditCourseFragment().apply { arguments = args }
        }
    }

    private val courseId by argument<Long>(ITEM_ID_EXTRA)
    private lateinit var courseViewModel: EditCourseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        activity?.title = if(courseId == 0L) getString(R.string.add_course) else getString(R.string.edit_course)

        courseViewModel = getViewModel { injector.courseViewModel() }.apply { initialise(courseId) }
        val binding = DataBindingUtil.inflate<FragmentCourseBinding>(inflater, R.layout.fragment_course, container, false).apply {
            viewModel = courseViewModel
            lifecycleOwner = (this@EditCourseFragment)
            fab.setOnClickListener {
                courseViewModel.addOrUpdate()
                activity?.finish()
            }

        }

        return binding.root


    }


}
