package com.bangnv.cafeorder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangnv.cafeorder.adapter.CategoryAdapter.CategoryViewHolder
import com.bangnv.cafeorder.databinding.ItemCategoryBinding
import com.bangnv.cafeorder.model.Category
import com.bangnv.cafeorder.utils.GlideUtils.loadUrl

class CategoryAdapter(private val mListCategory: MutableList<Category>,
                      private val iManagerCategoryListener: IManagerCategoryListener) : RecyclerView.Adapter<CategoryViewHolder>() {
    interface IManagerCategoryListener {
        fun clickItemCategory(category: Category?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val mItemCategoryBinding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(mItemCategoryBinding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = mListCategory[position]
        loadUrl(category.image, holder.mItemCategoryBinding.imgCategory)
        holder.mItemCategoryBinding.tvCategoryName.text = category.name
        holder.mItemCategoryBinding.layoutItem.setOnClickListener { iManagerCategoryListener.clickItemCategory(category) }
    }

    override fun getItemCount(): Int {
        return mListCategory.size
    }

    class CategoryViewHolder(val mItemCategoryBinding: ItemCategoryBinding) : RecyclerView.ViewHolder(mItemCategoryBinding.root)
}