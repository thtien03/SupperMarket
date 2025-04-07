package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.R
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.databinding.LayoutItemProductOrderAwaitProcessBinding
import com.example.marketgreenapp.model.DeliveryAddress
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.references.DataStoreManager

class ProductAwaitProcessAdapter(
    private val mListProductStatus: MutableList<ProductStatus>?,
    private val iOnClickDeliveryAddress: IOnClickDeliveryAddress,
    private val context: Context,
    private val iOnClickProductStatusDetail: IOnClickProductStatusDetail,
    private val iOnClickCancelProductOrder: IOnClickCancelProductOrder
) :
    RecyclerView.Adapter<ProductAwaitProcessAdapter.ProductAwaitProcessViewHolder>() {


    interface IOnClickDeliveryAddress {
        fun onClickDelivery(productStatus: ProductStatus, position: Int)
        fun onClickUpdateStatusOrderProduct(productStatus: ProductStatus, position: Int)
    }

    interface IOnClickProductStatusDetail {
        fun onClickProductDetail(product: ProductStatus, position: Int)
        fun onClickBuyAgainProduct(product: ProductStatus)
        fun onClickEvaluateProduct(product: ProductStatus)
    }

    interface IOnClickCancelProductOrder {
        fun onClickCancelProductOrder(product: ProductStatus, position: Int)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductAwaitProcessViewHolder {
        val itemProductProcessBinding: LayoutItemProductOrderAwaitProcessBinding =
            LayoutItemProductOrderAwaitProcessBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ProductAwaitProcessViewHolder(itemProductProcessBinding)
    }

    override fun getItemCount(): Int {
        return mListProductStatus?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProductAwaitProcessViewHolder, position: Int) {
        val productStatusCurrent = mListProductStatus?.get(position)
        if (productStatusCurrent != null) {
            holder.bindData(productStatusCurrent)

            val currentUserDataStoreManager = DataStoreManager.getUser()
            if (currentUserDataStoreManager != null) {
                if (currentUserDataStoreManager.role.equals("user")) {
                    if (productStatusCurrent.orderStatus!! == OrderStatus.PENDING || productStatusCurrent.orderStatus!! == OrderStatus.WAITING_FOR_PICKUP) {
                        holder.productProcessBinding!!.constraintLayoutDelivery.visibility =
                            View.VISIBLE
                        holder.productProcessBinding.txtChangeDeliveryAddress.visibility =
                            View.VISIBLE
                        holder.productProcessBinding.txtChangeDeliveryAddress.setOnClickListener {
                            iOnClickDeliveryAddress.onClickDelivery(
                                productStatusCurrent,
                                position
                            )
                        }
                        holder.productProcessBinding.btnCancelProduct.visibility = View.VISIBLE
                        holder.productProcessBinding.btnCancelProduct.setOnClickListener {
                            iOnClickCancelProductOrder.onClickCancelProductOrder(
                                productStatusCurrent,
                                position
                            )
                        }
                    } else if (productStatusCurrent.orderStatus!! == OrderStatus.CANCEL) {
                        holder.productProcessBinding!!.btnPaymentNow.visibility = View.VISIBLE
                        holder.productProcessBinding.btnPaymentNow.setOnClickListener {
                            iOnClickProductStatusDetail.onClickBuyAgainProduct(productStatusCurrent)
                        }
                    } else {
                        holder.productProcessBinding!!.constraintLayoutDelivery.visibility =
                            View.GONE
                        holder.productProcessBinding.txtChangeDeliveryAddress.visibility = View.GONE
                        if (productStatusCurrent.orderStatus!! == OrderStatus.COMPLETED) {
                            holder.productProcessBinding.constraintLayoutAction.visibility =
                                View.VISIBLE
                            holder.productProcessBinding.btnBuyAgain.setOnClickListener {
                                iOnClickProductStatusDetail.onClickBuyAgainProduct(
                                    productStatusCurrent
                                )
                            }
                            holder.productProcessBinding.btnEvaluateProduct.setOnClickListener {
                                iOnClickProductStatusDetail.onClickEvaluateProduct(
                                    productStatusCurrent
                                )
                            }
                            holder.productProcessBinding.btnDetailProduct.visibility = View.VISIBLE
                            holder.productProcessBinding.btnDetailProduct.setOnClickListener {
                                iOnClickProductStatusDetail.onClickProductDetail(
                                    productStatusCurrent,
                                    position
                                )
                            }
                        }
                    }
                } else if (currentUserDataStoreManager.role.equals("shop")) {
                    holder.productProcessBinding!!.btnAccessProduct.visibility = View.VISIBLE
                    holder.productProcessBinding.btnDetailProduct.visibility = View.VISIBLE
                    holder.productProcessBinding.btnDetailProduct.setOnClickListener {
                        iOnClickProductStatusDetail.onClickProductDetail(
                            productStatusCurrent,
                            position
                        )
                    }
                    holder.productProcessBinding.btnAccessProduct.setOnClickListener {
                        iOnClickDeliveryAddress.onClickUpdateStatusOrderProduct(
                            productStatusCurrent,
                            position
                        )
                    }
                    if (productStatusCurrent.orderStatus!! == OrderStatus.IN_TRANSIT
                        || productStatusCurrent.orderStatus!! == OrderStatus.COMPLETED
                    ) {
                        holder.productProcessBinding.btnAccessProduct.visibility = View.GONE
                    } else if (productStatusCurrent.orderStatus!! == OrderStatus.TRANSACTION_PRODUCT_FAIL) {
                        holder.productProcessBinding.btnAccessProduct.visibility = View.GONE
                        holder.productProcessBinding.btnDetailProduct.visibility = View.VISIBLE

                        val colorPrimary = ContextCompat.getColor(context, R.color.divider_color)
                        val colorText =
                            ContextCompat.getColor(context, R.color.colorDefaultPostColor3)
                        holder.productProcessBinding.txtStatus.setTextColor(colorText)
                        holder.productProcessBinding.constraintLayoutProductOrder.setBackgroundColor(
                            colorPrimary
                        )
                    }
                }
            }
        }
    }

    inner class ProductAwaitProcessViewHolder(val productProcessBinding: LayoutItemProductOrderAwaitProcessBinding?) :
        RecyclerView.ViewHolder(productProcessBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(productStatus: ProductStatus) {
            productProcessBinding!!.txtNameShop.text = productStatus.shopUserName
            productProcessBinding.txtNameProduct.text = productStatus.titleProduct
            GlideImageURL.loadImageURL(productStatus.imagePrimary, productProcessBinding.imgProduct)
            productProcessBinding.txtPriceProduct.text = productStatus.productDetail!!.priceTotal
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
            if(productStatus.productDetail != null)
            {
                if(productStatus.productDetail!!.sizeColor?.isNotEmpty() == true)
                {
                    productProcessBinding.txtSizeColor.visibility = View.VISIBLE
                    productProcessBinding.txtSizeColor.text = productStatus.productDetail!!.sizeColor.toString()
                }
            }
        }
    }

}