package com.jaredlinden.timingtrials.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding =  DataBindingUtil.inflate<FragmentAboutBinding>(inflater, R.layout.fragment_about, container, false).apply{
            aboutTextView4.text = HtmlCompat.fromHtml(getString(R.string.about_blurb_4), HtmlCompat.FROM_HTML_MODE_LEGACY);
            aboutTextView4.movementMethod = LinkMovementMethod.getInstance();
        }
        return binding.root
    }
}