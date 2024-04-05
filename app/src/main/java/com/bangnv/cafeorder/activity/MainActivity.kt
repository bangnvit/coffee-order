package com.bangnv.cafeorder.activity

import android.os.Bundle
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.constant.GlobalFunction.replaceFragment
import com.bangnv.cafeorder.databinding.ActivityMainBinding
import com.bangnv.cafeorder.fragment.AccountFragment
import com.bangnv.cafeorder.fragment.CartFragment
import com.bangnv.cafeorder.fragment.ContactFragment
import com.bangnv.cafeorder.fragment.FeedbackFragment
import com.bangnv.cafeorder.fragment.HomeFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : BaseActivity() {

    private lateinit var mActivityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mActivityMainBinding.root)

        mActivityMainBinding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(this, HomeFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_cart -> {
                    replaceFragment(this, CartFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_feedback -> {
                    replaceFragment(this, FeedbackFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_contact -> {
                    replaceFragment(this, ContactFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_account -> {
                    replaceFragment(this, AccountFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }

        val goToCart = intent.getBooleanExtra("goToCart", false)
        if (goToCart) {
            mActivityMainBinding.bottomNavigation.selectedItemId = R.id.nav_cart
        } else {
            mActivityMainBinding.bottomNavigation.selectedItemId = R.id.nav_home
        }




//        FirebaseMessaging.getInstance()

//        FirebaseInstallations.getInstance().id
//            .addOnCompleteListener(OnCompleteListener<Any?>{
//
//            })

    }

//    ViewPager2: No High Performance
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        mActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(mActivityMainBinding.root)
//        mActivityMainBinding.viewpager2.isUserInputEnabled = false
//        val mainViewPagerAdapter = MainViewPagerAdapter(this)
//        mActivityMainBinding.viewpager2.adapter = mainViewPagerAdapter
//        mActivityMainBinding.viewpager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                when (position) {
//                    0 -> mActivityMainBinding.bottomNavigation.menu.findItem(R.id.nav_home).isChecked = true
//                    1 -> mActivityMainBinding.bottomNavigation.menu.findItem(R.id.nav_cart).isChecked = true
//                    2 -> mActivityMainBinding.bottomNavigation.menu.findItem(R.id.nav_feedback).isChecked = true
//                    3 -> mActivityMainBinding.bottomNavigation.menu.findItem(R.id.nav_contact).isChecked = true
//                    4 -> mActivityMainBinding.bottomNavigation.menu.findItem(R.id.nav_account).isChecked = true
//                }
//            }
//        })
//        mActivityMainBinding.bottomNavigation.setOnNavigationItemSelectedListener { item: MenuItem ->
//            when (item.itemId) {
//                R.id.nav_home -> {
//                    mActivityMainBinding.viewpager2.currentItem = 0
//                }
//                R.id.nav_cart -> {
//                    mActivityMainBinding.viewpager2.currentItem = 1
//                }
//                R.id.nav_feedback -> {
//                    mActivityMainBinding.viewpager2.currentItem = 2
//                }
//                R.id.nav_contact -> {
//                    mActivityMainBinding.viewpager2.currentItem = 3
//                }
//                R.id.nav_account -> {
//                    mActivityMainBinding.viewpager2.currentItem = 4
//                }
//            }
//            true
//        }
//    }

    fun goToHome() {
//        goToCart = intent.getBooleanExtra("", false)
        mActivityMainBinding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    override fun onBackPressed() {
        showConfirmExitApp()
    }

    private fun showConfirmExitApp() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive { _: MaterialDialog?, _: DialogAction? -> finishAffinity() }// To continue FCM (if available), finishAffinity() will not run FCM
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show()
    }
}