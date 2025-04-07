package com.example.marketgreenapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.ActivityProductDetailOrderSuccessBinding
import com.example.marketgreenapp.model.ProductStatus

class ProductDetailOrderSuccessActivity : AppCompatActivity() {
    private lateinit var productDetailOrderSuccessBinding: ActivityProductDetailOrderSuccessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productDetailOrderSuccessBinding =
            ActivityProductDetailOrderSuccessBinding.inflate(layoutInflater)
        setContentView(productDetailOrderSuccessBinding.root)

        getDataIntent()
        productDetailOrderSuccessBinding.imgBack.setOnClickListener {
            this.finish()
        }
    }

    private fun getDataIntent() {
        val dataIntent = intent?.extras
        if (dataIntent != null) {
            val productStatusDetail: ProductStatus? =
                dataIntent.getSerializable(ConstantKey.KEY_PRODUCT_DETAIL_ORDER_SUCCESS) as ProductStatus?
            if (productStatusDetail != null) {
                bindDataProductStatusToView(productStatusDetail)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindDataProductStatusToView(productStatusDetail: ProductStatus) {
        productDetailOrderSuccessBinding.txtDataCodeTransaction.text =
            productStatusDetail.productStatusId.toString()
        if (productStatusDetail.productDetail != null) {
            productDetailOrderSuccessBinding.txtDataDateTransaction.text =
                productStatusDetail.productDetail!!.dateOrder.toString()
            productDetailOrderSuccessBinding.txtDataPriceSecondary.text =
                productStatusDetail.productDetail!!.priceProduct.toString()
            productDetailOrderSuccessBinding.txtDataPriceTransaction.text =
                productStatusDetail.productDetail!!.priceTransaction.toString()
            productDetailOrderSuccessBinding.txtDataPriceVoucher.text =
                productStatusDetail.productDetail!!.priceVoucher.toString()
            productDetailOrderSuccessBinding.txtDataTotalPrice.text =
                productStatusDetail.productDetail!!.priceTotal.toString()
        }
        productDetailOrderSuccessBinding.txtPrice.text =
            productStatusDetail.productDetail!!.priceProduct.toString()
        productDetailOrderSuccessBinding.txtDataQuantityBuy.text =
            productStatusDetail.quantityBuy.toString()
        productDetailOrderSuccessBinding.txtDataMethodPayment.text =
            productStatusDetail.methodPayment.toString()

        val deliveryAddressUserOrder = productStatusDetail.deliveryAddress
        if (deliveryAddressUserOrder != null) {
            productDetailOrderSuccessBinding.txtDataName.text =
                deliveryAddressUserOrder.name_receive.toString()
            productDetailOrderSuccessBinding.txtDataAddress.text =
                deliveryAddressUserOrder.address.toString()
            productDetailOrderSuccessBinding.txtDataPhone.text =
                deliveryAddressUserOrder.phone.toString()
        }

        GlideImageURL.loadImageURL(
            productStatusDetail.imagePrimary,
            productDetailOrderSuccessBinding.imgProduct
        )
        productDetailOrderSuccessBinding.txtTitleProduct.text = productStatusDetail.titleProduct
        productDetailOrderSuccessBinding.txtNumberProduct.text =
            "x${productStatusDetail.quantityBuy}"
    }
}