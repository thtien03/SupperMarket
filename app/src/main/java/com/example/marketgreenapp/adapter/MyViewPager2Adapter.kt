package com.example.marketgreenapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.marketgreenapp.fragment.*

class MyViewPager2Adapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {


    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                AwaitProcessProductFragment()
            }
            1 -> {
                AwaitToPickUpProductFragment()
            }
            2 -> {
                AwaitForDeliveryProductFragment()
            }
            3 -> {
                CompleteDeliveryProductFragment()
            }
            4 -> {
                CancelDeliveryProductFragment()
            }
            else -> {
                AwaitProcessProductFragment()
            }
        }
    }

    override fun getItemCount(): Int {
        return 5
    }
}