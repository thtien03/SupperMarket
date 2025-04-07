package com.example.marketgreenapp.model

class ProductOrder : java.io.Serializable {
    var productId:Long? = 0
    var imagePrimary:String? = null
    var titleProduct:String? = null
    var price:String? = null
    var quantityBuy:Int = 0
    var quantityWareHouse:Int = 0
    var shopUserId:String? = null
    var shopUserName:String? = null
    var isChecked: Boolean = false
    // Danh sách biến thể
    //var variation:Variation? = null
    var sizeColor:String? = null

    constructor(){}
    constructor(
        productId: Long?,
        imagePrimary: String?,
        titleProduct: String?,
        price: String?,
        quantityBuy: Int,
        quantityWareHouse:Int,
        shopUserId: String?,
        shopUserName:String?,
        sizeColor:String?
        //variation:Variation?
    ) {
        this.productId = productId
        this.imagePrimary = imagePrimary
        this.titleProduct = titleProduct
        this.price = price
        this.quantityBuy = quantityBuy
        this.quantityWareHouse = quantityWareHouse
        this.shopUserId = shopUserId
        this.shopUserName = shopUserName
        this.isChecked = false
        this.sizeColor = sizeColor
        //this.variation = variation
    }
}