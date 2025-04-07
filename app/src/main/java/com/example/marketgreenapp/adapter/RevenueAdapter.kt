package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemRevenueBinding
import com.example.marketgreenapp.model.ProductStatus

class RevenueAdapter(private var mListProductOrderSuccess: MutableList<ProductStatus>?) :
    RecyclerView.Adapter<RevenueAdapter.RevenueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevenueViewHolder {
        val revenueBinding: LayoutItemRevenueBinding =
            LayoutItemRevenueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RevenueViewHolder(revenueBinding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(listRevenue: MutableList<ProductStatus>?) {
        this.mListProductOrderSuccess = listRevenue
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mListProductOrderSuccess?.size ?: 0
    }

    override fun onBindViewHolder(holder: RevenueViewHolder, position: Int) {
        val revenueCurrent = mListProductOrderSuccess?.get(position)
        if (revenueCurrent != null) {
            holder.bindData(revenueCurrent)
            val isExpandable: Boolean = revenueCurrent.isExpandable
            holder.revenueBinding!!.linearLayoutInformationOrder.visibility =
                if (isExpandable) View.VISIBLE else View.GONE
            holder.revenueBinding.constraintLayoutOrder.setOnClickListener {
                isAnyItemExpanded(position)
                revenueCurrent.isExpandable = !revenueCurrent.isExpandable
                notifyItemChanged(position)
            }
        }
    }

    private fun isAnyItemExpanded(position: Int) {
        val temp = mListProductOrderSuccess?.indexOfFirst {
            it.isExpandable
        }
        if (temp != null) {
            if (temp >= 0 && temp != position) {
                mListProductOrderSuccess!![temp].isExpandable = false
                notifyItemChanged(temp)
            }
        }
    }

    inner class RevenueViewHolder(val revenueBinding: LayoutItemRevenueBinding?) :
        RecyclerView.ViewHolder(revenueBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(revenueCurrent: ProductStatus) {
            GlideImageURL.loadImageURL(revenueCurrent.imagePrimary, revenueBinding!!.imgProduct)
            revenueBinding.txtTitleProduct.text = revenueCurrent.titleProduct
            revenueBinding.txtIdProduct.text = revenueCurrent.productId.toString()
            revenueBinding.txtNameOrder.text = revenueCurrent.deliveryAddress!!.name_receive
            revenueBinding.txtNameProduct.text = revenueCurrent.titleProduct
            revenueBinding.txtDateOrder.text = revenueCurrent.productDetail!!.dateOrder
            revenueBinding.txtTotalPrice.text = revenueCurrent.priceTotal
            revenueBinding.txtPrice.text = revenueCurrent.productDetail!!.priceProduct.toString()
            revenueBinding.txtNumberProduct.text = "x${revenueCurrent.quantityBuy}"
        }
    }

}