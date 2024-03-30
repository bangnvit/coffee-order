package com.bangnv.cafeorder.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.adapter.OrderAdapter.OrderViewHolder
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.databinding.ItemOrderBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate

class OrderAdapter(private var mContext: Context?, private var mListOrder: MutableList<Order>,
                   private val mIClickOrderHistoryListener: IClickOrderHistoryListener) : RecyclerView.Adapter<OrderViewHolder>() {

    interface IClickOrderHistoryListener {
        fun trackDriver(order: Order)

        fun cancelOrder(order: Order)

        fun onClickItemOrder(order: Order)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemOrderBinding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return OrderViewHolder(itemOrderBinding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = mListOrder[position]
        when (order.status) {
            Constant.CODE_NEW_ORDER -> { //30
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.VISIBLE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_NEW_ORDER
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_PREPARING -> { //31
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_PREPARING
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_SHIPPING -> { //32
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.VISIBLE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_SHIPPING
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_COMPLETED -> { //33
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_COMPLETED
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_CANCELLED -> { //34
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_CANCELLED
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_red_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_red))
            }
            else -> { // 35: CODE_FAILED | or any other unexpected status
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_FAILED
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_red_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_red))
            }
        }
        holder.mItemOrderBinding.tvId.text = order.id.toString()
        holder.mItemOrderBinding.tvDate.text = convertTimeStampToDate(order.id)
        val strAmount: String = formatNumberWithPeriods(order.amount) + Constant.CURRENCY
        holder.mItemOrderBinding.tvTotalAmount.text = strAmount

        holder.mItemOrderBinding.tvTrackDriver.setOnClickListener { mIClickOrderHistoryListener.trackDriver(order) }
        holder.mItemOrderBinding.tvCancelOrder.setOnClickListener { mIClickOrderHistoryListener.cancelOrder(order) }
        holder.mItemOrderBinding.layoutItem.setOnClickListener{ mIClickOrderHistoryListener.onClickItemOrder(order) }
    }

    override fun getItemCount(): Int {
        return mListOrder.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newOrders: MutableList<Order>) {
        mListOrder = newOrders
        notifyDataSetChanged()
    }


    fun release() {
        mContext = null
    }

    class OrderViewHolder(val mItemOrderBinding: ItemOrderBinding) : RecyclerView.ViewHolder(mItemOrderBinding.root)
}