package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemProductBinding
import com.example.marketgreenapp.listener.IOnClickProduct
import com.example.marketgreenapp.model.Product
import java.text.NumberFormat
import java.util.*

class ProductAdapter(private var mListProduct: MutableList<Product>?,
    private val iOnClickProductShop: IOnClickProductShop,
    private val iOnClickProduct: IOnClickProduct)
    : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun setDataFilter(mListProduct: MutableList<Product>?)
    {
        this.mListProduct = mListProduct
        notifyDataSetChanged()
    }

    interface IOnClickProductShop
    {
        fun onClickProductShop(product: Product)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val productShopBinding:LayoutItemProductBinding
            = LayoutItemProductBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ProductViewHolder(productShopBinding)
    }

    override fun getItemCount(): Int {
        return mListProduct?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val productCurrent = mListProduct?.get(position)
        if(productCurrent != null)
        {
            holder.bindData(productCurrent)
            holder.productBinding!!.constraintLayoutShop.setOnClickListener {
                iOnClickProductShop.onClickProductShop(productCurrent)
            }
            holder.productBinding.layoutProductRoot.setOnClickListener {
                iOnClickProduct.onClickProduct(productCurrent)
            }
        }
    }

    inner class ProductViewHolder(val productBinding: LayoutItemProductBinding?) :
        RecyclerView.ViewHolder(productBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(product: Product) {
            GlideImageURL.loadImageURL(product.imagePrimary,productBinding!!.imgProduct)
            productBinding.txtTitleProduct.text = product.titleProduct
            val formattedPrice = NumberFormat.getNumberInstance(Locale.GERMANY).format(product.price!!.toInt())
            productBinding.txtPrice.text = "$formattedPrice.000VNĐ"
            productBinding.txtNumberProductSold.text = "đã bán ${product.soldQuantity}"

            productBinding.txtNameShop.text = product.shopUserName
        }
    }
}