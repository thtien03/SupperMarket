package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.R
import com.example.marketgreenapp.databinding.LayoutItemSizeProductBinding

class SizeProductAdapter(
    private val mListVariation: List<String?>,
    private var selectedSize: String?,
    private val onSizeClick: (String) -> Unit
) :
    RecyclerView.Adapter<SizeProductAdapter.SizeProductViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeProductViewHolder {
        val sizeProductBinding:
                LayoutItemSizeProductBinding = LayoutItemSizeProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SizeProductViewHolder(sizeProductBinding)
    }

    override fun getItemCount(): Int {
        return mListVariation.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: SizeProductViewHolder, position: Int) {
        val currentSize = mListVariation[position]
        if (currentSize != null) {
            holder.bindSize(currentSize)
            //holder.sizeProductBinding!!.root.isEnabled = currentSize != selectedSize
            /*holder.sizeProductBinding.root.setOnClickListener {
                onSizeClick(currentSize)
            }*/
            if (currentSize == selectedSize) {
                holder.sizeProductBinding!!.layoutItemSize.setBackgroundResource(R.drawable.bg_background_variation_selected)
                //holder.sizeProductBinding.txtSize.setBackgroundColor(R.drawable.bg_background_variation_selected)
                holder.sizeProductBinding.txtSize.setTextColor(Color.WHITE)
            } else {
                holder.sizeProductBinding!!.layoutItemSize.setBackgroundResource(R.drawable.bg_background_variation_unselected)
                //holder.sizeProductBinding.txtSize.setBackgroundColor(R.drawable.bg_background_variation_unselected)
                holder.sizeProductBinding.txtSize.setTextColor(Color.BLACK)
            }
            holder.itemView.setOnClickListener {
                selectedSize = currentSize
                onSizeClick(currentSize)
                notifyDataSetChanged()
            }
        }
    }

    inner class SizeProductViewHolder(val sizeProductBinding: LayoutItemSizeProductBinding?) :
        RecyclerView.ViewHolder(sizeProductBinding!!.root) {
        fun bindSize(size: String?) {
            sizeProductBinding!!.txtSize.text = size
        }
    }
}