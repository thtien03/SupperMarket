package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemTransactionBinding
import com.example.marketgreenapp.model.ProductOrder
import java.text.NumberFormat
import java.util.*

class ProductTransactionAdapter(private val mListProduct: MutableList<ProductOrder>?) :
    RecyclerView.Adapter<ProductTransactionAdapter.ProductTransactionViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductTransactionViewHolder {
        val productTransactionBinding: LayoutItemTransactionBinding =
            LayoutItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductTransactionViewHolder(productTransactionBinding)
    }

    override fun getItemCount(): Int {
        return mListProduct?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProductTransactionViewHolder, position: Int) {
        val currentProductTransaction = mListProduct?.get(position)
        if (currentProductTransaction != null) {
            holder.bindData(currentProductTransaction)
        }
    }

    inner class ProductTransactionViewHolder(private val itemTransactionBinding: LayoutItemTransactionBinding?) :
        RecyclerView.ViewHolder(itemTransactionBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(productOrder: ProductOrder) {
            GlideImageURL.loadImageURL(
                productOrder.imagePrimary,
                itemTransactionBinding!!.imgProduct
            )
            itemTransactionBinding.txtTitleProduct.text = productOrder.titleProduct
            itemTransactionBinding.txtNumberProduct.text = "x${productOrder.quantityBuy}"
            val formattedPrice =
                NumberFormat.getNumberInstance(Locale.GERMANY).format(productOrder.price!!.toInt())
            itemTransactionBinding.txtPrice.text = "${formattedPrice}.000VNĐ"
            if (productOrder.sizeColor?.isNotEmpty() == true) {
                itemTransactionBinding.txtSizeColor.visibility = View.VISIBLE
                itemTransactionBinding.txtSizeColor.text = productOrder.sizeColor.toString()
            }
        }
    }

}