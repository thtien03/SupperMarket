package com.example.marketgreenapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.databinding.LayoutItemVariationBinding
import com.example.marketgreenapp.model.Variation

class VariationAdapter(private val mListVariations: MutableList<Variation>?,
    private val iOnClickVariation: IOnClickVariation) :
    RecyclerView.Adapter<VariationAdapter.VariationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariationViewHolder {
        val variationBinding: LayoutItemVariationBinding =
            LayoutItemVariationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VariationViewHolder(variationBinding)
    }

    interface IOnClickVariation{
        fun onClickVariation(variation: Variation,position: Int)
    }

    override fun getItemCount(): Int {
        return mListVariations?.size ?: 0
    }

    override fun onBindViewHolder(holder: VariationViewHolder, position: Int) {
        val variationCurrent = mListVariations?.get(position)
        if (variationCurrent != null) {
            holder.bindData(variationCurrent)
            holder.variationBinding!!.imgDeleteVariation.setOnClickListener {
                iOnClickVariation.onClickVariation(variationCurrent,position)
            }
        }
    }

    inner class VariationViewHolder( val variationBinding: LayoutItemVariationBinding?) :
        RecyclerView.ViewHolder(variationBinding!!.root) {
        fun bindData(variation: Variation) {
            variationBinding!!.txtColor.text = variation.color
            variationBinding.txtSize.text = variation.size
            variationBinding.txtQuantity.text = variation.quantity.toString()
        }
    }
}