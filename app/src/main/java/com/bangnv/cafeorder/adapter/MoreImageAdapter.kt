package com.bangnv.cafeorder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangnv.cafeorder.adapter.MoreImageAdapter.MoreImageViewHolder
import com.bangnv.cafeorder.databinding.ItemMoreImageBinding
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.model.Image
import com.bangnv.cafeorder.utils.GlideUtils.loadUrl

class MoreImageAdapter(private val mListImages: List<Image>?) : RecyclerView.Adapter<MoreImageViewHolder>() {

    interface IOnClickOtherImagesListener {
        fun onClickOtherImages(urlImage: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreImageViewHolder {
        val itemMoreImageBinding = ItemMoreImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoreImageViewHolder(itemMoreImageBinding)
    }

    override fun onBindViewHolder(holder: MoreImageViewHolder, position: Int) {
        val image = mListImages!![position]
        loadUrl(image.url, holder.mItemMoreImageBinding.imageFood)
    }

    override fun getItemCount(): Int {
        return mListImages?.size ?: 0
    }

    class MoreImageViewHolder(val mItemMoreImageBinding: ItemMoreImageBinding) : RecyclerView.ViewHolder(mItemMoreImageBinding.root)
}