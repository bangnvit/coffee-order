package com.bangnv.cafeorder.fragment.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.AdminMainActivity
import com.bangnv.cafeorder.activity.AdminOrderDetailActivity
import com.bangnv.cafeorder.adapter.AdminOrderAdapter
import com.bangnv.cafeorder.adapter.AdminOrderAdapter.IClickAdminOrderListener
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.bangnv.cafeorder.constant.GlobalFunction.startActivity
import com.bangnv.cafeorder.databinding.FragmentAdminOrderBinding
import com.bangnv.cafeorder.fragment.BaseFragment
import com.bangnv.cafeorder.model.Order
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.util.*

class AdminOrderFragment : BaseFragment() {

    private var mFragmentAdminOrderBinding: FragmentAdminOrderBinding? = null
    private var mAdminOrderAdapter: AdminOrderAdapter? = null
    private var mListOrder: MutableList<Order>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        mAdminOrderAdapter =
            AdminOrderAdapter(activity, mListOrder, object : IClickAdminOrderListener {
                override fun acceptOrder(order: Order) {
                    handleAcceptOrder(order)
                }

                override fun refuseOrder(order: Order) {
                    handleRefuseOrder(order)
                }

                override fun sendOrder(order: Order) {
                    handleSendOrder(order)
                }

                override fun onClickItemAdminOrder(order: Order) {
                    goToAdminOrderDetail(order.id)
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

    private fun handleAcceptOrder(order: Order) {
        if (activity == null) {
            return
        }
        // user click accept => status : CODE_PREPARING  (prepare for this order)
        ControllerApplication[requireContext()].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_PREPARING)
    }

    private fun handleRefuseOrder(order: Order) {
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.msg_refuse_title))
            .setMessage(getString(R.string.msg_confirm_refuse))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                if (activity == null) {
                    return@setPositiveButton
                }
                // user click cancel => status : CODE_CANCELLED, create and set value for "cancel_by" of this order on Firebase
                ControllerApplication[requireContext()].bookingDatabaseReference
                    .child(order.id.toString()).child("status").setValue(Constant.CODE_CANCELLED)
                // Get Email of admin refused this order. user from DataStoreManager/Companion
                ControllerApplication[requireContext()].bookingDatabaseReference
                    .child(order.id.toString()).child("cancel_by").setValue(user!!.email)
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun handleSendOrder(order: Order) {
        if (activity == null) {
            return
        }
        // user click send => status : CODE_SHIPPING
        ControllerApplication[requireContext()].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_SHIPPING)

        // Cần làm thêm: yêu cầu bên vận chuyển
        Log.d("AdminOrderFragment: ", "Viết hàm gửi lên cho bên vận chuyển. Đã set trạng thái rồi")
    }

    private fun goToAdminOrderDetail(id: Long) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_ADMIN_ORDER_OBJECT, id)
        startActivity(requireContext(), AdminOrderDetailActivity::class.java, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mAdminOrderAdapter != null) {
            mAdminOrderAdapter!!.release()
        }
    }
}