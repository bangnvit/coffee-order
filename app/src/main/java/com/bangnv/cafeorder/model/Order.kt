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
    //    var isComplete  = false // đã sửa thành status
    var status : Int = 31
            //31 (PendingConfirmation)  User, Admin
            //32 (Confirmed)            Admin
            //33 (Processing)           User, Admin
            //34 (Shipping)             User, Admin
            //35 (Completed)            User, Admin
            //36 (Cancelled)            User, Admin
            // Ngoài ra còn có bên Vận chuyển...

    constructor() {}

    constructor(id: Long, name: String?, email: String?, phone: String?,
                address: String?, amount: Int, foods: String?, payment: Int, status: Int) {
        this.id = id
        this.name = name
        this.email = email
        this.phone = phone
        this.address = address
        this.amount = amount
        this.foods = foods
        this.payment = payment
        this.status = status
    }
}