package com.bangnv.cafeorder.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
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
import com.bangnv.cafeorder.constant.GlobalFunction.addMyTabs
import com.bangnv.cafeorder.constant.GlobalFunction.startActivity
import com.bangnv.cafeorder.databinding.ActivityOrderHistoryBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class OrderHistoryActivity : BaseActivity() {

    private lateinit var mActivityOrderHistoryBinding: ActivityOrderHistoryBinding
    private lateinit var mOrderAdapter: OrderAdapter
    private var mListOrder: MutableList<Order> = mutableListOf()        // Full Data
    private var displayedOrders: MutableList<Order> = mutableListOf()   // display Data filtered

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityOrderHistoryBinding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(mActivityOrderHistoryBinding.root)
        initToolbar()
        initView()
        initTabLayout()
        getListOrders()
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

    private fun getListOrders() {
        ControllerApplication[this].bookingDatabaseReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mListOrder.clear()
                    for (dataSnapshot in snapshot.children) {
                        val order = dataSnapshot.getValue(Order::class.java)
                        if (order != null) {
                            val strEmail = user!!.email
                            if (strEmail.equals(order.email, ignoreCase = true)) {
                                mListOrder.add(0, order)
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
            })
    }

    private fun handleTrackDriver(order: Order) {
        Toast.makeText(
            this,
            "id: " + order.id + " - Hãy làm cái function Track Driver sau khi thêm module ship :v",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleCancelOrder(order: Order) {
        AlertDialog.Builder(this@OrderHistoryActivity)
            .setTitle(getString(R.string.msg_cancel_title))
            .setMessage(getString(R.string.msg_confirm_cancel))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                // user click cancel => status : CODE_CANCELLED , create "cancel_by" of this order on Firebase
                ControllerApplication[this].bookingDatabaseReference
                    .child(order.id.toString()).child("status").setValue(CODE_CANCELLED)
                ControllerApplication[this].bookingDatabaseReference
                    .child(order.id.toString()).child("cancel_by").setValue(order.email)
                Toast.makeText(this, "order.email: " + order.email, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.action_back), null)
            .show()
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
        startActivity(this@OrderHistoryActivity, OrderHistoryDetailActivity::class.java, bundle)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOrderAdapter.release()
    }
}