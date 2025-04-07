package com.example.marketgreenapp.model

class AnswerHelp : java.io.Serializable{
    var id:Int = 0
    var question:String? = null
    var content:String? = null
    var link:Int? = 0

    constructor(){}
    constructor(id: Int, question: String?, content: String?,link:Int?) {
        this.id = id
        this.question = question
        this.content = content
        this.link = link
    }
}