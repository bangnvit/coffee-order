package com.bangnv.cafeorder.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.MainActivity
import com.bangnv.cafeorder.adapter.CartAdapter
import com.bangnv.cafeorder.adapter.CartAdapter.IClickListener
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.customizeDialog
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionDoneListener
import com.bangnv.cafeorder.constant.GlobalFunction.showToastMessage
import com.bangnv.cafeorder.database.FoodDatabase.Companion.getInstance
import com.bangnv.cafeorder.databinding.FragmentCartBinding
import com.bangnv.cafeorder.event.ReloadListCartEvent
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class CartFragment : BaseFragment() {

    private var mFragmentCartBinding: FragmentCartBinding? = null
    private var mCartAdapter: CartAdapter? = null
    private var mListFoodCart: MutableList<Food>? = null
    private var mAmount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mFragmentCartBinding = FragmentCartBinding.inflate(inflater, container, false)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        displayListFoodInCart()
        mFragmentCartBinding!!.tvOrderCart.setOnClickListener { onClickOrderCart() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEditTextNoteListener()

        return mFragmentCartBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as MainActivity?)!!.setToolBar(false, getString(R.string.cart))
        }
    }

    private fun displayListFoodInCart() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentCartBinding!!.rcvFoodCart.layoutManager = linearLayoutManager
