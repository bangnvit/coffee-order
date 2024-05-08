package com.bangnv.cafeorder.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.bangnv.cafeorder.activity.auth.ChangePasswordActivity
import com.bangnv.cafeorder.activity.OrderHistoryActivity
import com.bangnv.cafeorder.activity.auth.SignInActivity
import com.bangnv.cafeorder.constant.GlobalFunction.openActivity
import com.bangnv.cafeorder.databinding.FragmentAccountBinding
import com.bangnv.cafeorder.prefs.DataStoreManager
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AccountFragment : Fragment() {

    private var queryValueListener: ValueEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentAccountBinding = FragmentAccountBinding.inflate(inflater, container, false)
        fragmentAccountBinding.tvEmail.text = user!!.email
        fragmentAccountBinding.layoutSignOut.setOnClickListener { onClickSignOut() }
        fragmentAccountBinding.layoutChangePassword.setOnClickListener { onClickChangePassword() }
        fragmentAccountBinding.layoutOrderHistory.setOnClickListener { onClickOrderHistory() }
        return fragmentAccountBinding.root
    }


    private fun onClickOrderHistory() {
        openActivity(requireContext(), OrderHistoryActivity::class.java)
    }

    private fun onClickChangePassword() {
        openActivity(requireContext(), ChangePasswordActivity::class.java)
    }

    private fun onClickSignOut() {
        if (activity == null) {
            return
        }
        FirebaseAuth.getInstance().signOut()
        deleteFcmTokenOnRTDB()
        user = null

        openActivity(requireContext(), SignInActivity::class.java)
        requireActivity().finishAffinity()
    }

    private fun deleteFcmTokenOnRTDB() {
        val currentUserEmail = user!!.email
        val tokenIdToDelete = DataStoreManager.tokenId.toString()

        val userRef = ControllerApplication[requireContext()].userDatabaseReference
        val query = userRef.orderByChild("email").equalTo(currentUserEmail)


        queryValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val fcmtokenRef = userSnapshot.child("fcmtoken")

                    // Kiểm tra xem có tồn tại tokenId cần xóa không
                    if (fcmtokenRef.hasChild(tokenIdToDelete)) {
                        // Xóa tokenId khỏi nút fcmtoken
                        val tokenToDeleteRef = fcmtokenRef.child(tokenIdToDelete).ref
                        tokenToDeleteRef.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Delete Fcm Token", "Error: ${error.message}")
            }
        }
        query.addListenerForSingleValueEvent(queryValueListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove all Firebase Database listener when the Fragment is destroyed.
        queryValueListener?.let {
            val userRef = ControllerApplication[requireContext()].userDatabaseReference
            userRef.removeEventListener(it)
        }
    }
}