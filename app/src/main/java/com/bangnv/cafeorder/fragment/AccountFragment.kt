package com.bangnv.cafeorder.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.bangnv.cafeorder.activity.auth.ChangePasswordActivity
import com.bangnv.cafeorder.activity.OrderHistoryActivity
import com.bangnv.cafeorder.activity.auth.SignInActivity
import com.bangnv.cafeorder.constant.GlobalFunction.startActivity
import com.bangnv.cafeorder.databinding.FragmentAccountBinding
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user

class AccountFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentAccountBinding = FragmentAccountBinding.inflate(inflater, container, false)
        fragmentAccountBinding.tvEmail.text = user!!.email
        fragmentAccountBinding.layoutSignOut.setOnClickListener { onClickSignOut() }
        fragmentAccountBinding.layoutChangePassword.setOnClickListener { onClickChangePassword() }
        fragmentAccountBinding.layoutOrderHistory.setOnClickListener { onClickOrderHistory() }
        return fragmentAccountBinding.root
    }

    private fun onClickOrderHistory() {
        startActivity(requireContext(), OrderHistoryActivity::class.java)
    }

    private fun onClickChangePassword() {
        startActivity(requireContext(), ChangePasswordActivity::class.java)
    }

    private fun onClickSignOut() {
        if (activity == null) {
            return
        }
        FirebaseAuth.getInstance().signOut()
        user = null
        startActivity(requireContext(), SignInActivity::class.java)
        requireActivity().finishAffinity()
    }
}