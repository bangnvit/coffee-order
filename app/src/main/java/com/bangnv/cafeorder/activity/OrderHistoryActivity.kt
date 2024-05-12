package com.bangnv.cafeorder.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.adapter.OrderAdapter
import com.bangnv.cafeorder.adapter.OrderAdapter.IClickOrderHistoryListener
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.Constant.Companion.CODE_CANCELLED
import com.bangnv.cafeorder.constant.Constant.Companion.CODE_COMPLETED
import com.bangnv.cafeorder.constant.Constant.Companion.CODE_FAILED
import com.bangnv.cafeorder.constant.Constant.Companion.CODE_NEW_ORDER
import com.bangnv.cafeorder.constant.Constant.Companion.CODE_PREPARING
import com.bangnv.cafeorder.constant.Constant.Companion.CODE_SHIPPING
import com.bangnv.cafeorder.constant.Constant.Companion.TEXT_ALL_ORDER
import com.bangnv.cafeorder.constant.Constant.Companion.TEXT_CANCELLED
import com.bangnv.cafeorder.constant.Constant.Companion.TEXT_COMPLETED
import com.bangnv.cafeorder.constant.Constant.Companion.TEXT_FAILED
import com.bangnv.cafeorder.constant.Constant.Companion.TEXT_NEW_ORDER
import com.bangnv.cafeorder.constant.Constant.Companion.TEXT_PREPARING
import com.bangnv.cafeorder.constant.Constant.Companion.TEXT_SHIPPING
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.addMyTabs
import com.bangnv.cafeorder.constant.GlobalFunction.openActivity
import com.bangnv.cafeorder.database.AppApi
import com.bangnv.cafeorder.databinding.ActivityOrderHistoryBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.model.baseresponse.RetrofitClients
import com.bangnv.cafeorder.model.request.OrderRequest
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class OrderHistoryActivity : BaseActivity() {

    private lateinit var mActivityOrderHistoryBinding: ActivityOrderHistoryBinding
    private lateinit var mOrderAdapter: OrderAdapter
    private var mListOrder: MutableList<Order> = mutableListOf()        // Full Data
    private var displayedOrders: MutableList<Order> = mutableListOf()   // display Data filtered
    private var tempOrders: MutableList<Order> = mutableListOf()        // Temp

    private lateinit var bookingValueListener: ValueEventListener

    private var isLoading = false
    private var isLastPage = false
    private var currentPage = 1
    private var totalPage by Delegates.notNull<Int>()
    private var lastItemKey by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityOrderHistoryBinding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(mActivityOrderHistoryBinding.root)
        initToolbar()
        initView()
        initTabLayout()
        getListOrders()
//        getListOrdersFirst()
        calculatorTotalPage()
        tabLayoutTabSelectedListener()
    }

    private fun initToolbar() {
        mActivityOrderHistoryBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityOrderHistoryBinding.toolbar.imgCart.visibility = View.GONE
        mActivityOrderHistoryBinding.toolbar.tvTitle.text = getString(R.string.order_history)
        mActivityOrderHistoryBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun initView() {
        val linearLayoutManager = LinearLayoutManager(this)
        mActivityOrderHistoryBinding.rcvOrderHistory.layoutManager = linearLayoutManager
//        mActivityOrderHistoryBinding.rcvOrderHistory.addOnScrollListener(object : PaginationLinearScrollListener(linearLayoutManager) {
//            override fun loadMoreItems() {
//                this@OrderHistoryActivity.isLoading = true
//                showProgressDialog(true)
//                currentPage += 1
//                loadNextPage()
//            }
//
//            override val isLoading: Boolean
//                get() = this@OrderHistoryActivity.isLoading
//
//            override val isLastPage: Boolean
//                get() = this@OrderHistoryActivity.isLastPage
//        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadNextPage() {
        getListOrdersNext(lastItemKey)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (tempOrders.size != 0){
                mListOrder.addAll(tempOrders)
                mOrderAdapter.notifyDataSetChanged()
            }
            isLoading = false
            showProgressDialog(false)
            if(currentPage == totalPage){
                isLastPage = true
            }
        }, 300)
    }


    private fun initTabLayout() {
        mActivityOrderHistoryBinding.tabLayoutStatus.addMyTabs(
            TEXT_ALL_ORDER,
            TEXT_NEW_ORDER,
            TEXT_PREPARING,
            TEXT_SHIPPING,
            TEXT_COMPLETED,
            TEXT_CANCELLED,
            TEXT_FAILED,
            selectedTab = TEXT_ALL_ORDER
        )
    }

    private fun calculatorTotalPage() {
        ControllerApplication[this].bookingDatabaseReference.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totalItemCount = snapshot.childrenCount
                    totalPage = if (totalItemCount % 10 == 0L) {
                        (totalItemCount / Constant.MAX_ITEM_PER_LOAD_LINEAR).toInt()
                    } else {
                        ((totalItemCount / Constant.MAX_ITEM_PER_LOAD_LINEAR) + 1).toInt()
                    }
                    Log.d("totalPage = ", totalPage.toString())
                }

                override fun onCancelled(error: DatabaseError) {}
            }
        )
    }


    private fun getListOrders() {
        bookingValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mListOrder.clear()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        val strEmail = user!!.email
                        if (strEmail.equals(order.email, ignoreCase = true)) {
                            if(order.status != 1){
                                mListOrder.add(0, order)
                            }
                        }
                    }
                }
                mOrderAdapter = OrderAdapter(
                    this@OrderHistoryActivity,
                    mListOrder,
                    object : IClickOrderHistoryListener {
                        override fun trackDriver(order: Order) {
                            handleTrackDriver(order)
                        }

                        override fun cancelOrder(order: Order) {
                            handleCancelOrder(order)
                        }

                        override fun onClickItemOrder(order: Order) {
                            goToOrderHistoryDetail(order.id)
                        }
                    })
                mActivityOrderHistoryBinding.rcvOrderHistory.adapter = mOrderAdapter
                updateDisplayedOrders()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ControllerApplication[this].bookingDatabaseReference.addValueEventListener(bookingValueListener)
    }

    //Test
    private fun getListOrdersFirst() {
        bookingValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mListOrder.clear()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        val strEmail = user!!.email
                        if (strEmail.equals(order.email, ignoreCase = true)) {
                            mListOrder.add(order)
                        }
                    }
                }
                lastItemKey = mListOrder.last().id
                mOrderAdapter = OrderAdapter(
                    this@OrderHistoryActivity,
                    mListOrder,
                    object : IClickOrderHistoryListener {
                        override fun trackDriver(order: Order) {
                            handleTrackDriver(order)
                        }

                        override fun cancelOrder(order: Order) {
                            handleCancelOrder(order)
                        }

                        override fun onClickItemOrder(order: Order) {
                            goToOrderHistoryDetail(order.id)
                        }
                    })
                mActivityOrderHistoryBinding.rcvOrderHistory.adapter = mOrderAdapter
                updateDisplayedOrders()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ControllerApplication[this].bookingDatabaseReference
            .orderByKey()
            .limitToFirst(Constant.MAX_ITEM_PER_LOAD_LINEAR) // Giới hạn lấy ra MAX_ITEM_PER_LOAD_LINEAR mục
            .addValueEventListener(bookingValueListener)
    }

    private fun getListOrdersNext(lastItemKeyLoaded : Long) {
        bookingValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tempOrders.clear()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        val strEmail = user!!.email
                        if (strEmail.equals(order.email, ignoreCase = true)) {
                            tempOrders.add(0, order)
                        }
                    }
                }
                if (tempOrders.size != 0){
                    lastItemKey = tempOrders.last().id
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ControllerApplication[this].bookingDatabaseReference
            .orderByKey()
            .startAfter(lastItemKeyLoaded.toString())
            .limitToFirst(Constant.MAX_ITEM_PER_LOAD_LINEAR) // Giới hạn lấy ra MAX_ITEM_PER_LOAD_LINEAR mục
            .addValueEventListener(bookingValueListener)
    }

    private fun handleTrackDriver(order: Order) {
        Toast.makeText(
            this,
            "id: " + order.id + " - Tính năng đang phát triển",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleCancelOrder(order: Order) {
        AlertDialog.Builder(this@OrderHistoryActivity)
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
        val viewDialog = Dialog(this@OrderHistoryActivity)
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
                 Toast.makeText(this@OrderHistoryActivity, getString(R.string.select_cancel_reason), Toast.LENGTH_SHORT).show()
            }
            viewDialog.dismiss()
        }

        // Show dialog + set Customize
        viewDialog.show()
        GlobalFunction.customizeBottomSheetDialog(viewDialog)
    }


    private fun sendDataCancelOrderToRTDB(order: Order, selectedReason: String) {
        val updates = HashMap<String, Any>()
        updates["status"] = CODE_CANCELLED
        updates["cancel_by"] = order.email.toString()
        updates["cancel_reason"] = selectedReason

        ControllerApplication[this].bookingDatabaseReference
            .child(order.id.toString())
            .updateChildren(updates)
    }

    private fun sendNotiCancelOrderToAdmins(order: Order, selectedReason: String) {
        showProgressDialog(true)
        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postCancelOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.body() != null) {
                    showProgressDialog(false)
                    Log.d("Success", "Gửi thông báo thành công!")
                    // Thông báo cho cả hủy đơn thành công + thông báo
                    Toast.makeText(this@OrderHistoryActivity, getString(R.string.msg_cancel_order_successfully), Toast.LENGTH_SHORT).show()
                } else if (response.code() == 500) {
                    showProgressDialog(false)
//                    Toast.makeText(this@OrderHistoryActivity, "Không thể gửi thông báo do người nhận không đăng nhập!", Toast.LENGTH_SHORT).show()
                    Log.d("API", "Đối phương không đăng nhập (No token)")
                } else {
                    showProgressDialog(false)
//                    Toast.makeText(this@OrderHistoryActivity, getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                showProgressDialog(false)
//                Toast.makeText(this@OrderHistoryActivity, "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("onError():  ", t.message.toString())
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postCancelOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postCancelOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).request().body}")
    }

    private fun tabLayoutTabSelectedListener() {
        mActivityOrderHistoryBinding.tabLayoutStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val status = getStatusForTab(it.position)
                    filterOrdersByTab(status)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun getStatusForTab(tabPosition: Int): Int {
        return when (tabPosition) {
            1 -> CODE_NEW_ORDER
            2 -> CODE_PREPARING
            3 -> CODE_SHIPPING
            4 -> CODE_COMPLETED
            5 -> CODE_CANCELLED
            6 -> CODE_FAILED
            else -> -1 // Default case, should not occur
        }
    }

    private fun filterOrdersByTab(status: Int) {
        displayedOrders = if (status == -1) {
            mListOrder // Hiển thị tất cả các đơn hàng nếu không có trạng thái được chỉ định
        } else {

            // Không phải filter mà query lên luôn.
            // vì có phân trang nên nếu filter thì nó chỉ filter trong số item đã load được, nên sẽ không phù hợp bài toán
            mListOrder.filter { it.status == status }.toMutableList()

        }
        mOrderAdapter.updateData(displayedOrders)
    }

    private fun updateDisplayedOrders() {
        val selectedTabPosition = mActivityOrderHistoryBinding.tabLayoutStatus.selectedTabPosition
        val status = getStatusForTab(selectedTabPosition)
        filterOrdersByTab(status)
    }

    private fun goToOrderHistoryDetail(id: Long) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_ORDER_OBJECT, id)
        openActivity(this@OrderHistoryActivity, OrderHistoryDetailActivity::class.java, bundle)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOrderAdapter.release()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}