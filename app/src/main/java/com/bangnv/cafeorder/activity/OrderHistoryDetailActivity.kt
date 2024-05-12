package com.bangnv.cafeorder.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.constant.GlobalFunction.setOnClickCopyTextToClipboard
import com.bangnv.cafeorder.database.AppApi
import com.bangnv.cafeorder.databinding.ActivityOrderHistoryDetailBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.model.baseresponse.RetrofitClients
import com.bangnv.cafeorder.model.request.OrderRequest
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate_2
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate_3
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class OrderHistoryDetailActivity : BaseActivity() {

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
                            mOrder.deliveryFee = (snapshot.child("deliveryFee").getValue(Long::class.java) ?: 0).toInt()
                            mOrder.totalPrice = (snapshot.child("totalPrice").getValue(Long::class.java) ?: 0).toInt()
                            mOrder.transaction = snapshot.child("transaction").getValue(String::class.java) ?: ""
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
                    if (mOrder.cancelBy != null) {
                        mActivityOrderHistoryDetailBinding.tvCancelBy.text = cancelBy
                    }
                }

                override fun onCancelled(error: DatabaseError) { }
            })

        ControllerApplication[this].bookingDatabaseReference
            .child(idOrderBundle.toString()).child("cancel_reason").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cancelReason = snapshot.getValue(String::class.java)
                    mOrder.cancelReason = cancelReason
                    Log.d("cancel_reason: ", cancelReason.toString())
                    if (mOrder.cancelBy != null) {
                        mActivityOrderHistoryDetailBinding.tvCancelReason.text = cancelReason
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
        mActivityOrderHistoryDetailBinding.tvSubTotal.text = strAmount
        val strDeliveryFee: String = formatNumberWithPeriods(mOrder.deliveryFee) + Constant.CURRENCY
        mActivityOrderHistoryDetailBinding.tvDeliveryFee.text = strDeliveryFee
        val strTotalPrice: String = formatNumberWithPeriods(mOrder.totalPrice) + Constant.CURRENCY
        mActivityOrderHistoryDetailBinding.tvTotalPrice.text = strTotalPrice
        mActivityOrderHistoryDetailBinding.tvTotalPrice2.text = strTotalPrice

        if(!mOrder.transaction.isNullOrEmpty()) {
            mActivityOrderHistoryDetailBinding.layoutSubTransaction.visibility = View.VISIBLE
            mActivityOrderHistoryDetailBinding.tvTransactionId.text = mOrder.transaction
        }

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
                mActivityOrderHistoryDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.layoutCancelReason.visibility = View.GONE
            }
            Constant.CODE_PREPARING -> { //31
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_PREPARING
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
                mActivityOrderHistoryDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.layoutCancelReason.visibility = View.GONE
            }
            Constant.CODE_SHIPPING -> { //32
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.VISIBLE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_SHIPPING
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
                mActivityOrderHistoryDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.layoutCancelReason.visibility = View.GONE
            }
            Constant.CODE_COMPLETED -> { //33
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_COMPLETED
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
                mActivityOrderHistoryDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.layoutCancelReason.visibility = View.GONE
            }
            Constant.CODE_CANCELLED -> { //34
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_CANCELLED
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
                mActivityOrderHistoryDetailBinding.layoutCancelBy.visibility = View.VISIBLE
                mActivityOrderHistoryDetailBinding.layoutCancelReason.visibility = View.VISIBLE
            }
            else -> { // 35: CODE_FAILED | or any other unexpected status
                mActivityOrderHistoryDetailBinding.tvTrackDriver.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvCancelOrder.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.tvStatus.text = Constant.TEXT_FAILED
                mActivityOrderHistoryDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
                mActivityOrderHistoryDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityOrderHistoryDetailBinding.layoutCancelReason.visibility = View.GONE
            }
        }
    }

    private fun handleTrackDriver(order: Order) {
        Toast.makeText(
            this,
            "id: " + order.id + " - Tính năng đang phát triển",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleCancelOrder(order: Order) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.msg_cancel_title))
            .setMessage(getString(R.string.msg_confirm_cancel))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                // show dialog to choose reason cancel order
                showDialogCancelReason(order)
            }
            .setNegativeButton(getString(R.string.action_back), null)
            .show()
    }

    private fun showDialogCancelReason(order: Order) {
        //Init Custom Dialog
        val viewDialog = Dialog(this@OrderHistoryDetailActivity)
        viewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        viewDialog.setContentView(R.layout.layout_bottom_sheet_cancel_reason)

        // init ui
        val rdgCancelReasons = viewDialog.findViewById<RadioGroup>(R.id.rdg_cancel_reasons)
        val tvBack = viewDialog.findViewById<TextView>(R.id.tv_back)
        val tvSendCancelOrder = viewDialog.findViewById<TextView>(R.id.tv_send_cancel_order)

        // Set listeners
        tvBack.setOnClickListener { viewDialog.dismiss() }
        tvSendCancelOrder.setOnClickListener {
            // Get ID of RadioButton selected
            val selectedRadioButtonId = rdgCancelReasons.checkedRadioButtonId

            if (selectedRadioButtonId != -1) {
                // RadioButton selected
                val selectedRadioButton = viewDialog.findViewById<RadioButton>(selectedRadioButtonId)
                val selectedReason = selectedRadioButton.text.toString()

                // Realtime Database Firebase
                sendDataCancelOrderToRTDB(order, selectedReason)
                // API Server control notification
                sendNotiCancelOrderToAdmins(order, selectedReason)
            } else {
                // RadioButton not selected
                Toast.makeText(this@OrderHistoryDetailActivity, getString(R.string.select_cancel_reason), Toast.LENGTH_SHORT).show()
            }
            viewDialog.dismiss()
        }

        // Show dialog + set Customize
        viewDialog.show()
        GlobalFunction.customizeBottomSheetDialog(viewDialog)
    }

    private fun sendDataCancelOrderToRTDB(order: Order, selectedReason: String) {
        val updates = HashMap<String, Any>()
        updates["status"] = Constant.CODE_CANCELLED
        updates["cancel_by"] = order.email.toString()
        updates["cancel_reason"] = selectedReason

        ControllerApplication[this].bookingDatabaseReference
            .child(order.id.toString())
            .updateChildren(updates)
    }


    private fun sendNotiCancelOrderToAdmins(order: Order, selectedReason: String) {
        showProgressDialog(true)
        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postCancelOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).enqueue(object :
            Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

                showProgressDialog(false)
                if (response.body() != null) {
                    Log.d("Success", "Gửi thông báo thành công!")
                    // Thông báo cho cả hủy đơn thành công + thông báo
                    Toast.makeText(this@OrderHistoryDetailActivity, getString(R.string.msg_cancel_order_successfully), Toast.LENGTH_SHORT).show()
                } else if (response.code() == 500) {
                    Log.d("API", "Đối phương không đăng nhập (No token)")
//                    Toast.makeText(this@OrderHistoryDetailActivity, getString(R.string.msg_no_token), Toast.LENGTH_SHORT).show()
                }  else {
//                    Toast.makeText(this@OrderHistoryDetailActivity, getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                showProgressDialog(false)
//                Toast.makeText(this@OrderHistoryDetailActivity, "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("onError():  ", t.message.toString())
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postCancelOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postCancelOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).request().body}")
    }

    private fun copyFieldsClickListener() {
        mActivityOrderHistoryDetailBinding.layoutCopyOrderId.setOnClickCopyTextToClipboard(
            mActivityOrderHistoryDetailBinding.tvOrderId,
            this
        )
        mActivityOrderHistoryDetailBinding.layoutCopyTransactionId.setOnClickCopyTextToClipboard(
            mActivityOrderHistoryDetailBinding.tvTransactionId,
            this
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}