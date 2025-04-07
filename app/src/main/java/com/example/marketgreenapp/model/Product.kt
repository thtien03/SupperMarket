package com.example.marketgreenapp.model

class Product : java.io.Serializable {
    var productId:Long? = 0
    var imagePrimary:String? = null
    var titleProduct:String? = null
    var description:String? = null
    var imageDescription:String? = null
    var price:String? = null
    var quantity:Int = 0
    var soldQuantity:Int = 0
    var category:Int =0
    var categoryName:String? = null
    var shopUserId:String? = null
    var shopUserName:String? = null
    var imagesDetails:MutableList<String> = mutableListOf()
    var ratings:Float = 0f
    var totalReviews: Int = 0
    var variations: MutableList<Variation> = mutableListOf() // Danh sách biến thể

    constructor(){}
    constructor(
        productId: Long?,
        imagePrimary: String?,
        titleProduct: String?,
        description: String?,
        imageDescription: String?,
        price: String?,
        quantity: Int,
        soldQuantity: Int,
        category: Int,
        categoryName:String?,
        shopUserId: String?,
        shopUserName:String?,
        imagesDetails: MutableList<String>,
        ratings: Float,
        totalReviews: Int,
        variations: MutableList<Variation>
    ) {
        this.productId = productId
        this.imagePrimary = imagePrimary
        this.titleProduct = titleProduct
        this.description = description
        this.imageDescription = imageDescription
        this.price = price
        this.quantity = quantity
        this.soldQuantity = soldQuantity
        this.category = category
        this.categoryName = categoryName
        this.shopUserId = shopUserId
        this.shopUserName = shopUserName
        this.imagesDetails = imagesDetails
        this.ratings = ratings
        this.totalReviews = totalReviews
        this.variations = variations
    }

    constructor(
        productId: Long?,
        imagePrimary: String?,
        titleProduct: String?,
        shopUserId: String?,
        shopUserName: String?
    ) {
        this.productId = productId
        this.imagePrimary = imagePrimary
        this.titleProduct = titleProduct
        this.shopUserId = shopUserId
        this.shopUserName = shopUserName
    }

}