package com.bangnv.cafeorder.adapter.admin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bangnv.cafeorder.fragment.admin.AdminAccountFragment
import com.bangnv.cafeorder.fragment.admin.AdminCategoryFragment
import com.bangnv.cafeorder.fragment.admin.AdminFeedbackFragment
import com.bangnv.cafeorder.fragment.admin.AdminHomeFragment
import com.bangnv.cafeorder.fragment.admin.AdminOrderFragment

class AdminViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminHomeFragment()
            1 -> AdminCategoryFragment()
            2 -> AdminOrderFragment()
            3 -> AdminFeedbackFragment()
            4 -> AdminAccountFragment()
            else -> AdminHomeFragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}