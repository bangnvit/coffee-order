package com.bangnv.cafeorder.activity.admin

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.BaseActivity
//import com.bangnv.cafeorder.adapter.admin.AdminViewPagerAdapter
import com.bangnv.cafeorder.constant.GlobalFunction.replaceFragment
import com.bangnv.cafeorder.databinding.ActivityAdminMainBinding
import com.bangnv.cafeorder.fragment.admin.AdminAccountFragment
import com.bangnv.cafeorder.fragment.admin.AdminCategoryFragment
import com.bangnv.cafeorder.fragment.admin.AdminFeedbackFragment
import com.bangnv.cafeorder.fragment.admin.AdminHomeFragment
import com.bangnv.cafeorder.fragment.admin.AdminOrderFragment

class AdminMainActivity : BaseActivity() {

    private lateinit var mActivityAdminMainBinding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminMainBinding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(mActivityAdminMainBinding.root)

        mActivityAdminMainBinding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(this, AdminHomeFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_category -> {
                    replaceFragment(this,AdminCategoryFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_order -> {
                    replaceFragment(this,AdminOrderFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_feedback -> {
                    replaceFragment(this,AdminFeedbackFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_account -> {
                    replaceFragment(this,AdminAccountFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
        // Set default fragment
        mActivityAdminMainBinding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    //    ViewPager2: No High Performance
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        mActivityAdminMainBinding = ActivityAdminMainBinding.inflate(layoutInflater)
//        setContentView(mActivityAdminMainBinding.root)
//        mActivityAdminMainBinding.viewpager2.isUserInputEnabled = false
//        val adminViewPagerAdapter = AdminViewPagerAdapter(this)
//        mActivityAdminMainBinding.viewpager2.adapter = adminViewPagerAdapter
//        mActivityAdminMainBinding.viewpager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                when (position) {
//                    0 -> mActivityAdminMainBinding.bottomNavigation.menu.findItem(R.id.nav_home).isChecked = true
//                    1 -> mActivityAdminMainBinding.bottomNavigation.menu.findItem(R.id.nav_category).isChecked = true
//                    2 -> mActivityAdminMainBinding.bottomNavigation.menu.findItem(R.id.nav_order).isChecked = true
//                    3 -> mActivityAdminMainBinding.bottomNavigation.menu.findItem(R.id.nav_feedback).isChecked = true
//                    4 -> mActivityAdminMainBinding.bottomNavigation.menu.findItem(R.id.nav_account).isChecked = true
//                }
//            }
//        })
//        mActivityAdminMainBinding.bottomNavigation.setOnNavigationItemSelectedListener { item: MenuItem ->
//            when (item.itemId) {
//                R.id.nav_home -> {
//                    mActivityAdminMainBinding.viewpager2.currentItem = 0
//                }
//                R.id.nav_category -> {
//                    mActivityAdminMainBinding.viewpager2.currentItem = 1
//                }
//                R.id.nav_order -> {
//                    mActivityAdminMainBinding.viewpager2.currentItem = 2
//                }
//                R.id.nav_feedback -> {
//                    mActivityAdminMainBinding.viewpager2.currentItem = 3
//                }
//                R.id.nav_account -> {
//                    mActivityAdminMainBinding.viewpager2.currentItem = 4
//                }
//            }
//            true
//        }
////        Log.d("AdminActivityMain", "Test Log, đã chạy qua AdminActivityMain");
//    }

    override fun onBackPressed() {
        showConfirmExitApp()
    }

    private fun showConfirmExitApp() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive { _: MaterialDialog?, _: DialogAction? -> finish() } // Để nếu tích hợp FCM không bị lỗi như dùng finishAffinity()
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show()
    }
}