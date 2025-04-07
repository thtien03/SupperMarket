package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.R
import com.example.marketgreenapp.databinding.LayoutItemCodeVoucherAdminBinding
import com.example.marketgreenapp.listener.IOnClickItemCodeVoucher
import com.example.marketgreenapp.model.CodeVoucher

class CodeVoucherAdapter(private val mListCodeVouchers: MutableList<CodeVoucher>?,
                        private val iOnClickItemCodeVoucher: IOnClickItemCodeVoucher)
        : RecyclerView.Adapter<CodeVoucherAdapter.VoucherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val voucherBinding: LayoutItemCodeVoucherAdminBinding
            = LayoutItemCodeVoucherAdminBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return VoucherViewHolder(voucherBinding)
    }

    override fun getItemCount(): Int {
        return mListCodeVouchers?.size ?: 0
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucherCurrent = mListCodeVouchers?.get(position)
        if(voucherCurrent != null)
        {
            holder.bindData(voucherCurrent)
            holder.itemCodeVoucherAdminBinding?.imgSaleEdit?.setOnClickListener {
                iOnClickItemCodeVoucher.onClickEdit(voucherCurrent,position)
            }
            holder.itemCodeVoucherAdminBinding?.imgSaleDelete?.setOnClickListener {
                iOnClickItemCodeVoucher.onClickRemove(voucherCurrent,position)
            }
        }
    }
    inner class VoucherViewHolder( val itemCodeVoucherAdminBinding: LayoutItemCodeVoucherAdminBinding?) :
        RecyclerView.ViewHolder(itemCodeVoucherAdminBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(voucher: CodeVoucher)
        {
            itemCodeVoucherAdminBinding?.txtCodeVoucher?.text = voucher.code
            itemCodeVoucherAdminBinding?.txtCostCode?.text = "${voucher.cost_code}k"
            itemCodeVoucherAdminBinding?.txtDateValidSale?.text = "Hiệu lực đến ngày : ${voucher.date_valid}"

            itemCodeVoucherAdminBinding?.imgSale?.setImageResource(R.drawable.img_sale)
            //GlideImageURL.loadImageURL(voucher.image,itemVoucherAdminBinding?.imgSale)
        }
    }

}