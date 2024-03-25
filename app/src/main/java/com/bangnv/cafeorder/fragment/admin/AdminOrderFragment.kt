package com.bangnv.cafeorder.fragment.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.AdminMainActivity
import com.bangnv.cafeorder.adapter.AdminOrderAdapter
import com.bangnv.cafeorder.adapter.AdminOrderAdapter.IUpdateStatusListener
import com.bangnv.cafeorder.databinding.FragmentAdminOrderBinding
import com.bangnv.cafeorder.fragment.BaseFragment
import com.bangnv.cafeorder.model.Order
import java.util.*

class AdminOrderFragment : BaseFragment() {

    private var mFragmentAdminOrderBinding: FragmentAdminOrderBinding? = null
    private var mListOrder: MutableList<Order>? = null
    private var mAdminOrderAdapter: AdminOrderAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        mFragmentAdminOrderBinding = FragmentAdminOrderBinding.inflate(inflater, container, false)
        initView()
        getListOrders()
        return mFragmentAdminOrderBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as AdminMainActivity?)!!.setToolBar(getString(R.string.order))
        }
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentAdminOrderBinding!!.rcvOrder.layoutManager = linearLayoutManager
        mListOrder = ArrayList()
        mAdminOrderAdapter = AdminOrderAdapter(activity, mListOrder, object : IUpdateStatusListener {
            override fun updateStatus(order: Order) {
                handleUpdateStatusOrder(order)
            }
        })
        mFragmentAdminOrderBinding!!.rcvOrder.adapter = mAdminOrderAdapter
    }

    private fun getListOrders() {
        if (activity == null) {
            return
        }
        ControllerApplication[requireContext()].bookingDatabaseReference
                .addChildEventListener(object : ChildEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val order = dataSnapshot.getValue(Order::class.java)
                        if (order == null || mListOrder == null || mAdminOrderAdapter == null) {
                            return
                        }
                        mListOrder!!.add(0, order)
                        mAdminOrderAdapter!!.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                        val order = dataSnapshot.getValue(Order::class.java)
                        if (order == null || mListOrder == null || mListOrder!!.isEmpty() || mAdminOrderAdapter == null) {
                            return
                        }
                        for (i in mListOrder!!.indices) {
                            if (order.id == mListOrder!![i].id) {
                                mListOrder!![i] = order
                                break
                            }
                        }
                        mAdminOrderAdapter!!.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        val order = dataSnapshot.getValue(Order::class.java)
                        if (order == null || mListOrder == null || mListOrder!!.isEmpty() || mAdminOrderAdapter == null) {
                            return
                        }
                        for (orderObject in mListOrder!!) {
                            if (order.id == orderObject.id) {
                                mListOrder!!.remove(orderObject)
                                break
                            }
                        }
                        mAdminOrderAdapter!!.notifyDataSetChanged()
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }

    private fun handleUpdateStatusOrder(order: Order) {
        if (activity == null) {
            return
        }
        ControllerApplication[requireContext()].bookingDatabaseReference
                .child(order.id.toString()).child("completed").setValue(!order.isCompleted)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mAdminOrderAdapter != null) {
            mAdminOrderAdapter!!.release()
        }
    }
}