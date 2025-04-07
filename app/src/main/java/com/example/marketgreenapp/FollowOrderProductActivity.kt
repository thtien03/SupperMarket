package com.example.marketgreenapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.marketgreenapp.adapter.MyViewPager2Adapter
import com.example.marketgreenapp.databinding.ActivityFollowOrderProductBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FollowOrderProductActivity : AppCompatActivity() {
    private lateinit var myViewPager2Adapter: MyViewPager2Adapter
     lateinit var followOrderBinding: ActivityFollowOrderProductBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        followOrderBinding = ActivityFollowOrderProductBinding.inflate(layoutInflater)
        setContentView(followOrderBinding.root)

        myViewPager2Adapter = MyViewPager2Adapter(this@FollowOrderProductActivity)
        followOrderBinding.viewPager2.adapter = myViewPager2Adapter

        TabLayoutMediator(
            followOrderBinding.tabLayout, followOrderBinding.viewPager2
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Chờ xử lý"
                }
                1 -> {
                    tab.text = "Chờ lấy hàng"
                }
                2 -> {
                    tab.text = "Chờ giao hàng"
                }
                3 -> {
                    tab.text = "Đã giao hàng"
                }
                4->
                {
                    tab.text ="Đơn hàng đã hủy"
                }
            }
        }.attach()
        followOrderBinding.imgBack.setOnClickListener {
            this.finish()
        }
    }
}