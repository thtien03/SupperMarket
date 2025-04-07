package com.example.marketgreenapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.adapter.ProductAwaitProcessAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityProductOrderCancelBinding
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.model.User
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductOrderCancelActivity : AppCompatActivity() {
    private lateinit var productOrderCancelBinding: ActivityProductOrderCancelBinding
    private lateinit var productAwaitProcessAdapter: ProductAwaitProcessAdapter
    private var mListProductStatus: MutableList<ProductStatus>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productOrderCancelBinding = ActivityProductOrderCancelBinding.inflate(layoutInflater)
        setContentView(productOrderCancelBinding.root)

        lifecycleScope.launch(Dispatchers.Main) {
            val currentDataStoreManager = DataStoreManager.getUser()
            if (currentDataStoreManager != null) {
                getDataProductOrderCancel(currentDataStoreManager)
                setDataToRecyclerView() // Cập nhật RecyclerView
            }
        }
        productOrderCancelBinding.imgBack.setOnClickListener {
            this.finish()
        }
    }

    private fun setDataToRecyclerView() {
        productAwaitProcessAdapter = ProductAwaitProcessAdapter(mListProductStatus,
            object : ProductAwaitProcessAdapter.IOnClickDeliveryAddress {
                override fun onClickDelivery(productStatus: ProductStatus, position: Int) {
                }

                override fun onClickUpdateStatusOrderProduct(
                    productStatus: ProductStatus,
                    position: Int
                ) {
                }
            },this@ProductOrderCancelActivity, object : ProductAwaitProcessAdapter.IOnClickProductStatusDetail {
                override fun onClickProductDetail(product: ProductStatus, position: Int) {
                    val bundle = Bundle()
                    bundle.putSerializable(ConstantKey.KEY_PRODUCT_DETAIL_ORDER_SUCCESS, product)
                    TransitionHelper.navigateWithTransition(
                        this@ProductOrderCancelActivity,
                        ProductDetailOrderSuccessActivity::class.java,
                        productOrderCancelBinding.layoutRoot,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim,
                        bundle
                    )
                }

                override fun onClickBuyAgainProduct(product: ProductStatus) {
                }

                override fun onClickEvaluateProduct(product: ProductStatus) {
                }
            }, object : ProductAwaitProcessAdapter.IOnClickCancelProductOrder {
                override fun onClickCancelProductOrder(product: ProductStatus, position: Int) {
                }

            })
        productOrderCancelBinding.rcvProductAwaitForDelivery.setHasFixedSize(false)
        productOrderCancelBinding.rcvProductAwaitForDelivery.layoutManager =
            LinearLayoutManager(this@ProductOrderCancelActivity)
        val dividerItemDecoration =
            DividerItemDecoration(this@ProductOrderCancelActivity, RecyclerView.VERTICAL)
        productOrderCancelBinding.rcvProductAwaitForDelivery.addItemDecoration(
            dividerItemDecoration
        )
        productOrderCancelBinding.rcvProductAwaitForDelivery.adapter =
            productAwaitProcessAdapter
    }

    private suspend fun getDataProductOrderCancel(currentDataStoreManager: User) {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[this@ProductOrderCancelActivity]
                    .getDataProductOrderFromFirebaseDatabaseReference()
                    .get().await()
                if (mListProductStatus != null) mListProductStatus!!.clear()
                else mListProductStatus = mutableListOf()

                if (currentDataStoreManager.role.equals("shop")) {
                    for (snapshotChildren in dataSnapshot.children) {
                        val productStatus: ProductStatus? =
                            snapshotChildren.getValue(ProductStatus::class.java)
                        productStatus?.let {
                            if (it.shopUserId.equals(currentDataStoreManager.uid)) {
                                if (it.orderStatus.equals(OrderStatus.TRANSACTION_PRODUCT_FAIL)) {
                                    mListProductStatus!!.add(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}