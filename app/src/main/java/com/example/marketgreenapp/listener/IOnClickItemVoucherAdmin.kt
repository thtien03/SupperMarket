package com.example.marketgreenapp.listener

import com.example.marketgreenapp.model.Voucher

interface IOnClickItemVoucherAdmin {
    fun onClickEdit(voucher: Voucher,position: Int)
    fun onClickRemove(voucher: Voucher,position:Int)
}