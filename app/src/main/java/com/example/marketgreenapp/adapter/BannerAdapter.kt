package com.example.marketgreenapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemBannerBinding
import com.example.marketgreenapp.listener.IOnClickItemBanner
import com.example.marketgreenapp.model.Banner

class BannerAdapter(
    private val mListBanners: MutableList<Banner>?,
    private val iOnClickItemBanner: IOnClickItemBanner
) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val bannerBinding =
            LayoutItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(bannerBinding)
    }

    override fun getItemCount(): Int {
        return mListBanners?.size ?: 0
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val bannerCurrent = mListBanners?.get(position)
        if(bannerCurrent != null)
        {
            holder.bindData(bannerCurrent)
            holder.bannerBinding?.layoutBanner?.setOnClickListener {
                iOnClickItemBanner.onClickItemBanner(bannerCurrent)
            }
        }
    }

    inner class BannerViewHolder(val bannerBinding: LayoutItemBannerBinding?) :
        RecyclerView.ViewHolder(bannerBinding!!.root) {
        fun bindData(banner: Banner) {
            GlideImageURL.loadImageURL(banner.imageUrl, bannerBinding?.imgBannerUser)
        }
    }

}