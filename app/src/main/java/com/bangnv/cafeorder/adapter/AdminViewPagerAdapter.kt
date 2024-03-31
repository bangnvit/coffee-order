package com.bangnv.cafeorder.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bangnv.cafeorder.fragment.AdminAccountFragment
import com.bangnv.cafeorder.fragment.AdminFeedbackFragment
import com.bangnv.cafeorder.fragment.AdminHomeFragment
import com.bangnv.cafeorder.fragment.AdminOrderFragment

class AdminViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminHomeFragment()
            1 -> AdminFeedbackFragment()
            2 -> AdminOrderFragment()
            3 -> AdminAccountFragment()
            else -> AdminHomeFragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}