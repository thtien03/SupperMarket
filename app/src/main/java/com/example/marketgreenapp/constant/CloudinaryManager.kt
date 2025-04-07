package com.example.marketgreenapp.constant

import android.content.Context
import com.cloudinary.android.MediaManager

class CloudinaryManager {
    companion object
    {
        fun init(context:Context)
        {
            val config = mapOf(
                "cloud_name" to "dfuobcsmo",
                "api_key" to "688252153927452",
                "api_secret" to "r7WfDGio9m4IdDjFahHrf_qL9ZE"
            )
            MediaManager.init(context,config)
        }
    }
}