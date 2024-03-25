package com.bangnv.cafeorder.activity

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.databinding.ActivityForgotPasswordBinding
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import com.bangnv.cafeorder.utils.StringUtil.isValidEmail
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : BaseActivity() {

    private var mActivityForgotPasswordBinding: ActivityForgotPasswordBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityForgotPasswordBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(mActivityForgotPasswordBinding!!.root)
        mActivityForgotPasswordBinding!!.imgBack.setOnClickListener { onBackPressed() }
        mActivityForgotPasswordBinding!!.btnResetPassword.setOnClickListener { onClickValidateResetPassword() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEmailListener()
    }

    private fun onClickValidateResetPassword() {
        val strEmail = mActivityForgotPasswordBinding!!.edtEmail.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(
                this@ForgotPasswordActivity,
                getString(R.string.msg_email_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(
                this@ForgotPasswordActivity,
                getString(R.string.msg_email_invalid),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            resetPassword(strEmail)
        }
    }

    private fun resetPassword(email: String) {
        showProgressDialog(true)
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task: Task<Void?> ->
                showProgressDialog(false)
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        getString(R.string.msg_reset_password_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    mActivityForgotPasswordBinding!!.edtEmail.setText("")
                }
            }
    }

    private fun setupTouchOtherToClearAllFocus() {
        mActivityForgotPasswordBinding!!.layoutWrap.setOnClickListener {
            hideSoftKeyboard(this@ForgotPasswordActivity)
            mActivityForgotPasswordBinding!!.edtEmail.clearFocus()
        }
    }

    private fun setupLayoutEmailListener() {
        //Layout Email: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextListeners(
            mActivityForgotPasswordBinding!!.layoutEmail,
            mActivityForgotPasswordBinding!!.edtEmail,
            mActivityForgotPasswordBinding!!.imgClear,
            this@ForgotPasswordActivity
        )
    }

}