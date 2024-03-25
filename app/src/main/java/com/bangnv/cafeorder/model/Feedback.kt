package com.bangnv.cafeorder.model

class Feedback {

    var name: String? = null
    var phone: String? = null
    var email: String? = null
    var comment: String? = null

    constructor() {}

    constructor(name: String?, phone: String?, email: String?, comment: String?) {
        this.name = name
        this.phone = phone
        this.email = email
        this.comment = comment
    }
}