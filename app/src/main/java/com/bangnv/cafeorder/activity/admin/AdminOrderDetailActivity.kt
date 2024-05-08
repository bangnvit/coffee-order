package com.bangnv.cafeorder.activity.admin

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
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.BaseActivity
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.constant.GlobalFunction.setOnClickCopyTextToClipboard
import com.bangnv.cafeorder.database.AppApi
import com.bangnv.cafeorder.databinding.ActivityAdminOrderDetailBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.model.baseresponse.RetrofitClients
import com.bangnv.cafeorder.model.request.OrderRequest
import com.bangnv.cafeorder.utils.DateTimeUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap
import kotlin.properties.Delegates

class AdminOrderDetailActivity : BaseActivity() {

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
        mActivityAdminOrderDetailBinding.tvSendOrder.setOnClickListener { handleSendDeliveryOrder(mOrder) }
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
                if (mOrder.cancelBy != null) {
                    mActivityAdminOrderDetailBinding.tvCancelBy.text = cancelBy
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
                        mActivityAdminOrderDetailBinding.tvCancelReason.text = cancelReason
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
        val strAmount: String = formatNumberWithPeriods(mOrder.amount) + Constant.CURRENCY
        mActivityAdminOrderDetailBinding.tvSubTotal.text = strAmount
        val strDeliveryFee: String = formatNumberWithPeriods(mOrder.deliveryFee) + Constant.CURRENCY
        mActivityAdminOrderDetailBinding.tvDeliveryFee.text = strDeliveryFee
        val strTotalPrice: String = formatNumberWithPeriods(mOrder.totalPrice) + Constant.CURRENCY
        mActivityAdminOrderDetailBinding.tvTotalPrice.text = strTotalPrice
        mActivityAdminOrderDetailBinding.tvTotalPrice2.text = strTotalPrice

        if(!mOrder.transaction.isNullOrEmpty()) {
            mActivityAdminOrderDetailBinding.layoutSubTransaction.visibility = View.VISIBLE
            mActivityAdminOrderDetailBinding.tvTransactionId.text = mOrder.transaction
        }

        checkStatusListener()
    }

