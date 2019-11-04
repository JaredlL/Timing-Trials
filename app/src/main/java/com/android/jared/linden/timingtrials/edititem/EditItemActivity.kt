package com.android.jared.linden.timingtrials.edititem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.jared.linden.timingtrials.*
import com.android.jared.linden.timingtrials.data.ITEM_COURSE
import com.android.jared.linden.timingtrials.data.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.data.ITEM_RIDER
import com.android.jared.linden.timingtrials.data.ITEM_TYPE_EXTRA
import kotlinx.android.synthetic.main.activity_edit_item.*


class EditItemActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_item)

        setSupportActionBar(toolbar)

        val dataType = intent.getStringExtra(ITEM_TYPE_EXTRA)
        val itemId = intent.getLongExtra(ITEM_ID_EXTRA, 0)

        val string1 = if(itemId == 0L) "New" else "Edit"
        var string2 = ""

        if(savedInstanceState != null) return

        val frag: Fragment = when(dataType){
           ITEM_RIDER -> {
               string2 = " Rider"
               EditRiderFragment.newInstance(itemId)
           }
           ITEM_COURSE ->{
               string2 = " Course"
               EditCourseFragment.newInstance(itemId)
           }
           else -> EditRiderFragment.newInstance(itemId)
        }

        supportActionBar?.title = string1 + string2

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(editItemContainer.id, frag).commit()

    }


}
