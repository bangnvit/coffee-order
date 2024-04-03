package com.bangnv.cafeorder.activity.auth

import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.BaseActivity
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.databinding.ActivityChangePasswordBinding
import com.bangnv.cafeorder.prefs.DataStoreManager
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

class ChangePasswordActivity : BaseActivity() {

    private lateinit var mActivityChangePasswordBinding: ActivityChangePasswordBinding
    private var isOldPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityChangePasswordBinding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(mActivityChangePasswordBinding.root)
        mActivityChangePasswordBinding.imgBack.setOnClickListener { onBackPressed() }
        mActivityChangePasswordBinding.btnChangePassword.setOnClickListener { onClickValidateChangePassword() }

        setupTouchOtherToClearAllFocus()
        setupLayoutPasswordsListener()
    }

    private fun onClickValidateChangePassword() {
        val strOldPassword = mActivityChangePasswordBinding.edtOldPassword.text.toString().trim { it <= ' ' }
        val strNewPassword = mActivityChangePasswordBinding.edtNewPassword.text.toString().trim { it <= ' ' }
        val strConfirmPassword = mActivityChangePasswordBinding.edtConfirmPassword.text.toString().trim { it <= ' ' }
        if (isEmpty(strOldPassword)) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_old_password_require), Toast.LENGTH_SHORT).show()
        } else if (isEmpty(strNewPassword)) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_new_password_require), Toast.LENGTH_SHORT).show()
        } else if (isEmpty(strConfirmPassword)) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_confirm_password_require), Toast.LENGTH_SHORT).show()
        } else if (user!!.password != strOldPassword) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_old_password_invalid), Toast.LENGTH_SHORT).show()
        } else if (strNewPassword != strConfirmPassword) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_confirm_password_invalid), Toast.LENGTH_SHORT).show()
        } else if (strOldPassword == strNewPassword) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_new_password_invalid), Toast.LENGTH_SHORT).show()
        } else {
            changePassword(strNewPassword)
        }
    }

    private fun changePassword(newPassword: String) {
        showProgressDialog(true)
        val user = FirebaseAuth.getInstance().currentUser ?: return
        user.updatePassword(newPassword)
                .addOnCompleteListener { task: Task<Void?> ->
                    showProgressDialog(false)
                    if (task.isSuccessful) {
                        Toast.makeText(this@ChangePasswordActivity,
                                getString(R.string.msg_change_password_successfully), Toast.LENGTH_SHORT).show()
                        val userLogin = DataStoreManager.user
                        userLogin!!.password = newPassword
                        DataStoreManager.user = userLogin
                        mActivityChangePasswordBinding.edtOldPassword.setText("")
                        mActivityChangePasswordBinding.edtNewPassword.setText("")
                        mActivityChangePasswordBinding.edtConfirmPassword.setText("")
                    } else {
                        // Kiểm tra nếu lý do không thành công là người dùng cần xác thực lại
                        if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                            // Yêu cầu người dùng xác thực lại trước khi thay đổi mật khẩu
                            // Hiển thị thông báo hoặc thực hiện hành động phù hợp ở đây
                            // Ví dụ: Hiển thị thông báo yêu cầu đăng nhập lại hoặc gửi email xác thực

                            //Tạm thời làm thế này đã, ưu tiên chức năng khác
                            Toast.makeText(this, "Bạn đã lâu không đăng nhập lại", Toast.LENGTH_SHORT).show()
                            Toast.makeText(this, "Hãy đăng xuất và đăng nhập lại để thực hiện chức năng này", Toast.LENGTH_SHORT).show()
                            // Đúng ra là yêu cầu cái dialog cho nhập mật khẩu lại, còn email thì đã lưu trữ, xong đẩy lại firebase xác thực
                        } else {
                            // Xử lý các lỗi khác nếu cần
                        }
                    }
                }
    }
    private fun setupTouchOtherToClearAllFocus() {
        mActivityChangePasswordBinding.layoutWrap.setOnClickListener {
            GlobalFunction.hideSoftKeyboard(this@ChangePasswordActivity)
            mActivityChangePasswordBinding.edtOldPassword.clearFocus()
            mActivityChangePasswordBinding.edtNewPassword.clearFocus()
            mActivityChangePasswordBinding.edtConfirmPassword.clearFocus()
        }
    }

    private fun setupLayoutPasswordsListener() {
        // Layout Old Password: Listener focus
        GlobalFunction.setupLayoutPasswordListeners(
            mActivityChangePasswordBinding.layoutOldPassword,
            mActivityChangePasswordBinding.edtOldPassword,
            this@ChangePasswordActivity
        )
        // Old Password: Hide/show password + change icon when click
        mActivityChangePasswordBinding.imgOldPasswordShowHide.setOnClickListener {
            GlobalFunction.setPasswordVisibility(
                isOldPasswordVisible,
                mActivityChangePasswordBinding.edtOldPassword,
                mActivityChangePasswordBinding.imgOldPasswordShowHide
            )
            isOldPasswordVisible = !isOldPasswordVisible
        }

        // Layout New Password: Listener focus
        GlobalFunction.setupLayoutPasswordListeners(
            mActivityChangePasswordBinding.layoutNewPassword,
            mActivityChangePasswordBinding.edtNewPassword,
            this@ChangePasswordActivity
        )
        // New Password: Hide/show password + change icon when click
        mActivityChangePasswordBinding.imgNewPasswordShowHide.setOnClickListener {
            GlobalFunction.setPasswordVisibility(
                isNewPasswordVisible,
                mActivityChangePasswordBinding.edtNewPassword,
                mActivityChangePasswordBinding.imgNewPasswordShowHide
            )
            isNewPasswordVisible = !isNewPasswordVisible
        }

        // Layout Confirm Password: Listener focus
        GlobalFunction.setupLayoutPasswordListeners(
            mActivityChangePasswordBinding.layoutConfirmPassword,
            mActivityChangePasswordBinding.edtConfirmPassword,
            this@ChangePasswordActivity
        )
        // Confirm Password: Hide/show password + change icon when click
        mActivityChangePasswordBinding.imgConfirmPasswordShowHide.setOnClickListener {
            GlobalFunction.setPasswordVisibility(
                isConfirmPasswordVisible,
                mActivityChangePasswordBinding.edtConfirmPassword,
                mActivityChangePasswordBinding.imgConfirmPasswordShowHide
            )
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }
    }

}