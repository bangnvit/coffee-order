package com.bangnv.cafeorder.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.constant.GlobalFunction.setOnClickCopyTextToClipboard
import com.bangnv.cafeorder.databinding.ActivityOrderHistoryDetailBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate_2
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate_3
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.properties.Delegates

class OrderHistoryDetailActivity : AppCompatActivity() {

    private lateinit var mActivityOrderHistoryDetailBinding: ActivityOrderHistoryDetailBinding
    private  var mOrder: Order = Order()
    private var idOrderBundle by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityOrderHistoryDetailBinding =
            ActivityOrderHistoryDetailBinding.inflate(layoutInflater)
        setContentView(mActivityOrderHistoryDetailBinding.root)

        getDataIntent()
        initToolbar()
        getOrderDetailFirebase()

        mActivityOrderHistoryDetailBinding.tvTrackDriver.setOnClickListener { handleTrackDriver(mOrder) }
        mActivityOrderHistoryDetailBinding.tvCancelOrder.setOnClickListener { handleCancelOrder(mOrder) }
        copyFieldsClickListener()
    }

    private fun getDataIntent():Long {
        val bundle = intent.extras
        return if (bundle != null) {
            idOrderBundle = bundle[Constant.KEY_INTENT_ORDER_OBJECT] as Long
            Log.d("OrderHistoryDetail: ", idOrderBundle.toString())
            idOrderBundle
        } else {
            mActivityOrderHistoryDetailBinding.layoutOrderDetailWrap.visibility = View.GONE
            -1
        }
    }

    private fun initToolbar() {
        mActivityOrderHistoryDetailBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityOrderHistoryDetailBinding.toolbar.tvTitle.text =
            getString(R.string.order_detail_title)
        mActivityOrderHistoryDetailBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
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
            .child(idOrderBundle.toString()).child("status").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.getValue(Int::class.java)
                    mOrder.status = status ?: Constant.CODE_NEW_ORDER
                    checkStatusListener()
                }

                override fun onCancelled(error: DatabaseError) { }
            })

        ControllerApplication[this].bookingDatabaseReference
            .child(idOrderBundle.toString()).child("cancel_by").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cancelBy = snapshot.getValue(String::class.java)
                    mOrder.cancelBy = cancelBy
                    Log.d("Cancel_BY: ", cancelBy.toString())
                    if (mOrder.cancelBy == null) {
                        mActivityOrderHistoryDetailBinding.layoutCancelBy.visibility = View.GONE
                    } else {
                        mActivityOrderHistoryDetailBinding.layoutCancelBy.visibility = View.VISIBLE
                        mActivityOrderHistoryDetailBinding.tvCancelBy.text = cancelBy
                    }
                }

                override fun onCancelled(error: DatabaseError) { }
            })

    }

    private fun setOrderDetail() {
        mActivityOrderHistoryDetailBinding.tvAddress.text = mOrder.address
        mActivityOrderHistoryDetailBinding.tvFoods.text = mOrder.foods
        mActivityOrderHistoryDetailBinding.tvNote.text = mOrder.note
        val strPayment =
            if (mOrder.payment == Constant.TYPE_PAYMENT_COD) Constant.PAYMENT_METHOD_COD else Constant.PAYMENT_METHOD_WALLET
        mActivityOrderHistoryDetailBinding.tvPaymentMethod.text = strPayment

        mActivityOrderHistoryDetailBinding.tvDate.text =
            convertTimeStampToDate_2(mOrder.id)
        mActivityOrderHistoryDetailBinding.tvTime.text =
            convertTimeStampToDate_3(mOrder.id)
        mActivityOrderHistoryDetailBinding.tvOrderId.text = mOrder.id.toString()
        val strAmount: String = formatNumberWithPeriods(mOrder.amount) + Constant.CURRENCY
        mActivityOrderHistoryDetailBinding.tvSubtotal.text = strAmount

        checkStatusListener()
    }

    private fun checkStatusListener() {
        //when status
        when (mOrder.status) {
            Constant.CODE_NEW_ORDER -> { //30
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.VISIBLE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_NEW_ORDER
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_PREPARING -> { //31
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_PREPARING
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_SHIPPING -> { //32
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.VISIBLE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_SHIPPING
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_COMPLETED -> { //33
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_COMPLETED
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_CANCELLED -> { //34
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_CANCELLED
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
            }
            else -> { // 35: CODE_FAILED | or any other unexpected status
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_FAILED
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
            }
        }
    }

    private fun handleTrackDriver(order: Order) {
        Toast.makeText(
            this,
            "id: " + order.id + " - Hãy làm cái function Track Driver sau khi thêm module ship :v",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleCancelOrder(order: Order) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.msg_cancel_title))
            .setMessage(getString(R.string.msg_confirm_cancel))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                // user click cancel => status : CODE_CANCELLED, create "cancel_by" of this order on Firebase
                ControllerApplication[this].bookingDatabaseReference
                    .child(order.id.toString()).child("status").setValue(Constant.CODE_CANCELLED)
                ControllerApplication[this].bookingDatabaseReference
                    .child(order.id.toString()).child("cancel_by").setValue(order.email)
                Toast.makeText(this, "order.email: " + order.email, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.action_back), null)
            .show()
    }

    private fun copyFieldsClickListener() {
        mActivityOrderHistoryDetailBinding.layoutCopyOrderId.setOnClickCopyTextToClipboard(
            mActivityOrderHistoryDetailBinding.tvOrderId,
            this
        )
        mActivityOrderHistoryDetailBinding.layoutCopyTransitionId.setOnClickCopyTextToClipboard(
            mActivityOrderHistoryDetailBinding.tvTransitionId,
            this
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}