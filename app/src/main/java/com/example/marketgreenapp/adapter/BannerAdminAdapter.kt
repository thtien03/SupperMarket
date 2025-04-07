package com.example.marketgreenapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemBannerAdminBinding
import com.example.marketgreenapp.model.Banner

class BannerAdminAdapter(
    private val mListBanners: MutableList<Banner>?,
) :
    RecyclerView.Adapter<BannerAdminAdapter.BannerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val bannerBinding =
            LayoutItemBannerAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        }
    }

    inner class BannerViewHolder(val bannerBinding: LayoutItemBannerAdminBinding?) :
        RecyclerView.ViewHolder(bannerBinding!!.root) {
        fun bindData(banner: Banner) {
            bannerBinding?.txtTitleBanner?.text = banner.title
            bannerBinding?.txtContentBanner?.text = banner.description
            GlideImageURL.loadImageURL(banner.imageUrl, bannerBinding?.imgBanner)
        }
    }

}