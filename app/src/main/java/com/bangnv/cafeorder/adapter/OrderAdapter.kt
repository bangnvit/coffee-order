package com.bangnv.cafeorder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.adapter.OrderAdapter.OrderViewHolder
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.databinding.ItemOrderBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate

class OrderAdapter(private var mContext: Context?,
                   private val mListOrder: List<Order>?) : RecyclerView.Adapter<OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemOrderBinding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return OrderViewHolder(itemOrderBinding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = mListOrder!![position]
        if (order.status == 35) { //status: 35: Completed
            holder.mItemOrderBinding.layoutItem.setBackgroundResource(R.drawable.bg_color_overlay_border_radius_12)
        } else {
            holder.mItemOrderBinding.layoutItem.setBackgroundResource(R.drawable.bg_color_white_border_divider_radius_12)
        }
        holder.mItemOrderBinding.tvId.text = order.id.toString()
        holder.mItemOrderBinding.tvName.text = order.name
        holder.mItemOrderBinding.tvPhone.text = order.phone
        holder.mItemOrderBinding.tvAddress.text = order.address
        holder.mItemOrderBinding.tvMenu.text = order.foods
        holder.mItemOrderBinding.tvDate.text = convertTimeStampToDate(order.id)
        val strAmount: String = "" + order.amount + Constant.CURRENCY
        holder.mItemOrderBinding.tvTotalAmount.text = strAmount
        var paymentMethod = ""
        if (Constant.TYPE_PAYMENT_CASH == order.payment) {
            paymentMethod = Constant.PAYMENT_METHOD_CASH
        }
        holder.mItemOrderBinding.tvPayment.text = paymentMethod
    }

    override fun getItemCount(): Int {
        return mListOrder?.size ?: 0
    }

    fun release() {
        mContext = null
    }

    class OrderViewHolder(val mItemOrderBinding: ItemOrderBinding) : RecyclerView.ViewHolder(mItemOrderBinding.root)
}