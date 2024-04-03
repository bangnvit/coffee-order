package com.bangnv.cafeorder.adapter.admin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangnv.cafeorder.adapter.admin.AdminCategoryAdapter.*
import com.bangnv.cafeorder.databinding.ItemAdminCategoryBinding
import com.bangnv.cafeorder.listener.IOnManageCategoryListener
import com.bangnv.cafeorder.model.Category
import com.bangnv.cafeorder.utils.GlideUtils.loadUrl

class AdminCategoryAdapter(private var mListCategory: MutableList<Category>,
            private val mIOnManageCategoryListener: IOnManageCategoryListener) : RecyclerView.Adapter<AdminCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminCategoryViewHolder {
        val mItemAdminCategoryBinding = ItemAdminCategoryBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminCategoryViewHolder(mItemAdminCategoryBinding)
    }

    override fun onBindViewHolder(holder: AdminCategoryViewHolder, position: Int) {
        val category = mListCategory[position]
        loadUrl(category.image, holder.mItemAdminCategoryBinding.imgCategory)
        holder.mItemAdminCategoryBinding.tvCategoryName.text = category.name
        holder.mItemAdminCategoryBinding.imgEdit.setOnClickListener { mIOnManageCategoryListener.onClickUpdateCategory(category) }
        holder.mItemAdminCategoryBinding.imgDelete.setOnClickListener { mIOnManageCategoryListener.onClickDeleteCategory(category) }
    }

    override fun getItemCount(): Int {
        return mListCategory.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newListCategory: MutableList<Category>) {
        mListCategory = newListCategory
        notifyDataSetChanged()
    }

    class AdminCategoryViewHolder(val mItemAdminCategoryBinding: ItemAdminCategoryBinding) : RecyclerView.ViewHolder(mItemAdminCategoryBinding.root)
}