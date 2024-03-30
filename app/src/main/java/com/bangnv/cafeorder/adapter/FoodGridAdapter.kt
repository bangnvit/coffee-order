package com.bangnv.cafeorder.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.adapter.FoodGridAdapter.FoodGridViewHolder
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.databinding.ItemFoodGridBinding
import com.bangnv.cafeorder.listener.IOnClickFoodItemListener
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.utils.GlideUtils.loadUrl

class FoodGridAdapter(private val mContext: Context, private var mListFoods: MutableList<Food>,
                      private val iOnClickFoodItemListener: IOnClickFoodItemListener) : RecyclerView.Adapter<FoodGridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodGridViewHolder {
        val itemFoodGridBinding = ItemFoodGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodGridViewHolder(itemFoodGridBinding)
    }

    override fun onBindViewHolder(holder: FoodGridViewHolder, position: Int) {
        val food = mListFoods.get(position)
        loadUrl(food.image, holder.mItemFoodGridBinding.imgFood)
        if (food.sale <= 0) {
            holder.mItemFoodGridBinding.tvSaleOff.visibility = View.GONE
            holder.mItemFoodGridBinding.tvPrice.visibility = View.GONE
            val strPrice: String = formatNumberWithPeriods(food.price) + Constant.CURRENCY
            holder.mItemFoodGridBinding.tvPriceSale.text = strPrice
        } else {
            holder.mItemFoodGridBinding.tvSaleOff.visibility = View.VISIBLE
            holder.mItemFoodGridBinding.tvPrice.visibility = View.VISIBLE
            val strSale = mContext.getString(R.string.reduce) + " " + food.sale + mContext.getString(R.string.percent)
            holder.mItemFoodGridBinding.tvSaleOff.text = strSale
            val strOldPrice: String = formatNumberWithPeriods(food.price) + Constant.CURRENCY
            holder.mItemFoodGridBinding.tvPrice.text = strOldPrice
            holder.mItemFoodGridBinding.tvPrice.paintFlags = holder.mItemFoodGridBinding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = formatNumberWithPeriods(food.realPrice) + Constant.CURRENCY
            holder.mItemFoodGridBinding.tvPriceSale.text = strRealPrice
        }
        holder.mItemFoodGridBinding.tvFoodName.text = food.name
        holder.mItemFoodGridBinding.layoutItem.setOnClickListener {
            iOnClickFoodItemListener.onClickItemFood(food)
        }
    }

    override fun getItemCount(): Int {
        return mListFoods.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFoods: MutableList<Food>) {
        mListFoods = newFoods
        notifyDataSetChanged()
    }

    class FoodGridViewHolder(val mItemFoodGridBinding: ItemFoodGridBinding) : RecyclerView.ViewHolder(mItemFoodGridBinding.root)
}