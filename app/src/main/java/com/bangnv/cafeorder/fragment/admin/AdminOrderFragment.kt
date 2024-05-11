package com.bangnv.cafeorder.fragment.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.admin.AdminMainActivity
import com.bangnv.cafeorder.activity.admin.AdminOrderDetailActivity
import com.bangnv.cafeorder.adapter.admin.AdminOrderAdapter
import com.bangnv.cafeorder.adapter.admin.AdminOrderAdapter.IClickAdminOrderListener
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.addMyTabs
import com.bangnv.cafeorder.constant.GlobalFunction.openActivity
import com.bangnv.cafeorder.database.AppApi
import com.bangnv.cafeorder.databinding.FragmentAdminOrderBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.model.baseresponse.RetrofitClients
import com.bangnv.cafeorder.model.request.OrderRequest
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AdminOrderFragment : Fragment() {

    private lateinit var mFragmentAdminOrderBinding: FragmentAdminOrderBinding
    private lateinit var mAdminOrderAdapter: AdminOrderAdapter
    private var mListOrder: MutableList<Order> = mutableListOf()        // Full Data
    private var displayedOrders: MutableList<Order> = mutableListOf()   // display Data filtered

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mFragmentAdminOrderBinding = FragmentAdminOrderBinding.inflate(inflater, container, false)

        initView()
        initTabLayout()
        getListOrders()
        tabLayoutTabSelectedListener()
        return mFragmentAdminOrderBinding.root
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentAdminOrderBinding.rcvOrder.layoutManager = linearLayoutManager
        mListOrder = ArrayList()
        mAdminOrderAdapter =
            AdminOrderAdapter(activity, mListOrder, object : IClickAdminOrderListener {
                override fun acceptOrder(order: Order) {
                    handleAcceptOrder(order)
                }

                override fun refuseOrder(order: Order) {
                    // Tạm thời không nữa dùng vì thầy yêu cầu thế - đã ẩn nút từ chối (nhưng vẫn viết chức năng ok rồi)
                    handleRefuseOrder(order)
                }

                override fun sendOrder(order: Order) {
                    handleSendDeliveryOrder(order)
                }

                override fun completeOrder(order: Order) {
                    handleCompleteOrder(order)
                }

                override fun onClickItemAdminOrder(order: Order) {
                    goToAdminOrderDetail(order.id)
                }
            })
        mFragmentAdminOrderBinding.rcvOrder.adapter = mAdminOrderAdapter
    }

    private fun initTabLayout() {
        mFragmentAdminOrderBinding.tabLayoutStatus.addMyTabs(
            Constant.TEXT_ALL_ORDER,
            Constant.TEXT_NEW_ORDER,
            Constant.TEXT_PREPARING,
            Constant.TEXT_SHIPPING,
            Constant.TEXT_COMPLETED,
            Constant.TEXT_CANCELLED,
            Constant.TEXT_FAILED,
            selectedTab = Constant.TEXT_NEW_ORDER
        )
    }

    private fun getListOrders() {
        if (activity == null) {
            return
        }
        ControllerApplication[requireContext()].bookingDatabaseReference
            .addChildEventListener(object : ChildEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val order = dataSnapshot.getValue(Order::class.java) ?: return
                    if(order.status != 1){
                        mListOrder.add(0, order)
                    }
                    mAdminOrderAdapter.notifyDataSetChanged()
                    updateDisplayedOrders()
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                    val order = dataSnapshot.getValue(Order::class.java)
                    if (order == null || mListOrder.isEmpty()) {
                        return
                    }
                    for (i in mListOrder.indices) {
                        if (order.id == mListOrder[i].id) {
                            mListOrder[i] = order
                            break
                        }
                    }
                    mAdminOrderAdapter.notifyDataSetChanged()
                    updateDisplayedOrders()
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    val order = dataSnapshot.getValue(Order::class.java)
                    if (order == null || mListOrder.isEmpty()) {
                        return
                    }
                    for (orderObject in mListOrder) {
                        if (order.id == orderObject.id) {
                            mListOrder.remove(orderObject)
                            break
                        }
                    }
                    mAdminOrderAdapter.notifyDataSetChanged()
                    updateDisplayedOrders()
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun handleAcceptOrder(order: Order) {
        if (activity == null) {
            return
        }
        sendDataAcceptOrderToRTDB(order)
        sendNotiAcceptOrderToUser(order)
    }

    private fun sendDataAcceptOrderToRTDB(order: Order) {
        // user click accept => status : CODE_PREPARING  (prepare for this order)
        ControllerApplication[requireContext()].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_PREPARING)
    }

    private fun sendNotiAcceptOrderToUser(order: Order) {
        // Send notification to user
        (activity as? AdminMainActivity)?.showProgressDialog(true)
        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postAcceptOrder(OrderRequest(order.email, order.id.toString())).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.body() != null) {
                    (activity as? AdminMainActivity)?.showProgressDialog(false)
                    Log.d("Success", "Gửi thông báo thành công!")
                    // Thông báo cho cả chấp nhận đơn thành công + thông báo
                    Toast.makeText(requireContext(), getString(R.string.msg_accept_order_successfully), Toast.LENGTH_SHORT).show()
                } else {
                    (activity as? AdminMainActivity)?.showProgressDialog(false)
                    Toast.makeText(requireContext(), getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                (activity as? AdminMainActivity)?.showProgressDialog(false)
                Toast.makeText(requireContext(), "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("onError():  ", t.message.toString())
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postAcceptOrder(OrderRequest(order.email, order.id.toString())).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postAcceptOrder(OrderRequest(order.email, order.id.toString())).request().body}")

    }

    private fun handleRefuseOrder(order: Order) {
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.msg_refuse_title))
            .setMessage(getString(R.string.msg_confirm_refuse))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                if (activity == null) {
                    return@setPositiveButton
                }

                // show dialog to choose reason refuse order
                showDialogRefuseReason(order)
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun showDialogRefuseReason(order: Order) {
        //Init Custom Dialog
        val viewDialog = Dialog(requireContext())
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
                Toast.makeText(requireContext(), getString(R.string.select_refuse_reason), Toast.LENGTH_SHORT).show()
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

        ControllerApplication[requireContext()].bookingDatabaseReference
            .child(order.id.toString())
            .updateChildren(updates)
    }

    //Multi devices is available
    private fun sendNotiRefuseOrderToUser(order: Order, selectedReason: String) {
        (activity as? AdminMainActivity)?.showProgressDialog(true)
        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postRefuseOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.body() != null) {
                    (activity as? AdminMainActivity)?.showProgressDialog(false)
                    Log.d("Success", "Gửi thông báo thành công!")
                    // Thông báo cho cả từ chối đơn thành công + thông báo
                    Toast.makeText(requireContext(), getString(R.string.msg_refuse_order_successfully), Toast.LENGTH_SHORT).show()
                } else {
                    (activity as? AdminMainActivity)?.showProgressDialog(false)
                    Toast.makeText(requireContext(), getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                (activity as? AdminMainActivity)?.showProgressDialog(false)
                Toast.makeText(requireContext(), "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("onError():  ", t.message.toString())
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postRefuseOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postRefuseOrder(OrderRequest(order.email, order.id.toString(), selectedReason)).request().body}")

    }

    private fun handleSendDeliveryOrder(order: Order) {
        if (activity == null) {
            return
        }
        sendDataDeliveryOrderToRTDB(order)
        sendNotiDeliveryOrderToUser(order) // multi devices is available
    }

    private fun sendDataDeliveryOrderToRTDB(order: Order) {
        // user click send => status : CODE_SHIPPING
        ControllerApplication[requireContext()].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_SHIPPING)
    }

    private fun sendNotiDeliveryOrderToUser(order: Order) {
        // TO DO Cần làm thêm: yêu cầu bên vận chuyển
        // Cần làm thêm: yêu cầu bên vận chuyển
        Log.d("AdminOrderFragment: ", "Viết hàm gửi lên cho bên vận chuyển. Đã set trạng thái rồi")

        // Send notification to user
        (activity as? AdminMainActivity)?.showProgressDialog(true)
        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postSendDeliveryOrder(OrderRequest(order.email, order.id.toString())).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.body() != null) {
                    (activity as? AdminMainActivity)?.showProgressDialog(false)
                    Log.d("Success", "Gửi thông báo thành công!")
                    // Thông báo cho cả gửi đơn cho vận chuyển thành công + thông báo
                    Toast.makeText(requireContext(), getString(R.string.msg_send_delivery_order_successfully), Toast.LENGTH_SHORT).show()
                } else {
                    (activity as? AdminMainActivity)?.showProgressDialog(false)
                    Toast.makeText(requireContext(), getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                (activity as? AdminMainActivity)?.showProgressDialog(false)
                Toast.makeText(requireContext(), "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("onError():  ", t.message.toString())
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postSendDeliveryOrder(OrderRequest(order.email, order.id.toString())).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postSendDeliveryOrder(OrderRequest(order.email, order.id.toString())).request().body}")
    }

    private fun handleCompleteOrder(order: Order) {
        if (activity == null) {
            return
        }
        sendDataCompleteOrderToRTDB(order)
        sendNotiCompleteOrderToUser(order) // multi devices is available
    }

    private fun sendDataCompleteOrderToRTDB(order: Order) {
        // user click send => status : CODE_COMPLETED
        ControllerApplication[requireContext()].bookingDatabaseReference
            .child(order.id.toString()).child("status").setValue(Constant.CODE_COMPLETED)
    }

    private fun sendNotiCompleteOrderToUser(order: Order) {

        // Send notification to user
        (activity as? AdminMainActivity)?.showProgressDialog(true)
        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postCompleteOrder(OrderRequest(order.email, order.id.toString())).enqueue(object :
            Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.body() != null) {
                    (activity as? AdminMainActivity)?.showProgressDialog(false)
                    Log.d("Success", "Gửi thông báo thành công!")
                    Toast.makeText(requireContext(), getString(R.string.msg_send_complete_order_successfully), Toast.LENGTH_SHORT).show()
                } else {
                    (activity as? AdminMainActivity)?.showProgressDialog(false)
                    Toast.makeText(requireContext(), getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                (activity as? AdminMainActivity)?.showProgressDialog(false)
                Toast.makeText(requireContext(), "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("onError():  ", t.message.toString())
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postCompleteOrder(OrderRequest(order.email, order.id.toString())).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postCompleteOrder(OrderRequest(order.email, order.id.toString())).request().body}")

    }

    private fun tabLayoutTabSelectedListener() {
        mFragmentAdminOrderBinding.tabLayoutStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
            1 -> Constant.CODE_NEW_ORDER
            2 -> Constant.CODE_PREPARING
            3 -> Constant.CODE_SHIPPING
            4 -> Constant.CODE_COMPLETED
            5 -> Constant.CODE_CANCELLED
            6 -> Constant.CODE_FAILED
            else -> -1 // Default case, should not occur
        }
    }

    private fun filterOrdersByTab(status: Int) {
        displayedOrders = if (status == -1) {
            mListOrder // Hiển thị tất cả các đơn hàng nếu không có trạng thái được chỉ định
        } else {
            mListOrder.filter { it.status == status }.toMutableList()
        }
        mAdminOrderAdapter.updateData(displayedOrders)
    }

    private fun updateDisplayedOrders() {
        val selectedTabPosition = mFragmentAdminOrderBinding.tabLayoutStatus.selectedTabPosition
        val status = getStatusForTab(selectedTabPosition)
        filterOrdersByTab(status)
    }

    private fun goToAdminOrderDetail(id: Long) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_ADMIN_ORDER_OBJECT, id)
        openActivity(requireContext(), AdminOrderDetailActivity::class.java, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdminOrderAdapter.release()
    }
}