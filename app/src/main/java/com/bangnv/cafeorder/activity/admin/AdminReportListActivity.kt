package com.bangnv.cafeorder.activity.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.adapter.admin.RevenueAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.constant.GlobalFunction.showDatePicker
import com.bangnv.cafeorder.databinding.ActivityAdminReportListBinding
import com.bangnv.cafeorder.listener.IGetDateListener
import com.bangnv.cafeorder.listener.IOnSingleClickListener
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.utils.DateTimeUtils.convertDate2ToTimeStamp
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate_2
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import java.util.*

class AdminReportListActivity : AppCompatActivity() {

    private lateinit var mActivityAdminReportListBinding: ActivityAdminReportListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminReportListBinding = ActivityAdminReportListBinding.inflate(layoutInflater)
        setContentView(mActivityAdminReportListBinding.root)
        initToolbar()
        initListener()
        getListRevenue()
    }

    private fun initToolbar() {
        mActivityAdminReportListBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityAdminReportListBinding.toolbar.imgCart.visibility = View.GONE
        mActivityAdminReportListBinding.toolbar.tvTitle.text = getString(R.string.revenue)
        mActivityAdminReportListBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun initListener() {
        mActivityAdminReportListBinding.tvDateFrom.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                showDatePicker(this@AdminReportListActivity,
                        mActivityAdminReportListBinding.tvDateFrom.text.toString(), object : IGetDateListener {
                    override fun getDate(date: String?) {
                        mActivityAdminReportListBinding.tvDateFrom.text = date
                        getListRevenue()
                        setDateFromNewBackground()
                    }
                })
            }
        })
        mActivityAdminReportListBinding.tvDateTo.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                showDatePicker(this@AdminReportListActivity,
                        mActivityAdminReportListBinding.tvDateTo.text.toString(), object : IGetDateListener {
                    override fun getDate(date: String?) {
                        mActivityAdminReportListBinding.tvDateTo.text = date
                        getListRevenue()
                        setDateToNewBackground()
                    }
                })
            }
        })
    }

    private fun getListRevenue() {
        ControllerApplication[this].bookingDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list: MutableList<Order> = ArrayList()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(Order::class.java)!!
                    if (canAddOrder(order)) {
                        list.add(0, order)
                    }
                }
                handleDataHistories(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun canAddOrder(order: Order?): Boolean {
        if (order == null) {
            return false
        }
        if (order.status != Constant.CODE_COMPLETED) {
            return false
        }
        val strDateFrom = mActivityAdminReportListBinding.tvDateFrom.text.toString()
        val strDateTo = mActivityAdminReportListBinding.tvDateTo.text.toString()
        if (isEmpty(strDateFrom) && isEmpty(strDateTo)) {
            return true
        }
        val strDateOrder = convertTimeStampToDate_2(order.id)
        val longOrder = convertDate2ToTimeStamp(strDateOrder).toLong()
        if (isEmpty(strDateFrom) && !isEmpty(strDateTo)) {
            val longDateTo = convertDate2ToTimeStamp(strDateTo).toLong()
            return longOrder <= longDateTo
        }
        if (!isEmpty(strDateFrom) && isEmpty(strDateTo)) {
            val longDateFrom = convertDate2ToTimeStamp(strDateFrom).toLong()
            return longOrder >= longDateFrom
        }
        val longDateTo = convertDate2ToTimeStamp(strDateTo).toLong()
        val longDateFrom = convertDate2ToTimeStamp(strDateFrom).toLong()
        return longOrder in longDateFrom..longDateTo
    }

    private fun handleDataHistories(list: List<Order>?) {
        if (list == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(this)
        mActivityAdminReportListBinding.rcvOrderHistory.layoutManager = linearLayoutManager
        val revenueAdapter = RevenueAdapter(list)
        mActivityAdminReportListBinding.rcvOrderHistory.adapter = revenueAdapter

        // Calculate total
        val strTotalValue: String = formatNumberWithPeriods(getTotalValues(list)) + Constant.CURRENCY
        mActivityAdminReportListBinding.tvTotalValue.text = strTotalValue
    }

    private fun getTotalValues(list: List<Order>?): Int {
        if (list.isNullOrEmpty()) {
            return 0
        }
        var total = 0
        for (order in list) {
            total += order.amount
        }
        return total
    }

    private fun setDateFromNewBackground() {
        if(mActivityAdminReportListBinding.tvDateFrom.text.isNullOrEmpty()) {
            mActivityAdminReportListBinding.layoutDateFrom.setBackgroundResource(R.drawable.bg_white_corner_12_border_gray)
        }  else {
            mActivityAdminReportListBinding.layoutDateFrom.setBackgroundResource(R.drawable.bg_white_corner_12_border_primary)
        }
    }

    private fun setDateToNewBackground() {
        if(mActivityAdminReportListBinding.tvDateTo.text.isNullOrEmpty()) {
            mActivityAdminReportListBinding.layoutDateTo.setBackgroundResource(R.drawable.bg_white_corner_12_border_gray)
        }  else {
            mActivityAdminReportListBinding.layoutDateTo.setBackgroundResource(R.drawable.bg_white_corner_12_border_primary)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}