package com.bangnv.cafeorder.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.MainActivity
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.changeBackgroundOnFocusChange
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionNextListener
import com.bangnv.cafeorder.constant.GlobalFunction.showToastMessage
import com.bangnv.cafeorder.databinding.FragmentFeedbackBinding
import com.bangnv.cafeorder.model.Feedback
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

class FeedbackFragment : BaseFragment() {

    private var mFragmentFeedbackBinding: FragmentFeedbackBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mFragmentFeedbackBinding = FragmentFeedbackBinding.inflate(inflater, container, false)
        mFragmentFeedbackBinding!!.edtEmail.setText(user!!.email)
        mFragmentFeedbackBinding!!.btnSendFeedback.setOnClickListener { onClickSendFeedback() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEditTextsListener()

        return mFragmentFeedbackBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as MainActivity?)!!.setToolBar(false, getString(R.string.feedback))
        }
    }

    private fun onClickSendFeedback() {
        if (activity == null) {
            return
        }
        val activity = activity as MainActivity?
        val strName = mFragmentFeedbackBinding!!.edtName.text.toString()
        val strPhone = mFragmentFeedbackBinding!!.edtPhone.text.toString()
        val strEmail = mFragmentFeedbackBinding!!.edtEmail.text.toString()
        val strComment = mFragmentFeedbackBinding!!.edtComment.text.toString()
        when {
            isEmpty(strName) -> {
                showToastMessage(activity, getString(R.string.name_require))
            }

            isEmpty(strComment) -> {
                showToastMessage(activity, getString(R.string.comment_require))
            }

            else -> {
                activity!!.showProgressDialog(true)
                val feedback = Feedback(strName, strPhone, strEmail, strComment)
                ControllerApplication[requireActivity()].feedbackDatabaseReference
                    .child(System.currentTimeMillis().toString())
                    .setValue(feedback) { _: DatabaseError?, _: DatabaseReference? ->
                        activity.showProgressDialog(false)
                        sendFeedbackSuccess()
                    }
            }
        }
    }

    private fun sendFeedbackSuccess() {
        hideSoftKeyboard(requireActivity())
        showToastMessage(activity, getString(R.string.send_feedback_success))
        mFragmentFeedbackBinding!!.edtName.setText("")
        mFragmentFeedbackBinding!!.edtPhone.setText("")
        mFragmentFeedbackBinding!!.edtComment.setText("")
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mFragmentFeedbackBinding!!.layoutWrap.setOnTouchListener { _, _ ->
            hideSoftKeyboard(requireActivity())
            mFragmentFeedbackBinding!!.edtName.clearFocus()
            mFragmentFeedbackBinding!!.edtPhone.clearFocus()
            mFragmentFeedbackBinding!!.edtComment.clearFocus()
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupLayoutEditTextsListener() {
        //Layout Name: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextListeners(
            mFragmentFeedbackBinding!!.layoutName,
            mFragmentFeedbackBinding!!.edtName,
            mFragmentFeedbackBinding!!.imgClearFullName,
            requireActivity()
        )
        //Layout Phone: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextListeners(
            mFragmentFeedbackBinding!!.layoutPhone,
            mFragmentFeedbackBinding!!.edtPhone,
            mFragmentFeedbackBinding!!.imgClearPhone,
            requireActivity()
        )
        //Layout Comment: Listener focus, (No has icon clear text)
        mFragmentFeedbackBinding!!.edtComment.changeBackgroundOnFocusChange(mFragmentFeedbackBinding!!.layoutComment)

        mFragmentFeedbackBinding!!.edtName.setOnActionNextListener {
            mFragmentFeedbackBinding!!.edtPhone.requestFocus()
        }
        mFragmentFeedbackBinding!!.edtPhone.setOnActionNextListener {
            mFragmentFeedbackBinding!!.edtComment.requestFocus()
        }
    }

}