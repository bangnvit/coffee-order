package com.bangnv.cafeorder.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangnv.cafeorder.adapter.AdminFoodAdapter.AdminFoodViewHolder
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.databinding.ItemAdminFoodBinding
import com.bangnv.cafeorder.listener.IOnManagerFoodListener
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.utils.GlideUtils.loadUrl

class AdminFoodAdapter(private val mListFoods: List<Food>?,
                       val iOnManagerFoodListener: IOnManagerFoodListener) : RecyclerView.Adapter<AdminFoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminFoodViewHolder {
        val itemAdminFoodBinding = ItemAdminFoodBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminFoodViewHolder(itemAdminFoodBinding)
    }

    override fun onBindViewHolder(holder: AdminFoodViewHolder, position: Int) {
        val food = mListFoods!![position]
        loadUrl(food.image, holder.mItemAdminFoodBinding.imgFood)
        if (food.sale <= 0) {
            holder.mItemAdminFoodBinding.tvSaleOff.visibility = View.GONE
            holder.mItemAdminFoodBinding.tvPrice.visibility = View.GONE
            val strPrice: String = "" + food.price + Constant.CURRENCY
            holder.mItemAdminFoodBinding.tvPriceSale.text = strPrice
        } else {
            holder.mItemAdminFoodBinding.tvSaleOff.visibility = View.VISIBLE
            holder.mItemAdminFoodBinding.tvPrice.visibility = View.VISIBLE
            val strSale = "Giảm " + food.sale + "%"
            holder.mItemAdminFoodBinding.tvSaleOff.text = strSale
            val strOldPrice: String = "" + food.price + Constant.CURRENCY
            holder.mItemAdminFoodBinding.tvPrice.text = strOldPrice
            holder.mItemAdminFoodBinding.tvPrice.paintFlags = holder.mItemAdminFoodBinding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = "" + food.realPrice + Constant.CURRENCY
            holder.mItemAdminFoodBinding.tvPriceSale.text = strRealPrice
        }
        holder.mItemAdminFoodBinding.tvFoodName.text = food.name
        holder.mItemAdminFoodBinding.tvFoodDescription.text = food.description
        if (food.isPopular) {
            holder.mItemAdminFoodBinding.tvPopular.text = "Có"
        } else {
            holder.mItemAdminFoodBinding.tvPopular.text = "Không"
        }
        holder.mItemAdminFoodBinding.imgEdit.setOnClickListener { iOnManagerFoodListener.onClickUpdateFood(food) }
        holder.mItemAdminFoodBinding.imgDelete.setOnClickListener { iOnManagerFoodListener.onClickDeleteFood(food) }
    }

    override fun getItemCount(): Int {
        return mListFoods?.size ?: 0
    }

    class AdminFoodViewHolder(val mItemAdminFoodBinding: ItemAdminFoodBinding) : RecyclerView.ViewHolder(mItemAdminFoodBinding.root)
}