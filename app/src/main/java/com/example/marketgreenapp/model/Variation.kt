package com.example.marketgreenapp.model

class Variation : java.io.Serializable {
    var size: String? = null // Kích thước (S, M, L, XL, ...)
    var color: String? = null // Màu sắc (Red, Blue, ...)
    var quantity: Int = 0 // Số lượng tồn kho cho biến thể này

    constructor() {}
    constructor(size: String?, color: String?, quantity: Int) {
        this.size = size
        this.color = color
        this.quantity = quantity
    }
}