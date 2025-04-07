package com.example.marketgreenapp.model

import kotlin.math.cos

class CodeVoucher : java.io.Serializable {
    var id:Long = 0
    var code:String? = null
    var date_valid:String? = null
    var cost_code:Long = 0

    constructor(){}
    constructor(id: Long, code: String?,date_valid:String?,cost_code:Long) {
        this.id = id
        this.code = code
        this.date_valid = date_valid
        this.cost_code = cost_code
    }
}