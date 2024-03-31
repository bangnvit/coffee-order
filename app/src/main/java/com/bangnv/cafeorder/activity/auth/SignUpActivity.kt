package com.bangnv.cafeorder.activity.auth

import android.os.Bundle
import android.widget.Toast
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.BaseActivity
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.gotoMainActivity
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.databinding.ActivitySignUpBinding
import com.bangnv.cafeorder.model.User
import com.bangnv.cafeorder.prefs.DataStoreManager
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import com.bangnv.cafeorder.utils.StringUtil.isValidEmail
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : BaseActivity() {

    private var mActivitySignUpBinding: ActivitySignUpBinding? = null
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivitySignUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(mActivitySignUpBinding!!.root)
        mActivitySignUpBinding!!.rdbUser.isChecked = true
        mActivitySignUpBinding!!.imgBack.setOnClickListener { onBackPressed() }
        mActivitySignUpBinding!!.layoutSignIn.setOnClickListener { finish() }
        mActivitySignUpBinding!!.btnSignUp.setOnClickListener { onClickValidateSignUp() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEmailAndPasswordListener()
    }

    private fun onClickValidateSignUp() {
        val strEmail = mActivitySignUpBinding!!.edtEmail.text.toString().trim { it <= ' ' }
        val strPassword = mActivitySignUpBinding!!.edtPassword.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(
                this@SignUpActivity,
                getString(R.string.msg_email_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (isEmpty(strPassword)) {
            Toast.makeText(
                this@SignUpActivity,
                getString(R.string.msg_password_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(
                this@SignUpActivity,
                getString(R.string.msg_email_invalid),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (mActivitySignUpBinding!!.rdbAdmin.isChecked) {
                if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    Toast.makeText(
                        this@SignUpActivity,
                        getString(R.string.msg_email_invalid_admin),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    signUpUser(strEmail, strPassword)
                }
                return
            }
            if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                Toast.makeText(
                    this@SignUpActivity,
                    getString(R.string.msg_email_invalid_user),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                signUpUser(strEmail, strPassword)
            }
        }
    }

    private fun signUpUser(email: String, password: String) {
        showProgressDialog(true)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                showProgressDialog(false)
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val userObject = User(user.email, password)
                        if (user.email != null && user.email!!.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                            userObject.isAdmin = true
                        }
                        DataStoreManager.user = userObject
                        gotoMainActivity(this)
                        finishAffinity()
                    }
                } else {
                    Toast.makeText(
                        this@SignUpActivity, getString(R.string.msg_sign_up_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setupTouchOtherToClearAllFocus() {
        mActivitySignUpBinding!!.layoutWrap.setOnClickListener {
            hideSoftKeyboard(this@SignUpActivity)
            mActivitySignUpBinding!!.edtEmail.clearFocus()
            mActivitySignUpBinding!!.edtUserName.clearFocus()
            mActivitySignUpBinding!!.edtPassword.clearFocus()
        }
    }

    private fun setupLayoutEmailAndPasswordListener() {
        //Layout Email: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivitySignUpBinding!!.layoutEmail,
            mActivitySignUpBinding!!.edtEmail,
            mActivitySignUpBinding!!.imgClear
        )
//        //Layout UserName: Listener focus, clear text icon
//        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
//            mActivitySignUpBinding!!.layoutUserName,
//            mActivitySignUpBinding!!.edtUserName,
//            mActivitySignUpBinding!!.imgClearUserName
//        )
        //Layout Password: Listener focus
        GlobalFunction.setupLayoutPasswordListeners(
            mActivitySignUpBinding!!.layoutPassword,
            mActivitySignUpBinding!!.edtPassword,
            this@SignUpActivity
        )
        //IconHideShow: Hide/show password + change icon when click
        mActivitySignUpBinding!!.imgPasswordShowHide.setOnClickListener {
            GlobalFunction.setPasswordVisibility(
                isPasswordVisible,
                mActivitySignUpBinding!!.edtPassword,
                mActivitySignUpBinding!!.imgPasswordShowHide
            )
            isPasswordVisible = !isPasswordVisible
        }
    }
}