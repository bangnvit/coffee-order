package com.bangnv.cafeorder.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bangnv.cafeorder.R

class SearchActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    override fun onClick(v: View?) {
//        initLayoutCategory(v.tag.toString())
    }
}