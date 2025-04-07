package com.example.marketgreenapp.model

class Comment : java.io.Serializable{
    var id: Long? = 0
    var contentComment:String? = null
    var dateSend:String? = null
    var quantityLike:Int = 0
    var userId: String? = null
    var usernameComment: String? = null
    var postId:String? = null
    var likes: Map<String, Boolean>? = null

    constructor(){}
    constructor(
        id: Long?,
        contentComment: String?,
        dateSend: String?,
        quantityLike: Int,
        userId: String?,
        usernameComment: String?,
        postId:String?
    ) {
        this.id = id
        this.contentComment = contentComment
        this.dateSend = dateSend
        this.quantityLike = quantityLike
        this.userId = userId
        this.usernameComment = usernameComment
        this.postId = postId
        this.likes = hashMapOf()
    }
}