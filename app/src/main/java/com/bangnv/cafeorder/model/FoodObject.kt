package com.bangnv.cafeorder.model

import java.io.Serializable

class FoodObject : Serializable {

    var id: Long = 0
    var name: String? = null
    var description: String? = null
    var price = 0
    var sale = 0
    var image: String? = null
    var banner: String? = null
    var isPopular = false
    var images: List<Image>? = null
    var categoryId: Long = 0
    var categoryName: String? = null

    constructor() {}

    constructor(id: Long, name: String?, description: String?, price: Int, sale: Int,
                image: String?, banner: String?, popular: Boolean, categoryId: Long, categoryName: String?) {
        this.id = id
        this.name = name
        this.description = description
        this.price = price
        this.sale = sale
        this.image = image
        this.banner = banner
        this.isPopular = popular
        this.categoryId = categoryId
        this.categoryName = categoryName
    }
}