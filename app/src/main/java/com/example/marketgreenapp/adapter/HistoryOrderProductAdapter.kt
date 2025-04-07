package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.databinding.LayoutItemHistoryOrderProductBinding
import com.example.marketgreenapp.model.HistoryOrderUser
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.model.ProductOrder

class HistoryOrderProductAdapter(
    private var mListHistoryOrderUser: MutableList<HistoryOrderUser>?,
    private var mContext: Context
) : RecyclerView.Adapter<HistoryOrderProductAdapter.HistoryOrderProductViewHolder>() {

    private lateinit var productTransactionAdapter: ProductTransactionAdapter

    @SuppressLint("NotifyDataSetChanged")
    fun setDataFilter(mListHistoryOrderProduct: MutableList<HistoryOrderUser>?)
    {
        this.mListHistoryOrderUser = mListHistoryOrderProduct
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryOrderProductViewHolder {
        val historyOrDerProductBinding: LayoutItemHistoryOrderProductBinding =
            LayoutItemHistoryOrderProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return HistoryOrderProductViewHolder(historyOrDerProductBinding)
    }

    override fun getItemCount(): Int {
        return mListHistoryOrderUser?.size ?: 0
    }

    override fun onBindViewHolder(holder: HistoryOrderProductViewHolder, position: Int) {
        val currentHistoryOrderProduct = mListHistoryOrderUser?.get(position)
        if (currentHistoryOrderProduct != null) {
            holder.bindData(currentHistoryOrderProduct)
            holder.bindDataToRecyclerView(currentHistoryOrderProduct.productOrders)
        }
    }

    inner class HistoryOrderProductViewHolder(private val historyOrderBinding: LayoutItemHistoryOrderProductBinding?) :
        RecyclerView.ViewHolder(historyOrderBinding!!.root) {

        fun bindData(historyOrderUser: HistoryOrderUser) {
            historyOrderBinding!!.txtDataCodeTransaction.text =
                historyOrderUser.productOrderId.toString()
            historyOrderBinding.txtDataDateTransaction.text = historyOrderUser.dateOrder.toString()
            historyOrderBinding.txtDataMethodPayment.text =
                historyOrderUser.methodPayment.toString()
            historyOrderBinding.txtDataPriceSecondary.text =
                historyOrderUser.priceProduct.toString()
            historyOrderBinding.txtDataPriceTransaction.text =
                historyOrderUser.priceTransaction.toString()
            historyOrderBinding.txtDataPriceVoucher.text = historyOrderUser.priceVoucher.toString()
            historyOrderBinding.txtDataTotalPrice.text = historyOrderUser.priceTotal.toString()
            if (historyOrderUser.deliveryAddress != null) {
                historyOrderBinding.txtDataName.text =
                    historyOrderUser.deliveryAddress!!.name_receive.toString()
                historyOrderBinding.txtDataPhone.text =
                    historyOrderUser.deliveryAddress!!.phone.toString()
                historyOrderBinding.txtDataAddress.text =
                    historyOrderUser.deliveryAddress!!.address.toString()
            }
        }

        fun bindDataToRecyclerView(historyOrderUser: MutableList<ProductOrder>) {
            productTransactionAdapter = ProductTransactionAdapter(historyOrderUser)
            historyOrderBinding!!.rcvProductOrder.setHasFixedSize(false)
            historyOrderBinding.rcvProductOrder.layoutManager = LinearLayoutManager(mContext)
            historyOrderBinding.rcvProductOrder.adapter = productTransactionAdapter
        }
    }
}