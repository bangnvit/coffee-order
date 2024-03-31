package com.bangnv.cafeorder.model

import java.io.Serializable

class Category : Serializable {
    var id: Long = 0
    var name: String? = null
    var image: String? = null

    constructor() {}
    constructor(id: Long, name: String?, image: String?) {
        this.id = id
        this.name = name
        this.image = image
    }
}