package com.example.marketgreenapp.listener

import com.example.marketgreenapp.model.CodeVoucher
import com.example.marketgreenapp.model.Voucher

interface IOnClickItemCodeVoucher {
    fun onClickEdit(voucher: CodeVoucher,position: Int)
    fun onClickRemove(voucher: CodeVoucher,position:Int)
}