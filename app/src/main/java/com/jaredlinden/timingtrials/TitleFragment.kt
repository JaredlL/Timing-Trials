package com.jaredlinden.timingtrials

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.databinding.FragmentTitleBinding
import com.jaredlinden.timingtrials.timing.TimingActivity
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.Utils
import com.jaredlinden.timingtrials.util.injector
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream



class TitleFragment : Fragment()
{

    private val titleViewModel: TitleViewModel by viewModels()
    private val testViewModel: TestViewModel by viewModels()

    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
            Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {

//        (requireActivity() as MainActivity).mMainFab.setOnClickListener {
//            Toast.makeText(it.context, "i", Toast.LENGTH_SHORT).show()
//        }
        //(requireActivity() as MainActivity).mMainFab.setImageResource(R.drawable.ic_timer_black_24dp)


        titleViewModel.nonFinishedTimeTrial.observe(viewLifecycleOwner, Observer {tt->
            tt?.let { timeTrial->
                if(timeTrial.status == TimeTrialStatus.IN_PROGRESS){
                    val tIntent = Intent(requireActivity(), TimingActivity::class.java)
                    startActivity(tIntent)
                }
            }
        })

        testViewModel.timingTrialsDatabase.timeTrialRiderDao().getRiderIdTimeTrialStartTime().observe(viewLifecycleOwner, Observer {res->
            res?.let {

            }

        })

        val binding =  DataBindingUtil.inflate<FragmentTitleBinding>(inflater, R.layout.fragment_title, container, false).apply{

            startTtSetupButton.setOnClickListener{


            }

            viewDatabaseButton.setOnClickListener {
                val action = TitleFragmentDirections.actionDataBaseViewPagerFragmentToDataBaseViewPagerFragment2()
                Navigation.findNavController(this.root).navigate(action)
            }

            testSetupButton.setOnClickListener {
                testViewModel.testSetup()
                findNavController().popBackStack()
            }

            testPermissionButton.setOnClickListener {
                testViewModel.insert1000()
            }

            testPermissionButton2.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requireActivity().requestPermissions(listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE).toTypedArray(), 1)
                }
            }

            testSetupButton.setOnClickListener {
                testViewModel.testSetup()
            }


            testTimingButton.setOnClickListener {

                //view?.let {  testScreenShot(it)}

                testViewModel.testTiming()
            }

            testResults1.setOnClickListener {
                testViewModel.insertFinishedTt2()
                testViewModel.testInsertedEvent.observe(viewLifecycleOwner,EventObserver{
                    it?.let {id->
                        val action = TitleFragmentDirections.actionTitleFragmentToResultFragment(id)
                        findNavController().navigate(action)
                    }
                })
            }
            testResult2.setOnClickListener {
                testViewModel.insertFinishedTt()
                testViewModel.testInsertedEvent.observe(viewLifecycleOwner,EventObserver{
                    it?.let {id->
                        val action = TitleFragmentDirections.actionTitleFragmentToResultFragment(id)
                        findNavController().navigate(action)
                    }
                })
            }
//
//            button2.setOnClickListener {
//                testViewModel.insertFinishedTt3()
//                testViewModel.testInsertedEvent.observe(viewLifecycleOwner,EventObserver{
//                    it?.let {id->
//                        val action = TitleFragmentDirections.actionTitleFragmentToResultFragment(id)
//                        findNavController().navigate(action)
//                    }
//                })
//            }
        }


        return binding.root
    }

    fun testScreenShot(view: View){


        val imgName = "Test.jpeg"
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        val path = requireActivity().getExternalFilesDir(null)

        val fileName = Utils.createFileName(imgName)

        view.draw(canvas)

        val filePath = File(path, fileName)

        val imageOut = FileOutputStream(filePath)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, imageOut)

        val contentResolver = requireActivity().contentResolver

        val values = ContentValues(3)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATA, filePath.absolutePath)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

        val insertedImageString = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        insertedImageString?.let{refreshGallery(insertedImageString)}

        val intent = Intent()
        intent.setDataAndType(insertedImageString, "image/*")
        intent.action = Intent.ACTION_VIEW
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        Timber.d("Request open  $insertedImageString")
        startActivity(intent)

    }
    fun refreshGallery(filePath: Uri) {
        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        //val newPhotoPath = "file:" + image.getAbsolutePath() // image is the created file image
        //val contentUri = Uri.fromFile(filePath)
        scanIntent.data = filePath
        requireActivity().sendBroadcast(scanIntent)
    }


   private fun showSetupDialog(timeTrial: TimeTrialHeader){
        AlertDialog.Builder(requireActivity())
                .setTitle(resources.getString(R.string.resume_setup))
                .setMessage("${resources.getString(R.string.resume_setup)} ${timeTrial.ttName}?")
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->

                    val action = TitleFragmentDirections.actionTitleFragmentToSetupViewPagerFragment2()
                    findNavController().navigate(action)
                }
                .setNegativeButton(resources.getString(R.string.start_new)){_,_->
                    titleViewModel.clearTimeTrial(timeTrial)
                    val action = TitleFragmentDirections.actionTitleFragmentToSetupViewPagerFragment2()
                    findNavController().navigate(action)
                }
                .setNeutralButton(resources.getString(R.string.dismiss)){_,_->

                }
                .create().show()
    }
}