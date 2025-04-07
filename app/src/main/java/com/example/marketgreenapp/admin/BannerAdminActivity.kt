package com.example.marketgreenapp.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.R
import com.example.marketgreenapp.adapter.BannerAdminAdapter
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityBannerAdminBinding
import com.example.marketgreenapp.model.Banner
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BannerAdminActivity : AppCompatActivity() {
    private var bannerAdminBinding: ActivityBannerAdminBinding? = null
    private var mListBanner:MutableList<Banner>? = null
    private lateinit var bannerAdminAdapter : BannerAdminAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (bannerAdminBinding == null)
            bannerAdminBinding = ActivityBannerAdminBinding.inflate(layoutInflater)
        setContentView(bannerAdminBinding!!.root)

        getDataBannerFirebase()
        bannerAdminBinding?.btnAddBanner?.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                this@BannerAdminActivity,
                AddOrUpdateBannerActivity::class.java,
                bannerAdminBinding!!.layoutVoucherAdminRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
        }
    }

    private fun getDataBannerFirebase() {
        try {
            MarketGreenFirebaseApp[this@BannerAdminActivity].getDataBannerFromFirebaseDatabaseReference()
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (mListBanner == null) mListBanner = mutableListOf()
                        else mListBanner?.clear()
                        for (snapshotChildren in snapshot.children) {
                            val voucherChildren: Banner? =
                                snapshotChildren.getValue(Banner::class.java)
                            voucherChildren?.let {
                                mListBanner!!.add(0, it)
                            }
                        }
                        setDataBannerToRecyclerView()
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setDataBannerToRecyclerView() {
        bannerAdminAdapter =
            BannerAdminAdapter(mListBanners = mListBanner)
        bannerAdminBinding?.rcvBanner?.setHasFixedSize(false)
        bannerAdminBinding?.rcvBanner?.layoutManager = LinearLayoutManager(this)
        bannerAdminBinding?.rcvBanner?.adapter = bannerAdminAdapter
    }
}