package com.example.marketgreenapp.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.marketgreenapp.R
import com.example.marketgreenapp.activity_register.SignInActivity
import com.example.marketgreenapp.adapter.BannerAdminAdapter
import com.example.marketgreenapp.databinding.ActivityAdminMainBinding
import com.example.marketgreenapp.references.DataStoreManager
import com.google.firebase.auth.FirebaseAuth

class AdminMainActivity : AppCompatActivity() {
    private var adminMainBinding:ActivityAdminMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adminMainBinding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(adminMainBinding!!.root)

        adminMainBinding?.btnLogOut?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            DataStoreManager.setUser(null)
            startActivity(Intent(this, SignInActivity::class.java))
            this.finishAffinity()
        }
        adminMainBinding?.btnVoucher?.setOnClickListener {
            startActivity(Intent(this, VoucherAdminActivity::class.java))
        }
        adminMainBinding?.btnCodeVoucher?.setOnClickListener {
            startActivity(Intent(this, CodeVoucherActivity::class.java))
        }
        adminMainBinding?.btnBanner?.setOnClickListener {
            startActivity(Intent(this, BannerAdminActivity::class.java))
        }
    }
}