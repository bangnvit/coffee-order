package com.bangnv.cafeorder.model

import java.io.Serializable

class Order : Serializable {

    var id: Long = 0
    var name: String? = null
    var email: String? = null
    var phone: String? = null
    var address: String? = null
    var amount = 0
    var foods: String? = null
    var payment = 0
    var isCompleted = false

    constructor() {}

    constructor(id: Long, name: String?, email: String?, phone: String?,
                address: String?, amount: Int, foods: String?, payment: Int, completed: Boolean) {
        this.id = id
        this.name = name
        this.email = email
        this.phone = phone
        this.address = address
        this.amount = amount
        this.foods = foods
        this.payment = payment
        isCompleted = completed
    }
}