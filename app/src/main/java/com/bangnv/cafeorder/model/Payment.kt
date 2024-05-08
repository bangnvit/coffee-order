package com.bangnv.cafeorder.model

import java.io.Serializable

class Payment : Serializable {
    var name: String? = null
    var code: Int = 0

    constructor() {}

    constructor(name: String?, code: Int) {
        this.name = name
        this.code = code
    }
}