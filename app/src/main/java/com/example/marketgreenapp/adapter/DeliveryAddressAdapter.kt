package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.databinding.LayoutItemDeliveryAddressBinding
import com.example.marketgreenapp.model.DeliveryAddress

class DeliveryAddressAdapter(
    private val mListDelivery: MutableList<DeliveryAddress>?,
    private val iOnClickDelivery: IOnClickDelivery
)

    : RecyclerView.Adapter<DeliveryAddressAdapter.DeliveryAddressViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryAddressViewHolder {
        val deliveryBinding: LayoutItemDeliveryAddressBinding =
            LayoutItemDeliveryAddressBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DeliveryAddressViewHolder(deliveryBinding)
    }

    interface IOnClickDelivery {
        fun onClickDelivery(delivery: DeliveryAddress)
        fun onClickUpdateDelivery(delivery: DeliveryAddress,position: Int)
    }

    override fun getItemCount(): Int {
        return mListDelivery?.size ?: 0
    }

    override fun onBindViewHolder(holder: DeliveryAddressViewHolder, position: Int) {
        val deliveryCurrent = mListDelivery?.get(position)
        if (deliveryCurrent != null) {
            holder.bindData(deliveryCurrent)
            holder.deliveryBinding?.layoutItemDeliveryRoot?.setOnClickListener {
                iOnClickDelivery.onClickDelivery(deliveryCurrent)
            }
            holder.deliveryBinding?.btnUpdateDeliveryAddress?.setOnClickListener {
                    iOnClickDelivery.onClickUpdateDelivery(deliveryCurrent,position)
            }
            if (deliveryCurrent.isDefault)
                holder.deliveryBinding?.txtNotify?.visibility = View.VISIBLE
            else
                holder.deliveryBinding?.txtNotify?.visibility = View.GONE
        }
    }

    inner class DeliveryAddressViewHolder(val deliveryBinding: LayoutItemDeliveryAddressBinding?) :
        RecyclerView.ViewHolder(deliveryBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(delivery: DeliveryAddress) {
            deliveryBinding?.txtNameUser?.text = delivery.name_receive
            deliveryBinding?.txtPhoneNumber?.text = "(+84) ${delivery.phone}"
            deliveryBinding?.txtDeliveryAddress?.text = delivery.address
        }
    }
}