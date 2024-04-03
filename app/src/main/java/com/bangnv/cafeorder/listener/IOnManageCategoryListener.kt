package com.bangnv.cafeorder.listener

import com.bangnv.cafeorder.model.Category

interface IOnManageCategoryListener {
    fun onClickUpdateCategory(category: Category?)
    fun onClickDeleteCategory(category: Category?)
}