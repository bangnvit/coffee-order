package com.bangnv.cafeorder.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationGridScrollListener internal constructor(private val gridLayoutManager: GridLayoutManager) :
    RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = gridLayoutManager.childCount
        val totalItemCount = gridLayoutManager.itemCount
        val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()
        if (isLoading || isLastPage) {
            return
        }
        if (firstVisibleItemPosition >= 0 && visibleItemCount + firstVisibleItemPosition >= totalItemCount) {
            loadMoreItems()
        }
    }

    abstract fun loadMoreItems()
    abstract val isLoading: Boolean
    abstract val isLastPage: Boolean
}