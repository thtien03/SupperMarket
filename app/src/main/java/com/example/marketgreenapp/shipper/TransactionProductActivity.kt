package com.example.marketgreenapp.shipper

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.R
import com.example.marketgreenapp.activity_register.SignInActivity
import com.example.marketgreenapp.adapter.ProcessTransactionProductAdapter
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.databinding.ActivityTransactionProductBinding
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class TransactionProductActivity : AppCompatActivity() {
    private lateinit var transactionProcessProductBinding: ActivityTransactionProductBinding
    private lateinit var productTransactionAdapter: ProcessTransactionProductAdapter
    private var mListProductAwaitProcess: MutableList<ProductStatus>? = null
    private var mListProductAwaitProcessFilter: MutableList<ProductStatus>? = null
    private var loadingDialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionProcessProductBinding = ActivityTransactionProductBinding.inflate(layoutInflater)
        setContentView(transactionProcessProductBinding.root)

        transactionProcessProductBinding.lottieLoadingData.visibility = View.VISIBLE
        lifecycleScope.launch {
            getDataProductAwaitProcess()
            transactionProcessProductBinding.lottieLoadingData.cancelAnimation()
            transactionProcessProductBinding.lottieLoadingData.visibility = View.GONE
            setDataToRecyclerView()
        }

        transactionProcessProductBinding.navigationViewApp.itemIconTintList = null
        transactionProcessProductBinding.imgOpenNavigationApp.setOnClickListener {
            transactionProcessProductBinding.drawerLayout.openDrawer(GravityCompat.START)
        }
        eventNavigationViewApp()
        solutionSearchView()
    }

    private suspend fun getDataProductAwaitProcess() {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[this@TransactionProductActivity]
                    .getDataProductOrderFromFirebaseDatabaseReference()
                    .get().await()
                if (mListProductAwaitProcess != null) mListProductAwaitProcess!!.clear()
                else mListProductAwaitProcess = mutableListOf()
                for (snapshotChildren in dataSnapshot.children) {
                    val productStatus: ProductStatus? =
                        snapshotChildren.getValue(ProductStatus::class.java)
                    productStatus?.let {
                        if (it.orderStatus.equals(OrderStatus.IN_TRANSIT)) {
                            mListProductAwaitProcess!!.add(it)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateStatus() {
        if (mListProductAwaitProcess!!.isEmpty()) {
            transactionProcessProductBinding.rcvProcessProduct.visibility = View.GONE
            transactionProcessProductBinding.constraintLayoutNoProduct.visibility = View.VISIBLE
        } else {
            transactionProcessProductBinding.rcvProcessProduct.visibility = View.VISIBLE
            transactionProcessProductBinding.constraintLayoutNoProduct.visibility = View.GONE
        }
    }

    private fun setDataToRecyclerView() {
        updateStatus()
        productTransactionAdapter = ProcessTransactionProductAdapter(mListProductAwaitProcess,
            object : ProcessTransactionProductAdapter.IOnClickProcessProduct {
                override fun onClickProcessSuccess(product: ProductStatus, position: Int) {
                    solutionUpdateProductStatus(product, position)
                    updateStatus()
                }

                override fun onClickProcessFail(product: ProductStatus, position: Int) {
                    solutionUpdateProductStatusTransactionFail(product, position)
                    updateStatus()
                }
            })
        transactionProcessProductBinding.rcvProcessProduct.setHasFixedSize(false)
        transactionProcessProductBinding.rcvProcessProduct.layoutManager = LinearLayoutManager(this)
        transactionProcessProductBinding.rcvProcessProduct.adapter = productTransactionAdapter
    }

    private fun solutionSearchView() {
        transactionProcessProductBinding.searchInformation.setOnQueryTextListener(object :
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
        mListProductAwaitProcessFilter = mutableListOf()
        if (TextUtils.isEmpty(query)) {
            mListProductAwaitProcessFilter = mListProductAwaitProcess
            productTransactionAdapter.setDataFilter(mListProductAwaitProcess)
        } else {
            mListProductAwaitProcessFilter = mutableListOf()
            val listFilter = mutableListOf<ProductStatus>()
            for (item in mListProductAwaitProcess!!) {
                if (FunctionGlobal.getTextSearch(item.productStatusId.toString())
                        .lowercase(Locale.getDefault())
                        .trim()
                        .contains(
                            FunctionGlobal.getTextSearch(query.toString())
                                .lowercase(Locale.getDefault()).trim()
                        )
                )
                    listFilter.add(item)
            }
            mListProductAwaitProcessFilter = listFilter
            if (mListProductAwaitProcessFilter?.isEmpty() == false) {
                productTransactionAdapter.setDataFilter(mListProductAwaitProcessFilter)
            }
        }
    }

    private fun eventNavigationViewApp() {
        transactionProcessProductBinding.navigationViewApp.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    showDialog(true)
                    lifecycleScope.launch(Dispatchers.IO) {
                        FirebaseAuth.getInstance().signOut()
                        withContext(Dispatchers.Main)
                        {
                            DataStoreManager.setUser(null)
                            startActivity(
                                Intent(
                                    this@TransactionProductActivity,
                                    SignInActivity::class.java
                                )
                            )
                            finishAffinity()
                            showDialog(false)
                        }
                    }
                }
            }
            transactionProcessProductBinding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun solutionUpdateProductStatus(productStatus: ProductStatus, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (productStatus.orderStatus.equals(OrderStatus.IN_TRANSIT)) {
                    val productOrderRef = MarketGreenFirebaseApp[this@TransactionProductActivity]
                        .getDataProductOrderFromFirebaseDatabaseReference()
                        .child(productStatus.productStatusId.toString())
                    productOrderRef.child("orderStatus").setValue(OrderStatus.COMPLETED)
                        .await()
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(
                            this@TransactionProductActivity,
                            "Bạn đã giao đơn hàng thành công", Toast.LENGTH_SHORT
                        ).show()
                        if (mListProductAwaitProcess!!.isNotEmpty()) {
                            mListProductAwaitProcess!!.remove(productStatus)
                            productTransactionAdapter.notifyItemRemoved(position)
                            productTransactionAdapter.notifyItemRangeRemoved(
                                position,
                                mListProductAwaitProcess!!.size
                            )
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun solutionUpdateProductStatusTransactionFail(
        product: ProductStatus,
        position: Int
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val productOrderRef = MarketGreenFirebaseApp[this@TransactionProductActivity]
                    .getDataProductOrderFromFirebaseDatabaseReference()
                    .child(product.productStatusId.toString())
                productOrderRef.child("orderStatus")
                    .setValue(OrderStatus.TRANSACTION_PRODUCT_FAIL)
                    .await()

                val quantityOrdered = product.quantityBuy
                val shopProductRef = MarketGreenFirebaseApp[this@TransactionProductActivity]
                    .getDataProductFromFirebaseDatabaseReference()
                    .child(product.productId.toString())

                val productSnapshot = shopProductRef.get().await()

                val currentSold =
                    productSnapshot.child("soldQuantity").getValue(Int::class.java) ?: 0
                val updatedSold = currentSold - quantityOrdered

                if (!product.productDetail!!.sizeColor.isNullOrEmpty()) {
                    val input = product.productDetail!!.sizeColor.toString()
                    val parts = input.split(",").map { it.trim() }
                    val size = parts[0].substringAfter("size :").trim()
                    val color = parts[1].substringAfter("color :").trim()

                    val variationSnapshot = productSnapshot.child("variations").children

                    val matchedVariation = variationSnapshot.firstOrNull { snapshot ->
                        snapshot.child("size").value.toString() == size &&
                                snapshot.child("color").value.toString() == color
                    }
                    if (matchedVariation != null) {
                        val variationKey = matchedVariation.key
                        val currentVariationStock =
                            matchedVariation.child("quantity").getValue(Int::class.java) ?: 0
                        val updateVariationStock = currentVariationStock + quantityOrdered

                        shopProductRef.child("variations").child(variationKey!!)
                            .child("quantity").setValue(updateVariationStock).await()
                    }
                } else {
                    val currentStock =
                        productSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    val updatedStock = currentStock + quantityOrdered
                    shopProductRef.child("quantity").setValue(updatedStock).await()
                }
                shopProductRef.child("soldQuantity").setValue(updatedSold).await()


                withContext(Dispatchers.Main)
                {
                    Toast.makeText(
                        this@TransactionProductActivity,
                        "Đơn hàng giao không thành công", Toast.LENGTH_SHORT
                    ).show()
                    if (mListProductAwaitProcess!!.isNotEmpty()) {
                        mListProductAwaitProcess!!.remove(product)
                        productTransactionAdapter.notifyItemRemoved(position)
                        productTransactionAdapter.notifyItemRangeRemoved(
                            position,
                            mListProductAwaitProcess!!.size
                        )
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showDialog(isTrue: Boolean) {

        if (loadingDialog == null) {
            loadingDialog = Dialog(this)
            loadingDialog!!.setContentView(R.layout.dialog_loading)
            loadingDialog!!.setCancelable(false)
        }

        if (isTrue) {
            loadingDialog!!.show()
        } else {
            loadingDialog!!.dismiss()
        }
    }


    override fun onPause() {
        super.onPause()
        loadingDialog?.dismiss()
    }


}