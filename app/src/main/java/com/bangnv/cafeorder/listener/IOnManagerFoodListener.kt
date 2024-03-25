package com.bangnv.cafeorder.listener

import com.bangnv.cafeorder.model.Food

interface IOnManagerFoodListener {
    fun onClickUpdateFood(food: Food?)
    fun onClickDeleteFood(food: Food?)
}