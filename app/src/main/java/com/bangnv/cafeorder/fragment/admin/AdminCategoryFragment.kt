package com.bangnv.cafeorder.fragment.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.admin.AdminMainActivity
import com.bangnv.cafeorder.databinding.FragmentAdminCategoryBinding
import com.bangnv.cafeorder.fragment.BaseFragment

class AdminCategoryFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mFragmentAdminCategoryBinding = FragmentAdminCategoryBinding.inflate(inflater, container, false)



        return mFragmentAdminCategoryBinding.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as AdminMainActivity?)!!.setToolBar(getString(R.string.category))
        }
    }
}