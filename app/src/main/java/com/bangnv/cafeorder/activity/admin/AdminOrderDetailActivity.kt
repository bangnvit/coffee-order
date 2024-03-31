package com.bangnv.cafeorder.activity.admin

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.setOnClickCopyTextToClipboard
import com.bangnv.cafeorder.databinding.ActivityAdminOrderDetailBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.prefs.DataStoreManager
import com.bangnv.cafeorder.utils.DateTimeUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.properties.Delegates

class AdminOrderDetailActivity : AppCompatActivity() {

    private lateinit var mActivityAdminOrderDetailBinding: ActivityAdminOrderDetailBinding
    private  var mOrder: Order = Order()
    private var idOrderBundle by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminOrderDetailBinding = ActivityAdminOrderDetailBinding.inflate(layoutInflater)
        setContentView(mActivityAdminOrderDetailBinding.root)

        getDataIntent()
        initToolbar()
        getOrderDetailFirebase()

        mActivityAdminOrderDetailBinding.tvAcceptOrder.setOnClickListener { handleAcceptOrder(mOrder) }
        mActivityAdminOrderDetailBinding.tvSendOrder.setOnClickListener { handleSendOrder(mOrder) }
        mActivityAdminOrderDetailBinding.tvRefuseOrder.setOnClickListener { handleRefuseOrder(mOrder) }
        copyFieldsClickListener()
    }

    private fun getDataIntent():Long {
        val bundle = intent.extras
        return if (bundle != null) {
            idOrderBundle = bundle[Constant.KEY_INTENT_ADMIN_ORDER_OBJECT] as Long
            Log.d("AdminOrderDetail: ", idOrderBundle.toString())
            idOrderBundle
        } else {
            mActivityAdminOrderDetailBinding.layoutOrderDetailWrap.visibility = View.GONE
            -1
        }
    }

    private fun initToolbar() {
        mActivityAdminOrderDetailBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityAdminOrderDetailBinding.toolbar.tvTitle.text = getString(R.string.order_detail_title)
        mActivityAdminOrderDetailBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun getOrderDetailFirebase() {
        ControllerApplication[this].bookingDatabaseReference
            .child(idOrderBundle.toString()).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            mOrder.id = snapshot.child("id").getValue(Long::class.java) ?: 0
                            mOrder.name = snapshot.child("name").getValue(String::class.java)
                            mOrder.email = snapshot.child("email").getValue(String::class.java)
                            mOrder.phone = snapshot.child("phone").getValue(String::class.java)
                            mOrder.address = snapshot.child("address").getValue(String::class.java)
                            mOrder.amount = (snapshot.child("amount").getValue(Long::class.java) ?: 0).toInt()
                            mOrder.foods = snapshot.child("foods").getValue(String::class.java)
                            mOrder.payment = (snapshot.child("payment").getValue(Int::class.java) ?: 0)
                            mOrder.note = snapshot.child("note").getValue(String::class.java)
                            setOrderDetail()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) { }
                }
            )

        ControllerApplication[this].bookingDatabaseReference
            .child(idOrderBundle.toString()).child("status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(Int::class.java)
                mOrder.status = status ?: Constant.CODE_NEW_ORDER
                checkStatusListener()
            }

            override fun onCancelled(error: DatabaseError) { }
        })

        ControllerApplication[this].bookingDatabaseReference
            .child(idOrderBundle.toString()).child("cancel_by").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cancelBy = snapshot.getValue(String::class.java)
                mOrder.cancelBy = cancelBy
                Log.d("Cancel_BY: ", cancelBy.toString())
                if (mOrder.cancelBy == null) {
                    mActivityAdminOrderDetailBinding.layoutCancelBy.visibility = View.GONE
                } else {
                    mActivityAdminOrderDetailBinding.layoutCancelBy.visibility = View.VISIBLE
                    mActivityAdminOrderDetailBinding.tvCancelBy.text = cancelBy
                }
            }

            override fun onCancelled(error: DatabaseError) { }
        })

    }

    private fun setOrderDetail() {
        mActivityAdminOrderDetailBinding.tvAddress.text = mOrder.address
        mActivityAdminOrderDetailBinding.tvFoods.text = mOrder.foods
        mActivityAdminOrderDetailBinding.tvNote.text = mOrder.note
        val strPayment =
            if (mOrder.payment == Constant.TYPE_PAYMENT_COD) Constant.PAYMENT_METHOD_COD else Constant.PAYMENT_METHOD_WALLET
        mActivityAdminOrderDetailBinding.tvPaymentMethod.text = strPayment

        mActivityAdminOrderDetailBinding.tvDate.text =
            DateTimeUtils.convertTimeStampToDate_2(mOrder.id)
        mActivityAdminOrderDetailBinding.tvTime.text =
            DateTimeUtils.convertTimeStampToDate_3(mOrder.id)
        mActivityAdminOrderDetailBinding.tvOrderId.text = mOrder.id.toString()
        val strAmount: String = GlobalFunction.formatNumberWithPeriods(mOrder.amount) + Constant.CURRENCY
        mActivityAdminOrderDetailBinding.tvSubtotal.text = strAmount

        checkStatusListener()
    }

    private fun checkStatusListener() {
        //when status
        when (mOrder.status) {
            Constant.CODE_NEW_ORDER -> { //30
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.VISIBLE
                mActivityAdminOrderDetailBinding.layoutOrderDetailWrap.post {
                    mActivityAdminOrderDetailBinding.layoutOrderDetailWrap.fullScroll(View.FOCUS_DOWN)
                }
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_NEW_ORDER
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_PREPARING -> { //31
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.VISIBLE
                mActivityAdminOrderDetailBinding.layoutOrderDetailWrap.post {
                    mActivityAdminOrderDetailBinding.layoutOrderDetailWrap.fullScroll(View.FOCUS_DOWN)
                }
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_PREPARING
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_SHIPPING -> { //32
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_SHIPPING
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_COMPLETED -> { //33
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_COMPLETED
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_CANCELLED -> { //34
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_CANCELLED
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
            }
            else -> { // 35: CODE_FAILED | or any other unexpected status
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_FAILED
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
            }
        }
    }

    private fun handleAcceptOrder(order: Order) {
        // user click accept => status : CODE_PREPARING (prepare for this order)
        ControllerApplication[this@AdminOrderDetailActivity].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_PREPARING)
    }

    private fun handleRefuseOrder(order: Order) {
        AlertDialog.Builder(this@AdminOrderDetailActivity)
            .setTitle(getString(R.string.msg_refuse_title))
            .setMessage(getString(R.string.msg_confirm_refuse))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                // user click cancel => status : CODE_CANCELLED , create and set value for "cancel_by" of this order on Firebase
                ControllerApplication[this@AdminOrderDetailActivity].bookingDatabaseReference
                    .child(order.id.toString()).child("status").setValue(Constant.CODE_CANCELLED)
                // Get Email of admin refused this order. user from DataStoreManager/Companion
                ControllerApplication[this@AdminOrderDetailActivity].bookingDatabaseReference
                    .child(order.id.toString()).child("cancel_by").setValue(DataStoreManager.user!!.email)
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun handleSendOrder(order: Order) {
        // user click send => status : CODE_SHIPPING
        ControllerApplication[this@AdminOrderDetailActivity].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_SHIPPING)

        // Cần làm thêm: yêu cầu bên vận chuyển
    }

    private fun copyFieldsClickListener() {
        mActivityAdminOrderDetailBinding.layoutCopyOrderId.setOnClickCopyTextToClipboard(
            mActivityAdminOrderDetailBinding.tvOrderId,
            this
        )
        mActivityAdminOrderDetailBinding.layoutCopyTransitionId.setOnClickCopyTextToClipboard(
            mActivityAdminOrderDetailBinding.tvTransitionId,
            this
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}