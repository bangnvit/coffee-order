package com.bangnv.cafeorder.activity.auth

import android.os.Bundle
import android.widget.Toast
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.BaseActivity
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.gotoMainActivity
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.startActivity
import com.bangnv.cafeorder.databinding.ActivitySignInBinding
import com.bangnv.cafeorder.model.User
import com.bangnv.cafeorder.prefs.DataStoreManager
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import com.bangnv.cafeorder.utils.StringUtil.isValidEmail
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    private lateinit var mActivitySignInBinding: ActivitySignInBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivitySignInBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(mActivitySignInBinding.root)
        mActivitySignInBinding.rdbUser.isChecked = true
        mActivitySignInBinding.layoutSignUp.setOnClickListener {
            startActivity(this@SignInActivity, SignUpActivity::class.java)
        }
        mActivitySignInBinding.btnSignIn.setOnClickListener { onClickValidateSignIn() }
        mActivitySignInBinding.tvForgotPassword.setOnClickListener { onClickForgotPassword() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEmailAndPasswordListener()
    }

    private fun onClickForgotPassword() {
        startActivity(this, ForgotPasswordActivity::class.java)
    }

    private fun onClickValidateSignIn() {
        val strEmail = mActivitySignInBinding.edtEmail.text.toString().trim { it <= ' ' }
        val strPassword = mActivitySignInBinding.edtPassword.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(
                this@SignInActivity,
                getString(R.string.msg_email_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (isEmpty(strPassword)) {
            Toast.makeText(
                this@SignInActivity,
                getString(R.string.msg_password_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(
                this@SignInActivity,
                getString(R.string.msg_email_invalid),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (mActivitySignInBinding.rdbAdmin.isChecked) {
                if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    Toast.makeText(
                        this@SignInActivity,
                        getString(R.string.msg_email_invalid_admin),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    signInUser(strEmail, strPassword)
                }
                return
            }
            if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                Toast.makeText(
                    this@SignInActivity,
                    getString(R.string.msg_email_invalid_user),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                signInUser(strEmail, strPassword)
            }
        }
    }

    private fun signInUser(email: String, password: String) {
        showProgressDialog(true)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password)
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
                        this@SignInActivity, getString(R.string.msg_sign_in_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setupTouchOtherToClearAllFocus() {
        mActivitySignInBinding.layoutWrap.setOnClickListener {
            hideSoftKeyboard(this@SignInActivity)
            mActivitySignInBinding.edtEmail.clearFocus()
            mActivitySignInBinding.edtPassword.clearFocus()
        }
    }

    private fun setupLayoutEmailAndPasswordListener() {
        //Layout Email: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivitySignInBinding.layoutEmail,
            mActivitySignInBinding.edtEmail,
            mActivitySignInBinding.imgClear
        )
        //Layout Password: Listener focus
        GlobalFunction.setupLayoutPasswordListeners(
            mActivitySignInBinding.layoutPassword,
            mActivitySignInBinding.edtPassword,
            this@SignInActivity
        )
        //IconHideShow: Hide/show password + change icon when click
        mActivitySignInBinding.imgPasswordShowHide.setOnClickListener {
            GlobalFunction.setPasswordVisibility(
                isPasswordVisible,
                mActivitySignInBinding.edtPassword,
                mActivitySignInBinding.imgPasswordShowHide
            )
            isPasswordVisible = !isPasswordVisible
        }
    }
}