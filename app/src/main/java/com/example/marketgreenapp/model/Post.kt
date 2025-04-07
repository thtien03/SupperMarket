package com.example.marketgreenapp.model

class Post : java.io.Serializable {
    var id: Long? = 0
    var title:String? = null
    var content: String? = null
    var imageUrl: String? = null
    var datePost: String? = null
    var colorBackground:String? = null
    var quantityLike: Int = 0
    var quantityComment: Int = 0
    var userId: String? = null
    var isRole:String? = null
    var usernamePost: String? = null
    var likes: Map<String, Boolean>? = null

    constructor() {}
    constructor(
        id: Long?,
        title: String?,
        content: String?,
        imageUrl: String?,
        datePost: String?,
        colorBackground: String?,
        quantityLike: Int,
        quantityComment: Int,
        isRole:String?,
        userId: String?,
        usernamePost: String?,
    ) {
        this.id = id
        this.title = title
        this.content = content
        this.imageUrl = imageUrl
        this.datePost = datePost
        this.colorBackground = colorBackground
        this.quantityLike = quantityLike
        this.quantityComment = quantityComment
        this.userId = userId
        this.isRole = isRole
        this.usernamePost = usernamePost
        this.likes = hashMapOf()
    }
}