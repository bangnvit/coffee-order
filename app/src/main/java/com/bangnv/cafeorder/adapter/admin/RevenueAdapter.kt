package com.bangnv.cafeorder.adapter.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangnv.cafeorder.adapter.admin.RevenueAdapter.RevenueViewHolder
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.databinding.ItemRevenueBinding
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.utils.DateTimeUtils.convertTimeStampToDate_2

class RevenueAdapter(private val mListOrder: List<Order>?) : RecyclerView.Adapter<RevenueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevenueViewHolder {
        val itemRevenueBinding = ItemRevenueBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return RevenueViewHolder(itemRevenueBinding)
    }

    override fun onBindViewHolder(holder: RevenueViewHolder, position: Int) {
        val order = mListOrder!![position]
        holder.mItemRevenueBinding.tvId.text = order.id.toString()
        holder.mItemRevenueBinding.tvDate.text = convertTimeStampToDate_2(order.id)
        val strAmount: String = formatNumberWithPeriods(order.amount) + Constant.CURRENCY
        holder.mItemRevenueBinding.tvTotalAmount.text = strAmount
    }

    override fun getItemCount(): Int {
        return mListOrder?.size ?: 0
    }

    class RevenueViewHolder(val mItemRevenueBinding: ItemRevenueBinding) : RecyclerView.ViewHolder(mItemRevenueBinding.root)
}