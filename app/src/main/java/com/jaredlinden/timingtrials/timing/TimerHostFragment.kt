package com.jaredlinden.timingtrials.timing

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.jaredlinden.timingtrials.BuildConfig
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.select.SELECTED_RIDERS
import com.jaredlinden.timingtrials.select.SelectRiderFragmentArgs
import com.jaredlinden.timingtrials.util.*
import kotlinx.android.synthetic.main.fragment_host.*
import org.threeten.bp.Instant

class TimerHostFragment : Fragment() {

    private val TIMERTAG = "timing_tag"
    private val STATUSTAG = "status_tag"

    private lateinit var viewModel : TimingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = requireActivity().getViewModel { injector.timingViewModel() }
        setHasOptionsMenu(true)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })

        val v = inflater.inflate(R.layout.fragment_host, container, false)

        childFragmentManager.findFragmentByTag(TIMERTAG)?: TimerFragment.newInstance().also {
            childFragmentManager.beginTransaction().apply{
                add(R.id.higherFrame, it, TIMERTAG)
                commit()
            }
        }

        childFragmentManager.findFragmentByTag(STATUSTAG)?: RiderStatusFragment.newInstance().also {
            childFragmentManager.beginTransaction().apply{
                add(R.id.lowerFrame, it, STATUSTAG)
                commit()
            }
        }

        return v

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<LongArray>(SELECTED_RIDERS)?.observe(viewLifecycleOwner, Observer{
            Toast.makeText(requireContext(), it.joinToString(), Toast.LENGTH_SHORT).show()
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<LongArray>(SELECTED_RIDERS)
        })
    }

    fun showExitDialog(){
        (requireActivity() as ITimingActivity).showExitDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_timing, menu)
        if(BuildConfig.DEBUG){
            menu.findItem(R.id.timingTest)?.isVisible = true
        }

    }

    var prevBackPress = 0L
    fun onBackPressed() {
        val tt = viewModel.timeTrial.value

        if(tt==null)
        {
            (requireActivity() as ITimingActivity).endTiming()
        }

        if(System.currentTimeMillis() > prevBackPress + 2000){
            Toast.makeText(requireContext(), "Tap again to end", Toast.LENGTH_SHORT).show()
            prevBackPress = System.currentTimeMillis()
        }else{


            viewModel.timeTrial.value?.let{
                if(it.timeTrialHeader.startTime?.toInstant()?: Instant.MAX > Instant.now()){
                    (requireActivity() as ITimingActivity).showExitDialogWithSetup()
                }else{
                    showExitDialog()
                }
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId){

            R.id.timingMenuTips-> {
                showTipsDialog()
                true
            }
            R.id.timingTest->{
                viewModel.testFinishAll()
                true
            }

            R.id.timingMenuSettings->{
                val action = TimerHostFragmentDirections.actionTimerHostFragmentToMainPrefsFragment()
                findNavController().navigate(action)
                true
            }

            R.id.timingMenuAddLateRider->{
                val ids = viewModel.timeTrial.value?.riderList?.mapNotNull { it.riderId() }?: listOf()
                val action = TimerHostFragmentDirections.actionTimerHostFragmentToSelectRiderFragment(ids.toLongArray(), true)
                findNavController().navigate(action)
                true
            }

            else -> true

        }
//        Toast.makeText(this, "ToDo...", Toast.LENGTH_SHORT).show()
//        return true
    }

    fun showTipsDialog(){
        val htmlString =
                """    
    &#8226; ${getString(R.string.tip_timing_press_timer)}<br/><br/>
    &#8226; ${getString(R.string.tip_timing_assign_event)}<br/><br/>
    &#8226; ${getString(R.string.tip_timing_press_event)}<br/><br/>
    &#8226; ${getString(R.string.tip_timing_longpress_number)}<br/><br/>
    &#8226; ${getString(R.string.tip_timing_longpress_event)}
    
 """


        val html = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)

        val mColor = ContextCompat.getColor(requireContext(), R.color.secondaryDarkColor)
        val d = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_help_outline_24)
        Utils.colorDrawable(mColor, d)

        AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.tips))

                .setIcon(d)
                .setMessage(html)
                .setPositiveButton(R.string.ok){_,_->

                }
                .show()
    }

}