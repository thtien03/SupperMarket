package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemProductOrderBinding
import com.example.marketgreenapp.model.ProductOrder
import java.text.NumberFormat
import java.util.*

class ProductOrderAdapter(
    private val mListProduct: MutableList<ProductOrder>?,
    private val iOnClickProductOrder: IOnClickProductOrder
) :
    RecyclerView.Adapter<ProductOrderAdapter.ProductOrderViewHolder>() {

    interface IOnClickProductOrder {
        fun onClickDelete(product: ProductOrder, position: Int)
        fun onClickAccessShop(product: ProductOrder)
        fun onQuantityChanged(product: ProductOrder, position: Int)
        fun onCheckBoxChanged(product: ProductOrder, isChecked: Boolean)
        //fun onQuantityUpdated(product: ProductOrder, position: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductOrderViewHolder {
        val productOrderBinding: LayoutItemProductOrderBinding =
            LayoutItemProductOrderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ProductOrderViewHolder(productOrderBinding)
    }

    override fun getItemCount(): Int {
        return mListProduct?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProductOrderViewHolder, position: Int) {
        val currentProductOrder = mListProduct?.get(position)
        if (currentProductOrder != null) {
            holder.bindData(currentProductOrder)

            holder.productOrderBinding!!.chkPaymenty.isChecked = currentProductOrder.isChecked

            holder.productOrderBinding.imgDelete.setOnClickListener {
                iOnClickProductOrder.onClickDelete(currentProductOrder, position)
            }
            holder.productOrderBinding.txtAccessShop.setOnClickListener {
                iOnClickProductOrder.onClickAccessShop(currentProductOrder)
            }
            holder.productOrderBinding.txtNumber.text = currentProductOrder.quantityBuy.toString()
            holder.productOrderBinding.btnDecreased.setOnClickListener {
                if (currentProductOrder.quantityBuy > 1) {
                    currentProductOrder.quantityBuy -= 1
                    iOnClickProductOrder.onQuantityChanged(currentProductOrder, position)
                    if (currentProductOrder.isChecked) {
                        iOnClickProductOrder.onCheckBoxChanged(currentProductOrder, true)
                    }
                    notifyItemChanged(position)
                    //iOnClickProductOrder.onQuantityUpdated(currentProductOrder, position)
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Số lượng tối thiểu là 1",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            holder.productOrderBinding.btnIncrease.setOnClickListener {
                if (currentProductOrder.quantityBuy < 10) {
                    if (currentProductOrder.quantityBuy < currentProductOrder.quantityWareHouse) {
                        currentProductOrder.quantityBuy += 1
                        iOnClickProductOrder.onQuantityChanged(currentProductOrder, position)
                        if (currentProductOrder.isChecked) {
                            iOnClickProductOrder.onCheckBoxChanged(currentProductOrder, true)
                        }
                        notifyItemChanged(position)
                        //iOnClickProductOrder.onQuantityUpdated(currentProductOrder, position)
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Bạn mua sản phẩm vượt quá số lượng kho hàng rồi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Bạn chỉ được mua tối đa 10 sản phẩm",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            holder.productOrderBinding.chkPaymenty.setOnCheckedChangeListener { _, isChecked ->
                holder.productOrderBinding.chkPaymenty.isChecked = isChecked
                iOnClickProductOrder.onCheckBoxChanged(currentProductOrder, isChecked)
            }
        }
    }

    inner class ProductOrderViewHolder(val productOrderBinding: LayoutItemProductOrderBinding?) :
        RecyclerView.ViewHolder(productOrderBinding!!.root) {

        @SuppressLint("SetTextI18n")
        fun bindData(product: ProductOrder) {
            GlideImageURL.loadImageURL(product.imagePrimary, productOrderBinding!!.imgProduct)
            productOrderBinding.txtNameShop.text = product.shopUserName.toString().trim()
            productOrderBinding.txtTitleProduct.text = product.titleProduct.toString().trim()
            val formattedPrice =
                NumberFormat.getNumberInstance(Locale.GERMANY).format(product.price!!.toInt())
            productOrderBinding.txtPrice.text = "$formattedPrice.000VNĐ"

            if (product.sizeColor?.isNotEmpty() == true) {
                productOrderBinding.txtSizeColor.visibility = View.VISIBLE
                productOrderBinding.txtSizeColor.text = product.sizeColor.toString()
            }
        }
    }
}