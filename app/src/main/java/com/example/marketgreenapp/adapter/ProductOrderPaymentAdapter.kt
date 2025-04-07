package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemProductPaymentBinding
import com.example.marketgreenapp.model.ProductOrder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ProductOrderPaymentAdapter(private val mListProductOrder: MutableList<ProductOrder>?) :
    RecyclerView.Adapter<ProductOrderPaymentAdapter.ProductOrderPaymentViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductOrderPaymentViewHolder {
        val productPaymentBinding: LayoutItemProductPaymentBinding =
            LayoutItemProductPaymentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ProductOrderPaymentViewHolder(productPaymentBinding)
    }

    override fun getItemCount(): Int {
        return mListProductOrder?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProductOrderPaymentViewHolder, position: Int) {
        val currentProductPayment = mListProductOrder?.get(position)
        if (currentProductPayment != null) {
            holder.bindData(currentProductPayment)
        }
    }

    inner class ProductOrderPaymentViewHolder(private val productPaymentBinding: LayoutItemProductPaymentBinding?) :
        RecyclerView.ViewHolder(productPaymentBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(productOrder: ProductOrder) {
            GlideImageURL.loadImageURL(
                productOrder.imagePrimary,
                productPaymentBinding!!.imgProduct
            )
            productPaymentBinding.txtNameShop.text = productOrder.shopUserName
            val formattedPrice =
                NumberFormat.getNumberInstance(Locale.GERMANY).format(productOrder.price!!.toInt())
            productPaymentBinding.txtPriceProduct.text = "${formattedPrice}.000VNĐ"
            productPaymentBinding.txtNameProduct.text = productOrder.titleProduct
            productPaymentBinding.txtNumber.text = "số lượng ${productOrder.quantityBuy}"

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(calendar.time)
            productPaymentBinding.txtDateOrder.text = "Ngày đặt : $currentDate"

            if (productOrder.sizeColor?.isNotEmpty() == true) {
                productPaymentBinding.txtSizeColor.visibility = View.VISIBLE
                productPaymentBinding.txtSizeColor.text =
                    productOrder.sizeColor
            }
        }
    }

}