//        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
//        mFragmentCartBinding!!.rcvFoodCart.addItemDecoration(itemDecoration)
        initDataFoodCart()
    }

    private fun initDataFoodCart() {
        mListFoodCart = ArrayList()
        mListFoodCart = getInstance(requireActivity())!!.foodDAO()!!.listFoodCart
        if (mListFoodCart == null || mListFoodCart!!.isEmpty()) {
            return
        }
        mCartAdapter = CartAdapter(mListFoodCart, object : IClickListener {
            override fun clickDeteteFood(food: Food?, position: Int) {
                deleteFoodFromCart(food, position)
            }

            override fun updateItemFood(food: Food?, position: Int) {
                getInstance(requireActivity())!!.foodDAO()!!.updateFood(food)
                mCartAdapter!!.notifyItemChanged(position)
                calculateTotalPrice()
            }
        })

        mFragmentCartBinding!!.rcvFoodCart.itemAnimator = null
        mFragmentCartBinding!!.rcvFoodCart.adapter = mCartAdapter

        if (mCartAdapter!!.itemCount == 0) {
            mFragmentCartBinding!!.layoutCartWrap.visibility = View.GONE
        } else {
            mFragmentCartBinding!!.layoutCartWrap.visibility = View.VISIBLE
        }
        calculateTotalPrice()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearCart() {
        mListFoodCart?.clear()
        mCartAdapter!!.notifyDataSetChanged()
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        val listFoodCart = getInstance(requireActivity())!!.foodDAO()!!.listFoodCart
        if (listFoodCart == null || listFoodCart.isEmpty()) {
            val strZero: String = formatNumberWithPeriods(0) + Constant.CURRENCY
            mFragmentCartBinding!!.tvTotalPrice.text = strZero
            mAmount = 0
            return
        }
        var totalPrice = 0
        for (food in listFoodCart) {
            totalPrice += food.totalPrice
        }
        val strTotalPrice: String = formatNumberWithPeriods(totalPrice) + Constant.CURRENCY
        mFragmentCartBinding!!.tvTotalPrice.text = strTotalPrice
        mAmount = totalPrice
    }

    private fun deleteFoodFromCart(food: Food?, position: Int) {
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.confirm_delete_food))
            .setMessage(getString(R.string.message_delete_food))
            .setPositiveButton(getString(R.string.delete)) { _: DialogInterface?, _: Int ->
                getInstance(requireActivity())!!.foodDAO()!!.deleteFood(food)
                mListFoodCart?.removeAt(position)
                mCartAdapter!!.notifyItemRemoved(position)
                if (mCartAdapter!!.itemCount == 0) {
                    mFragmentCartBinding!!.layoutCartWrap.visibility = View.GONE
                } else {
                    mFragmentCartBinding!!.layoutCartWrap.visibility = View.VISIBLE
                }
                calculateTotalPrice()
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    @SuppressLint("ResourceType", "InflateParams")
    private fun onClickOrderCart() {
        if (activity == null) {
            return
        }
        if (mListFoodCart == null || mListFoodCart!!.isEmpty()) {
            return
        }

        //Init Custom Dialog
        val viewDialog = Dialog(requireContext())
        viewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        viewDialog.setContentView(R.layout.layout_bottom_sheet_order)

        // init ui
        val tvFoodsOrder = viewDialog.findViewById<TextView>(R.id.tv_foods_order)
        val tvPriceOrder = viewDialog.findViewById<TextView>(R.id.tv_price_order)
        val edtNameOrder = viewDialog.findViewById<TextView>(R.id.edt_name_order)
        val edtPhoneOrder = viewDialog.findViewById<TextView>(R.id.edt_phone_order)
        val edtAddressOrder = viewDialog.findViewById<TextView>(R.id.edt_address_order)
        val tvCancelOrder = viewDialog.findViewById<TextView>(R.id.tv_cancel_order)
        val tvCreateOrder = viewDialog.findViewById<TextView>(R.id.tv_create_order)

        // Set data
        tvFoodsOrder.text = getStringListFoodsOrder()
        tvPriceOrder.text = mFragmentCartBinding!!.tvTotalPrice.text.toString()

        // Set listener
        tvCancelOrder.setOnClickListener { viewDialog.dismiss() }
        tvCreateOrder.setOnClickListener {
            val strName = edtNameOrder.text.toString().trim { it <= ' ' }
            val strPhone = edtPhoneOrder.text.toString().trim { it <= ' ' }
            val strAddress = edtAddressOrder.text.toString().trim { it <= ' ' }
            val strNote = getStringNoteOrder()
            if (isEmpty(strName) || isEmpty(strPhone) || isEmpty(strAddress)) {
                showToastMessage(activity, getString(R.string.message_enter_infor_order))
            } else {
                val id = System.currentTimeMillis()
                val strEmail = user!!.email
                val order = Order(
                    id, strName, strEmail, strPhone, strAddress,
                    mAmount, getStringListFoodsOrder(), Constant.TYPE_PAYMENT_COD,
                    strNote, Constant.CODE_NEW_ORDER
                )
                ControllerApplication[requireActivity()].bookingDatabaseReference
                    .child(id.toString())
                    .setValue(order) { _: DatabaseError?, _: DatabaseReference? ->
                        showToastMessage(activity, getString(R.string.msg_order_success))
                        hideSoftKeyboard(requireActivity())
                        viewDialog.dismiss()

                        mFragmentCartBinding!!.edtNote.setText("")
                        getInstance(requireActivity())!!.foodDAO()!!.deleteAllFood()
                        clearCart()
                    }
            }
        }

        // Show dialog + set Customize
        viewDialog.show()
        customizeDialog(viewDialog)
    }

    private fun getStringListFoodsOrder(): String {
        if (mListFoodCart == null || mListFoodCart!!.isEmpty()) {
            return ""
        }
        var result = ""
        for (food in mListFoodCart!!) {
            result = if (isEmpty(result)) {
                ("- " + food.name + " (" + formatNumberWithPeriods(food.realPrice) + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.count)
            } else {
                (result + "\n" + ("- " + food.name + " (" + formatNumberWithPeriods(food.realPrice) + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.count))
            }
        }
        return result
    }

    private fun getStringNoteOrder(): String {
        val note = mFragmentCartBinding!!.edtNote.text.toString().ifBlank {
            getString(R.string.str_no_note)
        }
        return note
    }

    private fun setupTouchOtherToClearAllFocus() {
        mFragmentCartBinding!!.layoutWrap.setOnClickListener {
            hideSoftKeyboard(requireActivity())
            mFragmentCartBinding!!.edtNote.clearFocus()
        }
    }

    private fun setupLayoutEditTextNoteListener() {
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mFragmentCartBinding!!.layoutNote,
            mFragmentCartBinding!!.edtNote,
            mFragmentCartBinding!!.imgClearNote
        )
        mFragmentCartBinding!!.edtNote.setOnActionDoneListener(
            { hideSoftKeyboard(requireActivity()) },
            { mFragmentCartBinding!!.edtNote.clearFocus() }
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ReloadListCartEvent?) {
        displayListFoodInCart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}