package com.bangnv.cafeorder.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "food")
class Food : Serializable {

    @PrimaryKey
    var id: Long = 0
    var name: String? = null
    var image: String? = null
    var banner: String? = null
    var description: String? = null
    var price = 0
    var sale = 0
    var count = 0
    var totalPrice = 0
    var isPopular = false
    var categoryId: Long = 0
    var categoryName: String? = null

    @Ignore
    var images: List<Image>? = null

    val realPrice: Int
        get() = if (sale <= 0) {
            price
        } else price - price * sale / 100
}