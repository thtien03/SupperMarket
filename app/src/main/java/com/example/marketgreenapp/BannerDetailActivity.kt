package com.example.marketgreenapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.ActivityBannerDetailBinding
import com.example.marketgreenapp.model.Banner

class BannerDetailActivity : AppCompatActivity() {
    private var bannerDetailBinding:ActivityBannerDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(bannerDetailBinding == null)
            bannerDetailBinding = ActivityBannerDetailBinding.inflate(layoutInflater)
        setContentView(bannerDetailBinding!!.root)

        getDataIntent()

        bannerDetailBinding?.imgBack?.setOnClickListener {
            this.finish()
        }
    }
    private fun getDataIntent()
    {
        val bundle = intent.extras ?: return
        val banner: Banner? =
            bundle.getSerializable(ConstantKey.KEY_INTENT_BANNER) as Banner?
        if (banner != null) {
           setDataToView(banner)
        }
    }
    private fun setDataToView(banner: Banner)
    {
        bannerDetailBinding?.txtTitleBanner?.text = banner.title
        bannerDetailBinding?.txtContent?.text = banner.description
        GlideImageURL.loadImageURL(banner.imageUrl,bannerDetailBinding?.imgPromotion)
    }
}