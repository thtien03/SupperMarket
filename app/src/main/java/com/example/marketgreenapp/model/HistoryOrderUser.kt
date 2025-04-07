package com.example.marketgreenapp.model

class HistoryOrderUser : java.io.Serializable {
    var productOrderId: Long? = 0
    var dateOrder: String? = null
    var methodPayment: String? = null
    var priceProduct: String? = null
    var priceTransaction: String? = null
    var priceVoucher: String? = null
    var priceTotal: String? = null
    var quantityBuy: Int = 0
    var deliveryAddress:DeliveryAddress? = null
    var nameUserId:String? = null
    var productOrders:MutableList<ProductOrder> = mutableListOf()



    constructor(){}

    constructor(
        productOrderId: Long?,
        dateOrder: String?,
        methodPayment: String?,
        priceProduct: String?,
        priceTransaction: String?,
        priceVoucher: String?,
        priceTotal: String?,
        quantityBuy: Int,
        deliveryAddress: DeliveryAddress?,
        nameUserId:String? = null,
        productOrders: MutableList<ProductOrder>,
    ) {
        this.productOrderId = productOrderId
        this.dateOrder = dateOrder
        this.methodPayment = methodPayment
        this.priceProduct = priceProduct
        this.priceTransaction = priceTransaction
        this.priceVoucher = priceVoucher
        this.priceTotal = priceTotal
        this.quantityBuy = quantityBuy
        this.deliveryAddress = deliveryAddress
        this.nameUserId = nameUserId
        this.productOrders = productOrders
    }
}