package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.HistoryOrderProductSuccessAdapter
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.databinding.ActivityHistoryProductOrderSuccessBinding
import com.example.marketgreenapp.model.ProductDetail
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.model.User
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HistoryProductOrderSuccessActivity : AppCompatActivity() {
    private lateinit var historyOrderProductSuccessBinding: ActivityHistoryProductOrderSuccessBinding

    private var mListHistoryOrderSuccess: MutableList<ProductDetail>? = null
    private var mListHistoryOrderSuccessFilter: MutableList<ProductDetail>? = null
    private lateinit var historyOrProductSuccessAdapter: HistoryOrderProductSuccessAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyOrderProductSuccessBinding =
            ActivityHistoryProductOrderSuccessBinding.inflate(layoutInflater)
        setContentView(historyOrderProductSuccessBinding.root)

        historyOrderProductSuccessBinding.lottieLoadingData.visibility = View.VISIBLE
        lifecycleScope.launch {
            val currentDataStoreManager = DataStoreManager.getUser()
            if (currentDataStoreManager != null) {

                if (currentDataStoreManager.role.equals("user")) {
                    getDataHistoryOrderSuccessProduct(currentDataStoreManager)
                    historyOrderProductSuccessBinding.txtTileHeader.text = "Lịch sử đặt hàng"
                } else if (currentDataStoreManager.role.equals("shop")) {
                    getDataHistoryOrderSuccessProduct(currentDataStoreManager)
                    historyOrderProductSuccessBinding.txtTileHeader.text =
                        "Lịch sử đặt hàng của người dùng"
                }
                historyOrderProductSuccessBinding.lottieLoadingData.cancelAnimation()
                historyOrderProductSuccessBinding.lottieLoadingData.visibility = View.GONE
                setDataToRecyclerView()
            }
        }
        historyOrderProductSuccessBinding.imgBack.setOnClickListener {
            this.finish()
        }
        solutionSearchView()
    }

    private suspend fun getDataHistoryOrderSuccessProduct(userDataStore: User?) {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[this@HistoryProductOrderSuccessActivity]
                    .getDataProductOrderFromFirebaseDatabaseReference()
                    .get().await()
                if (mListHistoryOrderSuccess != null) mListHistoryOrderSuccess!!.clear()
                else mListHistoryOrderSuccess = mutableListOf()
                if (userDataStore!!.role.equals("user")) {
                    for (snapshotChildren in dataSnapshot.children) {
                        val productStatus: ProductStatus? =
                            snapshotChildren.getValue(ProductStatus::class.java)
                        productStatus?.let {
                            if (it.nameUserId == userDataStore.uid && it.orderStatus.equals(
                                    OrderStatus.COMPLETED
                                )
                            ) {
                                mListHistoryOrderSuccess!!.add(it.productDetail!!)
                            }
                        }
                    }
                }
                if (userDataStore.role.equals("shop")) {
                    for (snapshotChildren in dataSnapshot.children) {
                        val productStatus: ProductStatus? =
                            snapshotChildren.getValue(ProductStatus::class.java)
                        productStatus?.let {
                            if (it.orderStatus.equals(OrderStatus.COMPLETED) && it.shopUserId == userDataStore.uid) {
                                mListHistoryOrderSuccess!!.add(it.productDetail!!)
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
        historyOrProductSuccessAdapter =
            HistoryOrderProductSuccessAdapter(mListHistoryOrderSuccess)
        historyOrderProductSuccessBinding.rcvHistoryOrderSuccess.setHasFixedSize(false)
        historyOrderProductSuccessBinding.rcvHistoryOrderSuccess.layoutManager =
            LinearLayoutManager(this)
        historyOrderProductSuccessBinding.rcvHistoryOrderSuccess.adapter =
            historyOrProductSuccessAdapter
    }

    private fun solutionSearchView() {
        historyOrderProductSuccessBinding.searchInformation.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText)
                return true
            }

        })
    }

    private fun filterData(query: String?) {
        mListHistoryOrderSuccessFilter = mutableListOf()
        if (TextUtils.isEmpty(query)) {
            mListHistoryOrderSuccessFilter = mListHistoryOrderSuccess
            historyOrProductSuccessAdapter.setDataFilter(mListHistoryOrderSuccess)
        } else {
            mListHistoryOrderSuccessFilter = mutableListOf()
            val listFilter = mutableListOf<ProductDetail>()
            for (item in mListHistoryOrderSuccess!!) {
                if (item.productDetaiId.toString()
                        .trim()
                        .contains(
                            query.toString().trim()
                        )
                )
                    listFilter.add(item)
            }
            mListHistoryOrderSuccessFilter = listFilter
            if (mListHistoryOrderSuccessFilter?.isEmpty() == false) {
                historyOrProductSuccessAdapter.setDataFilter(mListHistoryOrderSuccessFilter)
            }
        }
    }
}