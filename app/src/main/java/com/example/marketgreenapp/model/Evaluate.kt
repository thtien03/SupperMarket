package com.example.marketgreenapp.model


//əˈvalyəˌwāt : đánh giá Evaluate
class Evaluate : java.io.Serializable {
    var id:Long? = 0
    var name:String? = null
    var message:String? = null
    var date:String? = null
    var numberStart:Float = 0f

    constructor(){}
    constructor(id: Long?, message: String?, name: String?, numberStart: Float, date: String?) {
        this.id = id
        this.message = message
        this.name = name
        this.numberStart = numberStart
        this.date = date
    }
}
