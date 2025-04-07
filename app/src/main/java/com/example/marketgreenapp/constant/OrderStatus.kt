package com.example.marketgreenapp.constant

object OrderStatus {
    const val PENDING = "Chờ xử lý"
    const val WAITING_FOR_PICKUP = "Chờ lấy hàng"
    const val IN_TRANSIT = "Chờ giao hàng"
    const val COMPLETED = "Đã giao hàng"
    const val CANCEL = "Đơn hàng đã bị hủy"
    const val TRANSACTION_PRODUCT_FAIL = "Đơn hàng không giao thành công"
}