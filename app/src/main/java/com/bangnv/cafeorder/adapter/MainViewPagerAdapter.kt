package com.bangnv.cafeorder.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bangnv.cafeorder.fragment.*

class MainViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> CartFragment()
            2 -> FeedbackFragment()
            3 -> ContactFragment()
            4 -> AccountFragment()
            else -> HomeFragment()
        }
    }

    override fun getItemCount(): Int {
        return 5
    }
}