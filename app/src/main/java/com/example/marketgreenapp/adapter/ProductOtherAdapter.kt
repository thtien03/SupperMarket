package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemProductOtherBinding
import com.example.marketgreenapp.listener.IOnClickProduct
import com.example.marketgreenapp.model.Product
import java.text.NumberFormat
import java.util.*

class ProductOtherAdapter(
    private val mListProduct: MutableList<Product>?,
    private val iOnClickProduct: IOnClickProduct
) : RecyclerView.Adapter<ProductOtherAdapter.ProductOtherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductOtherViewHolder {
        val productOtherBinding: LayoutItemProductOtherBinding =
            LayoutItemProductOtherBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ProductOtherViewHolder(productOtherBinding)
    }

    override fun getItemCount(): Int {
        return mListProduct?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProductOtherViewHolder, position: Int) {
        val currentProduct = mListProduct?.get(position)
        if (currentProduct != null) {
            holder.bindData(currentProduct)
            holder.productOtherBinding!!.productOtherRoot.setOnClickListener {
                iOnClickProduct.onClickProduct(product = currentProduct)
            }
        }
    }

    inner class ProductOtherViewHolder(val productOtherBinding: LayoutItemProductOtherBinding?) :
        RecyclerView.ViewHolder(productOtherBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(product: Product) {
            GlideImageURL.loadImageURL(product.imagePrimary, productOtherBinding?.imgProduct)
            productOtherBinding?.txtTitleProduct!!.text = product.titleProduct
            val formattedPrice = NumberFormat.getNumberInstance(Locale.GERMANY).format(product.price!!.toInt())
            productOtherBinding.txtPrice.text = "$formattedPrice.000VNĐ"
            productOtherBinding.txtNumberProductSold.text = "đã bán ${product.soldQuantity}"
        }
    }

}