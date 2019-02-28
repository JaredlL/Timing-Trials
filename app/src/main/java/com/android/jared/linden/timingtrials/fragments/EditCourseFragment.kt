package com.android.jared.linden.timingtrials.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.viewmodels.CourseViewModel

class EditCourseFragment : Fragment() {

    companion object {
        fun newInstance() = EditCourseFragment()
    }

    private lateinit var viewModel: CourseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_course_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CourseViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
