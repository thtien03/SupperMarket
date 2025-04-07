package com.example.marketgreenapp.model

class Category : java.io.Serializable {

    var id:Int = 0
    var name:String? = null
    var image:Int? = 0

    constructor(){}
    constructor(id: Int,name: String?,image:Int?) {
        this.id = id
        this.name = name
        this.image = image
    }
}