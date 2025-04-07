package com.example.marketgreenapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemImageSecondaryBinding
import com.example.marketgreenapp.listener.IOnClickItemImageSecondary

class SecondaryAdapter(
    private val mListImageSecond: MutableList<String>?,
    private val iOnClickItemImageSecondary: IOnClickItemImageSecondary
) :
    RecyclerView.Adapter<SecondaryAdapter.SecondaryViewHolder>() {

    inner class SecondaryViewHolder(val imageSecondBinding: LayoutItemImageSecondaryBinding?) :
        RecyclerView.ViewHolder(imageSecondBinding!!.root) {
        fun bindImage(string: String) {
            GlideImageURL.loadImageURL(string,imageSecondBinding!!.imgSecondary)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecondaryViewHolder {
        val imageBinding: LayoutItemImageSecondaryBinding = LayoutItemImageSecondaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SecondaryViewHolder((imageBinding))
    }

    override fun getItemCount(): Int {
        return mListImageSecond?.size ?: 0
    }

    override fun onBindViewHolder(holder: SecondaryViewHolder, position: Int) {
        val currentImage = mListImageSecond?.get(position)
        if (currentImage != null) {
            holder.bindImage(currentImage)
            holder.imageSecondBinding!!.imgRemoveImgPrimary.setOnClickListener {
                iOnClickItemImageSecondary.onClickItemImage(currentImage,position)
            }
        }
    }
}