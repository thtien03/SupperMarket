package com.example.marketgreenapp.constant

import android.app.Activity
import android.text.TextUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.marketgreenapp.R

object GlideImageURL {
    fun loadImageURL(url:String?,viewRoot: ImageView?) {
        if (viewRoot == null) return
        if (TextUtils.isEmpty(url)) {
            viewRoot.setImageResource(R.drawable.invalid_image)
            return
        }
        val context = viewRoot.context

        // Kiểm tra xem context có phải là Activity và nó có bị phá hủy không
        if (context is Activity && (context.isDestroyed || context.isFinishing)) {
            return
        }
        Glide
            .with(viewRoot.context)
            .load(url)
            .error(R.drawable.img_14)
            .dontAnimate()
            .into(viewRoot)
    }
    fun loadImageURL(url: String?, viewRoot: ImageView?, fragment: Fragment?) {
        if (viewRoot == null || fragment == null) return
        if (TextUtils.isEmpty(url)) {
            viewRoot.setImageResource(R.drawable.invalid_image)
            return
        }

        if (!fragment.isAdded || fragment.isDetached || fragment.activity == null) {
            return
        }

        Glide
            .with(fragment)
            .load(url)
            .error(R.drawable.invalid_image)
            .dontAnimate()
            .into(viewRoot)
    }
}