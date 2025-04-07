package com.example.marketgreenapp.model

class ProductDetail : java.io.Serializable {
    var productDetaiId: Long? = 0
    var productId: Long? = 0
    var imageProduct: String? = null
    var nameProduct: String? = null
    var dateOrder: String? = null
    var methodPayment: String? = null
    var priceProduct: String? = null
    var priceTransaction: String? = null
    var priceVoucher: String? = null
    var priceTotal: String? = null
    var quantityBuy: Int = 0
    var deliveryAddress: DeliveryAddress? = null
    var nameUserId: String? = null
    var sizeColor:String? = null


    constructor() {}

    constructor(
        productDetaiId: Long?,
        productId: Long?,
        imageProduct: String,
        nameProduct: String?,
        dateOrder: String?,
        methodPayment: String?,
        priceProduct: String?,
        priceTransaction: String?,
        priceVoucher: String?,
        priceTotal: String?,
        quantityBuy: Int,
        deliveryAddress: DeliveryAddress?,
        nameUserId: String? = null,
        sizeColor:String?
    ) {
        this.productDetaiId = productDetaiId
        this.productId = productId
        this.imageProduct = imageProduct
        this.nameProduct = nameProduct
        this.dateOrder = dateOrder
        this.methodPayment = methodPayment
        this.priceProduct = priceProduct
        this.priceTransaction = priceTransaction
        this.priceVoucher = priceVoucher
        this.priceTotal = priceTotal
        this.quantityBuy = quantityBuy
        this.deliveryAddress = deliveryAddress
        this.nameUserId = nameUserId
        this.sizeColor = sizeColor
    }
}