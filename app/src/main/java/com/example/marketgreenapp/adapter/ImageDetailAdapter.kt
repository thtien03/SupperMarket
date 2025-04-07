package com.example.marketgreenapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemImageDetailBinding

class ImageDetailAdapter(private val mListImageDetail: MutableList<String>?) :
    RecyclerView.Adapter<ImageDetailAdapter.ImageDetailViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageDetailViewHolder {
        val imageBinding =
            LayoutItemImageDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageDetailViewHolder(imageBinding)
    }

    override fun getItemCount(): Int {
        return mListImageDetail?.size ?: 0
    }

    override fun onBindViewHolder(holder: ImageDetailViewHolder, position: Int) {
        val currentImage = mListImageDetail?.get(position)
        if (currentImage != null) {
            holder.loadImage(currentImage)
        }
    }

    inner class ImageDetailViewHolder(private val imageDetailBinding: LayoutItemImageDetailBinding?) :
        RecyclerView.ViewHolder(imageDetailBinding!!.root) {
        fun loadImage(string: String) {
            GlideImageURL.loadImageURL(string, imageDetailBinding!!.imgDetail)
        }
    }

}