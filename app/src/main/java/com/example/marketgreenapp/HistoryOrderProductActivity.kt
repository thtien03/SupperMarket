package com.example.marketgreenapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.HistoryOrderProductAdapter
import com.example.marketgreenapp.databinding.ActivityHistoryOrderProductBinding
import com.example.marketgreenapp.model.HistoryOrderUser
import com.example.marketgreenapp.model.User
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class HistoryOrderProductActivity : AppCompatActivity() {
    private lateinit var historyOrderBinding: ActivityHistoryOrderProductBinding
    private var mListHistoryOrderUser: MutableList<HistoryOrderUser>? = null
    private var mListHistoryOrderUserFilter: MutableList<HistoryOrderUser>? = null
    private lateinit var historyOrProductAdapter: HistoryOrderProductAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyOrderBinding = ActivityHistoryOrderProductBinding.inflate(layoutInflater)
        setContentView(historyOrderBinding.root)

        historyOrderBinding.lottieLoadingData.visibility = View.VISIBLE
        lifecycleScope.launch {
            val currentDataStoreManager = DataStoreManager.getUser()
            if (currentDataStoreManager != null) {

                if (currentDataStoreManager.role.equals("user")) {
                    getDataHistoryOrderProduct(currentDataStoreManager)
                    historyOrderBinding.txtTitleHeader.text = "Chi tiết đơn hàng"
                }
                historyOrderBinding.lottieLoadingData.cancelAnimation()
                historyOrderBinding.lottieLoadingData.visibility = View.GONE
                setDataToRecyclerView()
            }
        }
        solutionSearchView()
        historyOrderBinding.imgBack.setOnClickListener {
            this.finish()
        }
    }

    private suspend fun getDataHistoryOrderProduct(userDataStore: User?) {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[this@HistoryOrderProductActivity]
                    .getDataHistoryFromFirebaseDatabaseReference()
                    .get().await()
                if (mListHistoryOrderUser != null) mListHistoryOrderUser!!.clear()
                else mListHistoryOrderUser = mutableListOf()

                for (snapshotChildren in dataSnapshot.children) {
                    val historyChildren: HistoryOrderUser? =
                        snapshotChildren.getValue(HistoryOrderUser::class.java)
                    historyChildren?.let {
                        if (it.nameUserId == userDataStore!!.uid) {
                            mListHistoryOrderUser!!.add(it)
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setDataToRecyclerView() {
        historyOrProductAdapter =
            HistoryOrderProductAdapter(mListHistoryOrderUser, this@HistoryOrderProductActivity)
        historyOrderBinding.rcvHistoryOrder.setHasFixedSize(false)
        historyOrderBinding.rcvHistoryOrder.layoutManager = LinearLayoutManager(this)
        historyOrderBinding.rcvHistoryOrder.adapter = historyOrProductAdapter
    }

    private fun solutionSearchView() {
        historyOrderBinding.searchInformation.setOnQueryTextListener(object :
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
        mListHistoryOrderUserFilter = mutableListOf()
        if (TextUtils.isEmpty(query)) {
            mListHistoryOrderUserFilter = mListHistoryOrderUser
            historyOrProductAdapter.setDataFilter(mListHistoryOrderUser)
        } else {
            mListHistoryOrderUserFilter = mutableListOf()
            val listFilter = mutableListOf<HistoryOrderUser>()
            for (item in mListHistoryOrderUser!!) {
                if (item.productOrderId.toString()
                        .trim()
                        .contains(
                            query.toString().trim()
                        )
                )
                    listFilter.add(item)
            }
            mListHistoryOrderUserFilter = listFilter
            if (mListHistoryOrderUserFilter?.isEmpty() == false) {
                historyOrProductAdapter.setDataFilter(mListHistoryOrderUserFilter)
            }
        }
    }
}