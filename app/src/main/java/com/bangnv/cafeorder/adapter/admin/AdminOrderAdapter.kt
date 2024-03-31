package com.bangnv.cafeorder.adapter.admin

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.adapter.admin.AdminOrderAdapter.AdminOrderViewHolder
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.databinding.ItemAdminOrderBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate

class AdminOrderAdapter(private var mContext: Context?, private var mListOrder: List<Order>,
                        private val mIClickAdminOrderListener: IClickAdminOrderListener
) : RecyclerView.Adapter<AdminOrderViewHolder>() {

    interface IClickAdminOrderListener {
        fun acceptOrder(order: Order)

        fun refuseOrder(order: Order)

        fun sendOrder(order: Order)

        fun onClickItemAdminOrder(order: Order)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val itemAdminOrderBinding = ItemAdminOrderBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return AdminOrderViewHolder(itemAdminOrderBinding)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        val order = mListOrder[position]
        when (order.status) {
            Constant.CODE_NEW_ORDER -> { //30
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.VISIBLE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_NEW_ORDER
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_PREPARING -> { //31
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.VISIBLE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_PREPARING
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_SHIPPING -> { //32
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_SHIPPING
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_COMPLETED -> { //33
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_COMPLETED
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_CANCELLED -> { //34
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_CANCELLED
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_red_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_red))
            }
            else -> { // 35: CODE_FAILED | or any other unexpected status
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_FAILED
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_red_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_red))
            }
        }
        holder.mItemAdminOrderBinding.tvAcceptOrder.setOnClickListener { mIClickAdminOrderListener.acceptOrder(order) }
        holder.mItemAdminOrderBinding.tvRefuseOrder.setOnClickListener { mIClickAdminOrderListener.refuseOrder(order) }
        holder.mItemAdminOrderBinding.tvSendOrder.setOnClickListener { mIClickAdminOrderListener.sendOrder(order) }
        holder.mItemAdminOrderBinding.layoutItem.setOnClickListener { mIClickAdminOrderListener.onClickItemAdminOrder(order) }

        holder.mItemAdminOrderBinding.tvId.text = order.id.toString()
        holder.mItemAdminOrderBinding.tvDate.text = convertTimeStampToDate(order.id)
        val strAmount: String = formatNumberWithPeriods(order.amount) + Constant.CURRENCY
                    holder . mItemAdminOrderBinding . tvTotalAmount . text = strAmount
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

    class AdminOrderViewHolder(val mItemAdminOrderBinding: ItemAdminOrderBinding) : RecyclerView.ViewHolder(mItemAdminOrderBinding.root)
}