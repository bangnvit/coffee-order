package com.bangnv.cafeorder.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.admin.AdminOrderDetailActivity
import com.bangnv.cafeorder.activity.auth.SignInActivity
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.gotoMainActivity
import com.bangnv.cafeorder.constant.GlobalFunction.openActivity
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Wakeup Server (free server will sleep after 15 minutes inactive)
        GlobalFunction.connectToServerOnRender()
        // log: connectToServerOnRender - Title of log in GlobalFunction.connectToServerOnRender()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ goToNextActivity() }, 1500)
    }

//    private fun goToNextActivity() {
//        if (user != null && !isEmpty(user!!.email)) {
////            checkUserFirebase()
//            gotoMainActivity(this)
//            finish()
//        } else {
//            openActivity(this, SignInActivity::class.java)
//            finish()
//        }
//    }

    private fun goToNextActivity() {
        val orderId = intent.getStringExtra("orderId")
        if (user != null && !isEmpty(user!!.email)) {
            if (orderId != null) {
                // Nếu có orderId từ thông báo, chuyển đến chi tiết đơn hàng
                gotoOrderDetailActivity(orderId)
            } else {
                // Không có orderId, chuyển đến MainActivity
                gotoMainActivity(this)
            }
            finish()
        } else {
            openActivity(this, SignInActivity::class.java)
            finish()
        }
    }

    private fun gotoOrderDetailActivity(orderId: String) {
        if (user!!.type == Constant.TYPE_USER_ADMIN) {
            val intent = Intent(this, AdminOrderDetailActivity::class.java).apply {
                putExtra(Constant.KEY_INTENT_ADMIN_ORDER_OBJECT, orderId.toLong())
            }
            startActivity(intent)
        } else {
            val intent = Intent(this, OrderHistoryDetailActivity::class.java).apply {
                putExtra(Constant.KEY_INTENT_ORDER_OBJECT, orderId.toLong())
            }
            startActivity(intent)
        }
    }

    // Không cần nữa
    private fun checkUserFirebase() {

        auth = FirebaseAuth.getInstance()

        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val myUser = firebaseAuth.currentUser
            if (myUser != null) {
                // Người dùng đã đăng nhập
                // Kiểm tra lại thông tin đăng nhập với Firebase Console ở đây
                Toast.makeText(this@SplashActivity,"currentUser 0: " + auth.currentUser?.email, Toast.LENGTH_SHORT).show()
                myUser.reload().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        Toast.makeText(this@SplashActivity,"currentUser 1: " + auth.currentUser?.email, Toast.LENGTH_SHORT).show()
                        if (currentUser != null) {
                            Toast.makeText(this@SplashActivity,"currentUser 2: " + auth.currentUser?.email, Toast.LENGTH_SHORT).show()
                            // Người dùng vẫn còn tồn tại, chuyển tới MainActivity
                            gotoMainActivity(this@SplashActivity)
                        } else {
                            // Người dùng không còn tồn tại, chuyển tới LoginActivity
                            Toast.makeText(this@SplashActivity,"currentUser 3: " + auth.currentUser?.email, Toast.LENGTH_SHORT).show()
                            openActivity(this@SplashActivity, SignInActivity::class.java)
                            finish()
                        }
                    } else {
                        // Đăng nhập không thành công
                        Toast.makeText(this@SplashActivity,"currentUser 4: " + auth.currentUser?.email, Toast.LENGTH_SHORT).show()
                        openActivity(this@SplashActivity, SignInActivity::class.java)
                        finish()
                    }
                }
            } else {
                // Người dùng chưa đăng nhập, chuyển tới LoginActivity
                Toast.makeText(this@SplashActivity,"currentUser 5: " + auth.currentUser?.email, Toast.LENGTH_SHORT).show()
                openActivity(this@SplashActivity, SignInActivity::class.java)
                finish()
            }
        }
        auth.addAuthStateListener(authListener)
    }

    override fun onDestroy() {
        super.onDestroy()
//        auth.removeAuthStateListener(authListener)
    }
}