package com.bangnv.cafeorder.fragment.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.bangnv.cafeorder.activity.admin.AdminReportListActivity
import com.bangnv.cafeorder.activity.auth.ChangePasswordActivity
import com.bangnv.cafeorder.activity.auth.SignInActivity
import com.bangnv.cafeorder.constant.GlobalFunction.startActivity
import com.bangnv.cafeorder.databinding.FragmentAdminAccountBinding
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user

class AdminAccountFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentAdminAccountBinding = FragmentAdminAccountBinding.inflate(inflater, container, false)
        fragmentAdminAccountBinding.tvEmail.text = user!!.email
        fragmentAdminAccountBinding.layoutReport.setOnClickListener { onClickReport() }
        fragmentAdminAccountBinding.layoutSignOut.setOnClickListener { onClickSignOut() }
        fragmentAdminAccountBinding.layoutChangePassword.setOnClickListener { onClickChangePassword() }
        Log.d("TEST_REPLACE_FRAGMENT: ", "Admin account")
        return fragmentAdminAccountBinding.root
    }

    private fun onClickReport() {
        startActivity(requireActivity(), AdminReportListActivity::class.java)
    }

    private fun onClickChangePassword() {
        startActivity(requireActivity(), ChangePasswordActivity::class.java)
    }

    private fun onClickSignOut() {
        if (activity == null) {
            return
        }
        FirebaseAuth.getInstance().signOut()
        user = null
        startActivity(requireActivity(), SignInActivity::class.java)
        requireActivity().finishAffinity()
    }
}