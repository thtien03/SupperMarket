package com.example.marketgreenapp.shop

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.RevenueAdapter
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.databinding.ActivityShopRevenueBinding
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.model.User
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

class ShopRevenueActivity : AppCompatActivity() {
    private lateinit var shopRevenueBinding: ActivityShopRevenueBinding
    private var mListProductOrderSuccess: MutableList<ProductStatus>? = null
    private lateinit var revenueAdapter: RevenueAdapter

    companion object {
        private var totalPrice: Float = 0f
        private var totalCount: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shopRevenueBinding = ActivityShopRevenueBinding.inflate(layoutInflater)
        setContentView(shopRevenueBinding.root)
        totalPrice = 0f
        totalCount = 0

        lifecycleScope.launch {
            val currentDataStoreManager = DataStoreManager.getUser()
            if (currentDataStoreManager != null) {
                getDataProductOrderSuccess(currentDataStoreManager)
                setQuantityAndPriceTotal()
                setDataToRecyclerView()
            }
        }
        shopRevenueBinding.imgBackMain.setOnClickListener {
            this.finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setQuantityAndPriceTotal() {
        if (mListProductOrderSuccess != null) {
            for (item in mListProductOrderSuccess!!) {
                totalCount += item.quantityBuy
                totalPrice += convertPriceToInt(item.priceTotal.toString())
            }
            shopRevenueBinding.txtTotalQuantity.text = "$totalCount số lượng"
            val formattedPrice =
                NumberFormat.getNumberInstance(Locale.GERMANY).format(totalPrice)
            shopRevenueBinding.txtTotalPrice.text = "${formattedPrice}.000VNĐ"        }
    }

    private fun convertPriceToInt(price: String): Int {
        val cleanPrice = price.replace(".", "").replace("VNĐ", "").trim()
        return (cleanPrice.toIntOrNull() ?: 0) / 1000
    }

    private suspend fun getDataProductOrderSuccess(currentDataStoreManager: User) {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[this@ShopRevenueActivity]
                    .getDataProductOrderFromFirebaseDatabaseReference()
                    .get().await()
                if (mListProductOrderSuccess != null) mListProductOrderSuccess!!.clear()
                else mListProductOrderSuccess = mutableListOf()

                for (snapshotChildren in dataSnapshot.children) {
                    val productStatus: ProductStatus? =
                        snapshotChildren.getValue(ProductStatus::class.java)
                    productStatus?.let {
                        if (it.shopUserId.equals(currentDataStoreManager.uid)) {
                            if (it.orderStatus.equals(OrderStatus.COMPLETED)) {
                                mListProductOrderSuccess!!.add(it)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setDataToRecyclerView() {
        revenueAdapter = RevenueAdapter(mListProductOrderSuccess)
        shopRevenueBinding.rcvRevenue.setHasFixedSize(false)
        shopRevenueBinding.rcvRevenue.layoutManager = LinearLayoutManager(this@ShopRevenueActivity)
        shopRevenueBinding.rcvRevenue.adapter = revenueAdapter
    }
}