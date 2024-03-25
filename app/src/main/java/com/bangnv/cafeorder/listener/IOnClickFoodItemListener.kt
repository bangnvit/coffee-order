package com.bangnv.cafeorder.listener

import com.bangnv.cafeorder.model.Food

interface IOnClickFoodItemListener {
    fun onClickItemFood(food: Food)
}