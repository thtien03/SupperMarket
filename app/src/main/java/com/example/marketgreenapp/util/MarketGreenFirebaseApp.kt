package com.example.marketgreenapp.util

import android.app.Application
import android.content.Context
import com.example.marketgreenapp.constant.CloudinaryManager
import com.example.marketgreenapp.references.DataStoreManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MarketGreenFirebaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DataStoreManager.init(applicationContext)
        CloudinaryManager.init(applicationContext)
        FirebaseApp.initializeApp(this)
    }
    fun getDataDeliveryUserFromFirebaseDatabaseReference() : DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/delivery_address")
    }
    fun getDataVoucherFromFirebaseDatabaseReference() : DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/vouchers")
    }
    fun getDataCodeVoucherFromFirebaseDatabaseReference() : DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/code_vouchers")
    }
    fun getDataPostArticleFromFirebaseDatabaseReference() : DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/posts")
    }

    fun getDataEvaluateFromFirebaseDatabaseReference():DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/postEvaluates")
    }
    fun getDataBannerFromFirebaseDatabaseReference():DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/banners")
    }
    fun getDataProductFromFirebaseDatabaseReference():DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/products")
    }
    fun getDataProductAddToCartFromFirebaseDatabaseReference():DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/orders")
    }
    fun getDataProductOrderFromFirebaseDatabaseReference():DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/productOrders")
    }
    fun getDataHistoryFromFirebaseDatabaseReference():DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/historyOrders")
    }
    fun getDataEvaluateProductFromFirebaseDatabaseReference():DatabaseReference
    {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/detailsEvaluateProduct")
    }
    companion object
    {
        private const val FIREBASE_URL = "https://dacn-market-app-default-rtdb.firebaseio.com"
        operator fun get(context: Context?): MarketGreenFirebaseApp {
            return context!!.applicationContext as MarketGreenFirebaseApp
        }
    }
}