    private fun checkStatusListener() {
        //when status
        when (mOrder.status) {
            Constant.CODE_NEW_ORDER -> { //30
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.VISIBLE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_NEW_ORDER
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
                mActivityAdminOrderDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityAdminOrderDetailBinding.layoutCancelReason.visibility = View.GONE
            }
            Constant.CODE_PREPARING -> { //31
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.VISIBLE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_PREPARING
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
                mActivityAdminOrderDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityAdminOrderDetailBinding.layoutCancelReason.visibility = View.GONE
            }
            Constant.CODE_SHIPPING -> { //32
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_SHIPPING
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
                mActivityAdminOrderDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityAdminOrderDetailBinding.layoutCancelReason.visibility = View.GONE
            }
            Constant.CODE_COMPLETED -> { //33
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_COMPLETED
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_green_main_shape_corner_8)
                mActivityAdminOrderDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityAdminOrderDetailBinding.layoutCancelReason.visibility = View.GONE
            }
            Constant.CODE_CANCELLED -> { //34
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_CANCELLED
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
                mActivityAdminOrderDetailBinding.layoutCancelBy.visibility = View.VISIBLE
                mActivityAdminOrderDetailBinding.layoutCancelReason.visibility = View.VISIBLE
            }
            else -> { // 35: CODE_FAILED | or any other unexpected status
                mActivityAdminOrderDetailBinding.layoutAcceptRefuse.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvSendOrder.visibility = View.GONE
                mActivityAdminOrderDetailBinding.tvStatus.text = Constant.TEXT_FAILED
                mActivityAdminOrderDetailBinding.tvStatus.setBackgroundResource(R.drawable.bg_red_main_shape_corner_8)
                mActivityAdminOrderDetailBinding.layoutCancelBy.visibility = View.GONE
                mActivityAdminOrderDetailBinding.layoutCancelReason.visibility = View.GONE
            }
        }
    }

    private fun handleAcceptOrder(order: Order) {
        sendDataAcceptOrderToRTDB(order)
        sendNotiAcceptOrderToUser(order)
    }



    private fun sendDataAcceptOrderToRTDB(order: Order) {
        // user click accept => status : CODE_PREPARING  (prepare for this order)
        ControllerApplication[this@AdminOrderDetailActivity].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_PREPARING)
    }

    private fun sendNotiAcceptOrderToUser(order: Order) {
        // Send notification to user
        showProgressDialog(true)
        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postAcceptOrder(OrderRequest(order.email, order.id.toString())).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.body() != null) {
                    showProgressDialog(false)
                    Log.d("Success", "Gửi thông báo thành công!")
                    // Thông báo cho cả chấp nhận đơn thành công + thông báo
                    Toast.makeText(this@AdminOrderDetailActivity, getString(R.string.msg_accept_order_successfully), Toast.LENGTH_SHORT).show()
                } else {
                    showProgressDialog(false)
                    Toast.makeText(this@AdminOrderDetailActivity, getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                showProgressDialog(false)
                Toast.makeText(this@AdminOrderDetailActivity, "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("onError():  ", t.message.toString())
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postAcceptOrder(OrderRequest(order.email, order.id.toString())).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postAcceptOrder(OrderRequest(order.email, order.id.toString())).request().body}")

    }


    private fun handleRefuseOrder(order: Order) {
        AlertDialog.Builder(this@AdminOrderDetailActivity)
            .setTitle(getString(R.string.msg_refuse_title))
            .setMessage(getString(R.string.msg_confirm_refuse))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->

                // show dialog to choose reason refuse order
                showDialogRefuseReason(order)
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun showDialogRefuseReason(order: Order) {
        //Init Custom Dialog
        val viewDialog = Dialog(this@AdminOrderDetailActivity)
        viewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        viewDialog.setContentView(R.layout.layout_bottom_admin_refuse_reason)

        // init ui
        val rdgRefuseReasons = viewDialog.findViewById<RadioGroup>(R.id.rdg_refuse_reasons)
        val tvBack = viewDialog.findViewById<TextView>(R.id.tv_back)
        val tvSendRefuseOrder = viewDialog.findViewById<TextView>(R.id.tv_send_refuse_order)

        // Set listeners
        tvBack.setOnClickListener { viewDialog.dismiss() }
        tvSendRefuseOrder.setOnClickListener {
            // Get ID of RadioButton selected
            val selectedRadioButtonId = rdgRefuseReasons.checkedRadioButtonId

            if (selectedRadioButtonId != -1) {
                // RadioButton selected
                val selectedRadioButton = viewDialog.findViewById<RadioButton>(selectedRadioButtonId)
                val selectedReason = selectedRadioButton.text.toString()

                // Realtime Database Firebase
                sendDataRefuseOrderToRTDB(order, selectedReason)
                // API Server control notification
                sendNotiRefuseOrderToUser(order, selectedReason)
            } else {
                // RadioButton not selected
                Toast.makeText(this@AdminOrderDetailActivity, getString(R.string.select_refuse_reason), Toast.LENGTH_SHORT).show()
            }
            viewDialog.dismiss()
        }

        // Show dialog + set Customize
        viewDialog.show()
        GlobalFunction.customizeBottomSheetDialog(viewDialog)
    }

    private fun sendDataRefuseOrderToRTDB(order: Order, selectedReason: String) {
        // user click cancel => status : CODE_CANCELLED, create and set value for "cancel_by" of this order on Firebase
        val updates = HashMap<String, Any>()
        updates["status"] = Constant.CODE_CANCELLED
        updates["cancel_by"] = order.email.toString()
        updates["cancel_reason"] = selectedReason

        ControllerApplication[this@AdminOrderDetailActivity].bookingDatabaseReference
            .child(order.id.toString())
            .updateChildren(updates)
    }

    //Multi devices is available
    private fun sendNotiRefuseOrderToUser(order: Order, selectedReason: String) {
        showProgressDialog(true)
        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postRefuseOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.body() != null) {
                    showProgressDialog(false)
                    Log.d("Success", "Gửi thông báo thành công!")
                    // Thông báo cho cả từ chối đơn thành công + thông báo
                    Toast.makeText(this@AdminOrderDetailActivity, getString(R.string.msg_refuse_order_successfully), Toast.LENGTH_SHORT).show()
                } else {
                    showProgressDialog(false)
                    Toast.makeText(this@AdminOrderDetailActivity, getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                showProgressDialog(false)
                Toast.makeText(this@AdminOrderDetailActivity, "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("onError():  ", t.message.toString())
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postRefuseOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postRefuseOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).request().body}")

    }

    private fun handleSendDeliveryOrder(order: Order) {
        // user click send => status : CODE_SHIPPING
        ControllerApplication[this@AdminOrderDetailActivity].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_SHIPPING)

        // Cần làm thêm: yêu cầu bên vận chuyển

        sendDataDeliveryOrderToRTDB(order)
        sendNotiDeliveryOrderToUser(order) // multi devices is available
    }

    private fun sendDataDeliveryOrderToRTDB(order: Order) {
        // user click send => status : CODE_SHIPPING
        ControllerApplication[this@AdminOrderDetailActivity].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_SHIPPING)
    }

    private fun sendNotiDeliveryOrderToUser(order: Order) {
        // TO DO Cần làm thêm: yêu cầu bên vận chuyển
        // Cần làm thêm: yêu cầu bên vận chuyển
        Log.d("AdminOrderFragment: ", "Viết hàm gửi lên cho bên vận chuyển. Đã set trạng thái rồi")

        // Send notification to user
        showProgressDialog(true)
        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postSendDeliveryOrder(OrderRequest(order.email, order.id.toString())).enqueue(object :
            Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.body() != null) {
                    showProgressDialog(false)
                    Log.d("Success", "Gửi thông báo thành công!")
                    // Thông báo cho cả gửi đơn cho vận chuyên thành công + thông báo
                    Toast.makeText(this@AdminOrderDetailActivity, getString(R.string.msg_send_delivery_order_successfully), Toast.LENGTH_SHORT).show()
                } else {
                    showProgressDialog(false)
                    Toast.makeText(this@AdminOrderDetailActivity, getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                showProgressDialog(false)
                Toast.makeText(this@AdminOrderDetailActivity, "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("onError():  ", t.message.toString())
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postSendDeliveryOrder(OrderRequest(order.email, order.id.toString())).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postSendDeliveryOrder(OrderRequest(order.email, order.id.toString())).request().body}")

    }

    private fun copyFieldsClickListener() {
        mActivityAdminOrderDetailBinding.layoutCopyOrderId.setOnClickCopyTextToClipboard(
            mActivityAdminOrderDetailBinding.tvOrderId,
            this
        )
        mActivityAdminOrderDetailBinding.layoutCopyTransactionId.setOnClickCopyTextToClipboard(
            mActivityAdminOrderDetailBinding.tvTransactionId,
            this
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}