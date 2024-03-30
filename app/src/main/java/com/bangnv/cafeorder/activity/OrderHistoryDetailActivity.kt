package com.bangnv.cafeorder.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.constant.GlobalFunction.setOnClickCopyTextToClipboard
import com.bangnv.cafeorder.databinding.ActivityOrderHistoryDetailBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate_2
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate_3

class OrderHistoryDetailActivity : AppCompatActivity() {

    private var mActivityOrderHistoryDetailBinding: ActivityOrderHistoryDetailBinding? = null
    private var mOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityOrderHistoryDetailBinding =
            ActivityOrderHistoryDetailBinding.inflate(layoutInflater)
        setContentView(mActivityOrderHistoryDetailBinding!!.root)

        getDataIntent()
        initToolbar()

        setOrderDetail()
        mActivityOrderHistoryDetailBinding!!.tvTrackDriver.setOnClickListener { handleTrackDriver(mOrder!!) }
        mActivityOrderHistoryDetailBinding!!.tvCancelOrder.setOnClickListener { handleCancelOrder(mOrder!!) }
        copyFieldsClickListener()
    }

    private fun getDataIntent() {
        val bundle = intent.extras
        if (bundle != null) {
            mOrder = bundle[Constant.KEY_INTENT_ORDER_OBJECT] as Order?
        } else {
            mOrder = Order()
            mActivityOrderHistoryDetailBinding!!.layoutOrderDetailWrap.visibility = View.GONE
        }
    }

    private fun initToolbar() {
        mActivityOrderHistoryDetailBinding!!.toolbar.imgBack.visibility = View.VISIBLE
        mActivityOrderHistoryDetailBinding!!.toolbar.tvTitle.text =
            getString(R.string.order_detail_title)
        mActivityOrderHistoryDetailBinding!!.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun setOrderDetail() {
        mActivityOrderHistoryDetailBinding!!.tvAddress.text = mOrder!!.address
        mActivityOrderHistoryDetailBinding!!.tvFoods.text = mOrder!!.foods
        mActivityOrderHistoryDetailBinding!!.tvNote.text = mOrder!!.note
        val strPayment =
            if (mOrder!!.payment == Constant.TYPE_PAYMENT_COD) Constant.PAYMENT_METHOD_COD else Constant.PAYMENT_METHOD_WALLET
        mActivityOrderHistoryDetailBinding!!.tvPaymentMethod.text = strPayment

        mActivityOrderHistoryDetailBinding!!.tvDate.text = convertTimeStampToDate_2(mOrder!!.id)
        mActivityOrderHistoryDetailBinding!!.tvTime.text = convertTimeStampToDate_3(mOrder!!.id)
        mActivityOrderHistoryDetailBinding!!.tvOrderId.text = mOrder!!.id.toString()
        val strAmount: String = formatNumberWithPeriods(mOrder!!.amount) + Constant.CURRENCY
        mActivityOrderHistoryDetailBinding!!.tvSubtotal.text = strAmount

                //when status
        when (mOrder!!.status) {
            Constant.CODE_NEW_ORDER -> { //30
                mActivityOrderHistoryDetailBinding!!.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvCancelOrder.visibility = View.VISIBLE
                mActivityOrderHistoryDetailBinding!!.tvStatus.text = Constant.TEXT_NEW_ORDER
                mActivityOrderHistoryDetailBinding!!.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_PREPARING -> { //31
                mActivityOrderHistoryDetailBinding!!.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvStatus.text = Constant.TEXT_PREPARING
                mActivityOrderHistoryDetailBinding!!.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_SHIPPING -> { //32
                mActivityOrderHistoryDetailBinding!!.tvTrackDriver.visibility = View.VISIBLE
                mActivityOrderHistoryDetailBinding!!.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvStatus.text = Constant.TEXT_SHIPPING
                mActivityOrderHistoryDetailBinding!!.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_COMPLETED -> { //33
                mActivityOrderHistoryDetailBinding!!.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvStatus.text = Constant.TEXT_COMPLETED
                mActivityOrderHistoryDetailBinding!!.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
            }
            Constant.CODE_CANCELLED -> { //34
                mActivityOrderHistoryDetailBinding!!.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvStatus.text = Constant.TEXT_CANCELLED
                mActivityOrderHistoryDetailBinding!!.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
            }
            else -> { // 35: CODE_FAILED | or any other unexpected status
                mActivityOrderHistoryDetailBinding!!.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding!!.tvStatus.text = Constant.TEXT_FAILED
                mActivityOrderHistoryDetailBinding!!.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
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
        mActivityOrderHistoryDetailBinding!!.layoutCopyOrderId.setOnClickCopyTextToClipboard(
            mActivityOrderHistoryDetailBinding!!.tvOrderId,
            this
        )
        mActivityOrderHistoryDetailBinding!!.layoutCopyTransitionId.setOnClickCopyTextToClipboard(
            mActivityOrderHistoryDetailBinding!!.tvTransitionId,
            this
        )
    }
}