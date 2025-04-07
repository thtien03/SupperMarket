package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.R
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemSaleAdminBinding
import com.example.marketgreenapp.listener.IOnClickItemVoucherAdmin
import com.example.marketgreenapp.model.Voucher

class VoucherAdminAdapter(private val mListVouchers: MutableList<Voucher>?,
        private val iOnClickItemVoucherAdmin: IOnClickItemVoucherAdmin)
        : RecyclerView.Adapter<VoucherAdminAdapter.VoucherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val voucherBinding: LayoutItemSaleAdminBinding
            = LayoutItemSaleAdminBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return VoucherViewHolder(voucherBinding)
    }

    override fun getItemCount(): Int {
        return mListVouchers?.size ?: 0
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucherCurrent = mListVouchers?.get(position)
        if(voucherCurrent != null)
        {
            holder.bindData(voucherCurrent)
            holder.itemVoucherAdminBinding?.imgSaleEdit?.setOnClickListener {
                iOnClickItemVoucherAdmin.onClickEdit(voucherCurrent,position)
            }
            holder.itemVoucherAdminBinding?.imgSaleDelete?.setOnClickListener {
                iOnClickItemVoucherAdmin.onClickRemove(voucherCurrent,position)
            }
        }
    }
    inner class VoucherViewHolder( val itemVoucherAdminBinding: LayoutItemSaleAdminBinding?) :
        RecyclerView.ViewHolder(itemVoucherAdminBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(voucher: Voucher)
        {
            itemVoucherAdminBinding?.txtTitleSale?.text = voucher.title
            itemVoucherAdminBinding?.txtRequiredSale?.text = voucher.content
            itemVoucherAdminBinding?.txtDateValidSale?.text = "Hiệu lực đến ngày : ${voucher.date_valid}"

            itemVoucherAdminBinding?.imgSale?.setImageResource(R.drawable.ic_sale_3)
            //GlideImageURL.loadImageURL(voucher.image,itemVoucherAdminBinding?.imgSale)
        }
    }

}