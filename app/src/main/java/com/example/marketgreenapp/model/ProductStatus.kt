package com.example.marketgreenapp.model

import com.example.marketgreenapp.constant.OrderStatus

class ProductStatus : java.io.Serializable {
    var productStatusId:Long? = 0
    var productId: Long? = 0
    var imagePrimary: String? = null
    var titleProduct: String? = null
    var priceTotal: String? = null
    var quantityBuy: Int = 0
    var shopUserId: String? = null
    var shopUserName: String? = null
    var nameUserId:String? = null
    var methodPayment: String? = null
    var deliveryAddress: DeliveryAddress? = null
    var orderStatus: String? = OrderStatus.PENDING
    var statusHistory: MutableList<String> = mutableListOf()
    var updateTime: String? = null
    var productDetail:ProductDetail? = null
    var isExpandable:Boolean = false

    constructor() {}
    constructor(
        productStatusId:Long,
        productId: Long?,
        imagePrimary: String?,
        titleProduct: String?,
        priceTotal: String?,
        quantityBuy: Int,
        shopUserId: String?,
        shopUserName: String?,
        nameUserId:String?,
        methodPayment: String?,
        deliveryAddress: DeliveryAddress?,
        orderStatus: String?,
        statusHistory: MutableList<String>,
        updateTime: String?,
        productDetail:ProductDetail?
    ) {
        this.productStatusId = productStatusId
        this.productId = productId
        this.imagePrimary = imagePrimary
        this.titleProduct = titleProduct
        this.priceTotal = priceTotal
        this.quantityBuy = quantityBuy
        this.shopUserId = shopUserId
        this.shopUserName = shopUserName
        this.nameUserId = nameUserId
        this.methodPayment = methodPayment
        this.deliveryAddress = deliveryAddress
        this.orderStatus = orderStatus
        this.statusHistory = statusHistory
        this.updateTime = updateTime
        this.productDetail = productDetail
    }

    constructor(orderStatus: String?, statusHistory: MutableList<String>, updateTime: String?) {
        this.orderStatus = orderStatus
        this.statusHistory = statusHistory
        this.updateTime = updateTime
    }
}

/*
shipper:
    nguyen2kk3
 */