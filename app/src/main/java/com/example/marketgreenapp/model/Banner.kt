package com.example.marketgreenapp.model

class Banner : java.io.Serializable {
    var bannerId: Long? = null
    var title: String? = null
    var description: String? = null
    var imageUrl: String? = null
    var linkUrl: String? = null

    constructor(){}
    constructor(
        bannerId: Long?,
        title: String?,
        description: String?,
        imageUrl: String?,
        linkUrl: String?
    ) {
        this.bannerId = bannerId
        this.title = title
        this.description = description
        this.imageUrl = imageUrl
        this.linkUrl = linkUrl
    }
}