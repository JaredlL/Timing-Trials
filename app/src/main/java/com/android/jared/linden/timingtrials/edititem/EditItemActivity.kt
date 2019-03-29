package com.android.jared.linden.timingtrials.edititem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.jared.linden.timingtrials.*
import com.android.jared.linden.timingtrials.viewdata.*
import kotlinx.android.synthetic.main.activity_edit_item.*


class EditItemActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_item)

        val dataType = intent.getStringExtra(ITEM_TYPE_EXTRA)
        val itemId = intent.getLongExtra(ITEM_ID_EXTRA, 0)

        if(savedInstanceState != null) return

        val frag: Fragment = when(dataType){
           ITEM_RIDER -> RiderEditFragment.newInstance(itemId)
           ITEM_COURSE -> CourseEditFragment.newInstance(itemId)
           else -> RiderListFragment.newInstance()
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(editItemContainer.id, frag).commit()

    }


}
