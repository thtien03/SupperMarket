package com.example.marketgreenapp.model

class Voucher : java.io.Serializable{
    var id:Long = 0
    var title:String? = null
    var content:String? = null
    var image:String? = null
    var date_valid:String? = null

    constructor(){}
    constructor(id: Long, title: String?, content: String?,image:String?,date_valid:String?) {
        this.id = id
        this.title = title
        this.content = content
        this.image = image
        this.date_valid = date_valid
    }
}