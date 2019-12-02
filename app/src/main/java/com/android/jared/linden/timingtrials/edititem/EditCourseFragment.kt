package com.android.jared.linden.timingtrials.edititem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentCourseBinding
import com.android.jared.linden.timingtrials.data.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector


class EditCourseFragment : Fragment() {



    private val args: EditCourseFragmentArgs by navArgs()
    private lateinit var courseViewModel: EditCourseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val courseId = args.courseId

        courseViewModel = requireActivity().getViewModel { requireActivity().injector.courseViewModel() }

        courseViewModel.changeCourse(courseId)
        courseViewModel.mutableCourse.observe(viewLifecycleOwner, Observer {  })

        //Set title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = if(args.courseId == 0L) getString(R.string.add_course) else getString(R.string.edit_course)

        val binding = DataBindingUtil.inflate<FragmentCourseBinding>(inflater, R.layout.fragment_course, container, false).apply {
            viewModel = courseViewModel
            lifecycleOwner = (this@EditCourseFragment)
            fab.setOnClickListener {

                if(courseViewModel.courseName.value.isNullOrBlank()) Toast.makeText(requireContext(), getString(R.string.course_requires_name), Toast.LENGTH_SHORT).show()
                else{
                    courseViewModel.addOrUpdate()
                    findNavController().popBackStack()
                }

            }

        }

        return binding.root


    }


}
