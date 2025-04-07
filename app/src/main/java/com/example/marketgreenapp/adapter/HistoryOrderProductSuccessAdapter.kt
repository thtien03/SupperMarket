package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemHistoryOrderProductSuccessBinding
import com.example.marketgreenapp.model.ProductDetail

class HistoryOrderProductSuccessAdapter(
    private var mListProductOrderSuccessDetail: MutableList<ProductDetail>?
) : RecyclerView.Adapter<HistoryOrderProductSuccessAdapter.HistoryOrderProductSuccessViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setDataFilter(mListProductOrderSuccess: MutableList<ProductDetail>?) {
        this.mListProductOrderSuccessDetail = mListProductOrderSuccess
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryOrderProductSuccessViewHolder {
        val historyOrDerProductBinding: LayoutItemHistoryOrderProductSuccessBinding =
            LayoutItemHistoryOrderProductSuccessBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return HistoryOrderProductSuccessViewHolder(historyOrDerProductBinding)
    }

    override fun getItemCount(): Int {
        return mListProductOrderSuccessDetail?.size ?: 0
    }

    override fun onBindViewHolder(holder: HistoryOrderProductSuccessViewHolder, position: Int) {
        val currentProductDetail = mListProductOrderSuccessDetail?.get(position)
        if (currentProductDetail != null) {
            holder.bindData(currentProductDetail)
        }
    }

    inner class HistoryOrderProductSuccessViewHolder(private val historyOrderSuccessBinding: LayoutItemHistoryOrderProductSuccessBinding?) :
        RecyclerView.ViewHolder(historyOrderSuccessBinding!!.root) {

        @SuppressLint("SetTextI18n")
        fun bindData(productDetail: ProductDetail?) {
            if (productDetail != null) {
                GlideImageURL.loadImageURL(productDetail.imageProduct,historyOrderSuccessBinding!!.imgProduct)
                historyOrderSuccessBinding.txtTitleProduct.text = productDetail.nameProduct
                historyOrderSuccessBinding.txtNumberProduct.text = "x${productDetail.quantityBuy}"
                historyOrderSuccessBinding.txtDataCodeTransaction.text =
                    productDetail.productDetaiId.toString()
                historyOrderSuccessBinding.txtDataDateTransaction.text =
                    productDetail.dateOrder.toString()
                historyOrderSuccessBinding.txtDataMethodPayment.text =
                    productDetail.methodPayment.toString()
                historyOrderSuccessBinding.txtDataPriceSecondary.text =
                    productDetail.priceProduct.toString()
                historyOrderSuccessBinding.txtDataPriceTransaction.text =
                    productDetail.priceTransaction.toString()
                historyOrderSuccessBinding.txtDataPriceVoucher.text =
                    productDetail.priceVoucher.toString()
                historyOrderSuccessBinding.txtDataTotalPrice.text =
                    productDetail.priceTotal.toString()
                if (productDetail.deliveryAddress != null) {
                    historyOrderSuccessBinding.txtDataName.text =
                        productDetail.deliveryAddress!!.name_receive.toString()
                    historyOrderSuccessBinding.txtDataPhone.text =
                        productDetail.deliveryAddress!!.phone.toString()
                    historyOrderSuccessBinding.txtDataAddress.text =
                        productDetail.deliveryAddress!!.address.toString()
                }
            }
        }
    }
}