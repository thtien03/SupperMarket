package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.databinding.LayoutItemProcessTransactionProductBinding
import com.example.marketgreenapp.databinding.LayoutItemProductOrderAwaitProcessBinding
import com.example.marketgreenapp.model.DeliveryAddress
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.references.DataStoreManager

class ProcessTransactionProductAdapter(
    private var mListProductStatus: MutableList<ProductStatus>?,
    private val iOnClickProcessProduct: IOnClickProcessProduct
) :
    RecyclerView.Adapter<ProcessTransactionProductAdapter.ProcessTransactionProductViewHolder>() {


    interface IOnClickProcessProduct {
        fun onClickProcessSuccess(product: ProductStatus, position: Int)
        fun onClickProcessFail(product: ProductStatus,position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataFilter(mListProduct: MutableList<ProductStatus>?) {
        this.mListProductStatus = mListProduct
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProcessTransactionProductViewHolder {
        val itemProductProcessBinding: LayoutItemProcessTransactionProductBinding =
            LayoutItemProcessTransactionProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ProcessTransactionProductViewHolder(itemProductProcessBinding)
    }

    override fun getItemCount(): Int {
        return mListProductStatus?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProcessTransactionProductViewHolder, position: Int) {
        val productStatusCurrent = mListProductStatus?.get(position)
        if (productStatusCurrent != null) {
            holder.bindData(productStatusCurrent)

            val currentUserDataStoreManager = DataStoreManager.getUser()
            holder.productProcessBinding!!.btnDetailProduct.setOnClickListener {
                holder.productProcessBinding.constraintLayoutDetailProduct.visibility = View.VISIBLE
            }
            holder.productProcessBinding.btnCollapsedDetailProduct.setOnClickListener {
                holder.productProcessBinding.constraintLayoutDetailProduct.visibility = View.GONE
            }
            holder.productProcessBinding.btnTransactionSuccess.setOnClickListener {
                iOnClickProcessProduct.onClickProcessSuccess(productStatusCurrent, position)
            }
            holder.productProcessBinding.btnTransactionFailed.setOnClickListener {
                iOnClickProcessProduct.onClickProcessFail(productStatusCurrent,position)
            }
        }
    }

    inner class ProcessTransactionProductViewHolder(val productProcessBinding: LayoutItemProcessTransactionProductBinding?) :
        RecyclerView.ViewHolder(productProcessBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(productStatus: ProductStatus) {
            productProcessBinding!!.txtNameShop.text = productStatus.shopUserName
            productProcessBinding.txtNameProduct.text = productStatus.titleProduct
            GlideImageURL.loadImageURL(productStatus.imagePrimary, productProcessBinding.imgProduct)
            productProcessBinding.txtPriceProduct.text = productStatus.productDetail!!.priceTotal
            productProcessBinding.txtCodeOrder.text = productStatus.productStatusId.toString()
            productProcessBinding.txtStatus.text = productStatus.orderStatus
            productProcessBinding.txtNumber.text = "Số lượng : ${productStatus.quantityBuy}"

            if (productStatus.deliveryAddress != null) {
                productProcessBinding.txtNameUser.text =
                    productStatus.deliveryAddress!!.name_receive.toString()
                productProcessBinding.txtPhoneNumber.text =
                    productStatus.deliveryAddress!!.phone.toString()
                productProcessBinding.txtDeliveryAddress.text =
                    productStatus.deliveryAddress!!.address.toString()
            }

            productProcessBinding.txtDataCodeTransaction.text =
                productStatus.productStatusId.toString()
            if (productStatus.productDetail != null) {
                productProcessBinding.txtDataDateTransaction.text =
                    productStatus.productDetail!!.dateOrder.toString()
                productProcessBinding.txtDataPriceSecondary.text =
                    productStatus.productDetail!!.priceProduct.toString()
                productProcessBinding.txtDataPriceTransaction.text =
                    productStatus.productDetail!!.priceTransaction.toString()
                productProcessBinding.txtDataPriceVoucher.text =
                    productStatus.productDetail!!.priceVoucher.toString()
                productProcessBinding.txtDataTotalPrice.text =
                    productStatus.productDetail!!.priceTotal.toString()
            }
            productProcessBinding.txtDataQuantityBuy.text =
                productStatus.quantityBuy.toString()
            productProcessBinding.txtDataMethodPayment.text =
                productStatus.methodPayment.toString()

            val deliveryAddressUserOrder = productStatus.deliveryAddress
            if (deliveryAddressUserOrder != null) {
                productProcessBinding.txtDataName.text =
                    deliveryAddressUserOrder.name_receive.toString()
                productProcessBinding.txtDataAddress.text =
                    deliveryAddressUserOrder.address.toString()
                productProcessBinding.txtDataPhone.text =
                    deliveryAddressUserOrder.phone.toString()
            }

        }
    }

}