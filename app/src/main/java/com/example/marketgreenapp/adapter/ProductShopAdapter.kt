package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemProductShopBinding
import com.example.marketgreenapp.listener.IOnClickProductShop
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.references.DataStoreManager
import java.text.NumberFormat
import java.util.*

class ProductShopAdapter(private var mListProduct: MutableList<Product>?,
    private val iOnClickProductShop: IOnClickProductShop
) :
    RecyclerView.Adapter<ProductShopAdapter.ProductShopViewHolder>() {



    @SuppressLint("NotifyDataSetChanged")
    fun setDataFilter(mListProduct: MutableList<Product>?)
    {
        this.mListProduct = mListProduct
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductShopViewHolder {
        val productShopBinding: LayoutItemProductShopBinding =
            LayoutItemProductShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductShopViewHolder(productShopBinding)
    }

    override fun getItemCount(): Int {
        return mListProduct?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProductShopViewHolder, position: Int) {
        val productCurrent = mListProduct?.get(position)
        if (productCurrent != null) {
            holder.bindData(productCurrent)
            val userManager = DataStoreManager.getUser()
            if(userManager?.uid != null)
            {
                if(userManager.role.equals("shop"))
                {
                    holder.productShopBinding!!.btnUpdateProduct.visibility = View.VISIBLE
                    holder.productShopBinding.btnUpdateProduct.setOnClickListener {
                        iOnClickProductShop.onClickUpdateProduct(productCurrent,position)
                    }
                    holder.productShopBinding.constraintLayoutBuy.visibility = View.GONE
                }
                else if(userManager.role.equals("user"))
                {
                    holder.productShopBinding!!.btnUpdateProduct.visibility = View.GONE
                    holder.productShopBinding.constraintLayoutBuy.visibility = View.VISIBLE
                    holder.productShopBinding.btnBuyProduct.setOnClickListener {
                        iOnClickProductShop.onClickBuyProduct(productCurrent)
                    }
                    holder.productShopBinding.imgAddToCart.setOnClickListener {
                         iOnClickProductShop.onClickAddToCart(productCurrent)
                    }
                }
            }
            holder.productShopBinding!!.constraintLayoutShopRoot.setOnClickListener {
                iOnClickProductShop.onClickProduct(productCurrent)
            }
        }
    }

    inner class ProductShopViewHolder( val productShopBinding: LayoutItemProductShopBinding?) :
        RecyclerView.ViewHolder(productShopBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(product: Product) {
            GlideImageURL.loadImageURL(product.imagePrimary, productShopBinding!!.imgProduct)
            productShopBinding.txtTitleProduct.text = product.titleProduct
            productShopBinding.txtNumberProductSold.text = "đã bán ${product.soldQuantity}"
            val formattedPrice = NumberFormat.getNumberInstance(Locale.GERMANY).format(product.price!!.toInt())
            productShopBinding.txtPrice.text = "$formattedPrice.000VNĐ"

        }
    }

}