package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.R
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemSaleBinding
import com.example.marketgreenapp.listener.IOnClickItemVoucher
import com.example.marketgreenapp.model.Voucher

class VoucherAdapter(
    private val mListVouchers: MutableList<Voucher>?,
    private val listener: IOnClickItemVoucher
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    private var selectedPosition = -1
    private var selectedVoucher: Voucher? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val voucherBinding: LayoutItemSaleBinding =
            LayoutItemSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VoucherViewHolder(voucherBinding)
    }

    override fun getItemCount(): Int {
        return mListVouchers?.size ?: 0
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucherCurrent = mListVouchers?.get(position)
        if (voucherCurrent != null) {
            holder.bindData(voucherCurrent, position)
        }
    }

    inner class VoucherViewHolder(private val itemVoucherBinding: LayoutItemSaleBinding?) :
        RecyclerView.ViewHolder(itemVoucherBinding!!.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bindData(voucher: Voucher, position: Int) {
            itemVoucherBinding?.txtTitleSale?.text = voucher.title
            itemVoucherBinding?.txtRequiredSale?.text = voucher.content
            itemVoucherBinding?.txtDateValidSale?.text = voucher.date_valid

            itemVoucherBinding?.imgSale?.setImageResource(R.drawable.ic_sale_3)

            itemVoucherBinding?.chkSelectedSale?.isChecked = position == selectedPosition
            itemVoucherBinding?.chkSelectedSale?.setOnClickListener { checkbox ->
                val isChecked = (checkbox as CheckBox).isChecked
                if (isChecked) {
                    selectedPosition = position
                    selectedVoucher = voucher
                    listener.onClickItemVoucher(voucher) // Chỉ gọi callback nếu checkbox được check
                } else {
                    selectedPosition = -1 // Reset lại khi không chọn
                    selectedVoucher = null
                    listener.onClickItemVoucher(null)
                }
                notifyDataSetChanged() // Cập nhật giao diện
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setInvalidSelection() {
        selectedPosition = -1 // Reset vị trí được chọn
        notifyDataSetChanged() // Cập nhật lại giao diện RecyclerView
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedVoucher(selectedVoucher: Voucher?) {
        this.selectedVoucher = selectedVoucher
        selectedPosition = mListVouchers?.indexOfFirst { it.id == selectedVoucher?.id } ?: -1
        // Log vị trí được chọn
        Log.d("VoucherAdapter", "Selected position: $selectedPosition")
        notifyDataSetChanged()
    }
}