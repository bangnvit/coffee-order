package com.bangnv.cafeorder.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.adapter.AdminViewPagerAdapter
import com.bangnv.cafeorder.databinding.ActivityAdminMainBinding

class AdminMainActivity : BaseActivity() {

    private var mActivityAdminMainBinding: ActivityAdminMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminMainBinding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(mActivityAdminMainBinding!!.root)
        mActivityAdminMainBinding!!.viewpager2.isUserInputEnabled = false
        val adminViewPagerAdapter = AdminViewPagerAdapter(this)
        mActivityAdminMainBinding!!.viewpager2.adapter = adminViewPagerAdapter
        mActivityAdminMainBinding!!.viewpager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> mActivityAdminMainBinding!!.bottomNavigation.menu.findItem(R.id.nav_home).isChecked = true
                    1 -> mActivityAdminMainBinding!!.bottomNavigation.menu.findItem(R.id.nav_feedback).isChecked = true
                    2 -> mActivityAdminMainBinding!!.bottomNavigation.menu.findItem(R.id.nav_order).isChecked = true
                    3 -> mActivityAdminMainBinding!!.bottomNavigation.menu.findItem(R.id.nav_account).isChecked = true
                }
            }
        })
        mActivityAdminMainBinding!!.bottomNavigation.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    mActivityAdminMainBinding!!.viewpager2.currentItem = 0
                }
                R.id.nav_feedback -> {
                    mActivityAdminMainBinding!!.viewpager2.currentItem = 1
                }
                R.id.nav_order -> {
                    mActivityAdminMainBinding!!.viewpager2.currentItem = 2
                }
                R.id.nav_account -> {
                    mActivityAdminMainBinding!!.viewpager2.currentItem = 3
                }
            }
            true
        }
//        Log.d("AdminActivityMain", "Test Log, đã chạy qua AdminActivityMain");
    }

    override fun onBackPressed() {
        showConfirmExitApp()
    }

    private fun showConfirmExitApp() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive { _: MaterialDialog?, _: DialogAction? -> finishAffinity() }
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show()
    }

    fun setToolBar(title: String?) {
        mActivityAdminMainBinding!!.toolbar.layoutToolbar.visibility = View.VISIBLE
        mActivityAdminMainBinding!!.toolbar.tvTitle.text = title
    }
}