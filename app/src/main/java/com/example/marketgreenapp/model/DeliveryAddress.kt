package com.example.marketgreenapp.model

class DeliveryAddress : java.io.Serializable {
    var id:Long = 0
    var name_receive:String? = null
    var phone:String? = null
    var address:String? = null
    var isDefault:Boolean = false

    constructor(){

    }

    constructor(id: Long, name_receive: String?, phone: String?, address: String?,isDefault:Boolean) {
        this.id = id
        this.name_receive = name_receive
        this.phone = phone
        this.address = address
        this.isDefault = isDefault
    }

}

/*
    address_delivery
        "id_user_1"
            "id_address_delivery_new1"
                "key":"value"
            "id_address_delivery_new2"
                "key":"value"
        "id_user_2"
            "id_address_delivery_new1"
                "key":"value"
            "id_address_delivery_new2"
                "key":"value"
* */