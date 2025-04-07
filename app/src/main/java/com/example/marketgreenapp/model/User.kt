package com.example.marketgreenapp.model

import com.google.gson.Gson

class User {
    var uid: String? = null
    var email:String? = null
    var image:String? = null
    var password:String? = null
    var fullname:String? = null
    var phone:String? = null
    var dateOfBirth:String? = null
    var isEnable:Boolean = true
    var role:String? = null

    constructor(){}

    constructor(
        uid: String?,
        email: String?,
        password: String?,
        fullname: String?,
        phone: String?,
        dateOfBirth: String?,
        isEnable: Boolean,
        role:String?
    ) {
        this.uid = uid
        this.email = email
        this.password = password
        this.fullname = fullname
        this.phone = phone
        this.dateOfBirth = dateOfBirth
        this.isEnable = isEnable
        this.role = role
    }
    constructor(
        uid: String?,
        email: String?,
        image:String?,
        fullname: String?,
        isEnable: Boolean,
        role:String?
    ) {
        this.uid = uid
        this.email = email
        this.fullname = fullname
        this.image = image
        this.isEnable = isEnable
        this.role = role
    }
    constructor(
        uid: String?,
        email: String?,
        fullname: String?,
        isEnable: Boolean,
        role:String?
    ) {
        this.uid = uid
        this.email = email
        this.fullname = fullname
        this.isEnable = isEnable
        this.role = role
    }

    constructor(email: String?, password: String?) {
        this.email = email
        this.password = password
    }


    fun toJson():String
    {
        val gson = Gson()
        return gson.toJson(this)
    }
}