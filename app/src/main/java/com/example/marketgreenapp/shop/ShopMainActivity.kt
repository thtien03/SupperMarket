package com.example.marketgreenapp.shop

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.*
import com.example.marketgreenapp.activity_register.SignInActivity
import com.example.marketgreenapp.adapter.DeliveryAddressAdapter
import com.example.marketgreenapp.adapter.ProductShopAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityShopMainBinding
import com.example.marketgreenapp.listener.IOnClickProductShop
import com.example.marketgreenapp.model.AnswerHelp
import com.example.marketgreenapp.model.DeliveryAddress
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class ShopMainActivity : AppCompatActivity() {
    private var shopMainBinding: ActivityShopMainBinding? = null
    private var mListProduct: MutableList<Product>? = null
    private var mListProductFilter: MutableList<Product>? = null
    private lateinit var productShopAdapter: ProductShopAdapter
    private var loadingDialog: Dialog? = null
    private var positionUpdateProduct = -1
    private var activityResultLauncherProduct = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val action = result.data?.getStringExtra(ConstantKey.KEY_ACTION_PRODUCT)
                val product =
                    result.data!!.getSerializableExtra(ConstantKey.KEY_PRODUCT) as Product?
                product?.let {
                    when (action) {
                        ConstantKey.KEY_ADD_PRODUCT -> {
                            solutionAddProduct(it)
                        }
                        ConstantKey.KEY_UPDATE_PRODUCT -> {
                            solutionUpdateProduct(it)
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (shopMainBinding == null)
            shopMainBinding = ActivityShopMainBinding.inflate(layoutInflater)
        setContentView(shopMainBinding!!.root)

        val userDataManager = DataStoreManager.getUser()
        if (userDataManager != null) {
            shopMainBinding?.txtNameShop?.text = userDataManager.fullname
        }

        shopMainBinding!!.lottieLoadingData.visibility = View.VISIBLE
        lifecycleScope.launch {
            getListProductFB()
            setQuantityProduct()
            shopMainBinding!!.lottieLoadingData.cancelAnimation()
            shopMainBinding!!.lottieLoadingData.visibility = View.GONE
            println("kakakak==============${userDataManager!!.uid}")
            setListProductToRecyclerView()
        }
        setNavigationView()
        eventNavigationView()
        eventNavigationViewApp()
        eventBottomNavigationView()

        solutionSearchView()
    }

    private suspend fun getListProductFB() {
        return withContext(Dispatchers.IO)
        {
            try {
                val userCurrent = DataStoreManager.getUser()
                if (userCurrent?.uid != null) {
                    val dataSnapshot = MarketGreenFirebaseApp[this@ShopMainActivity]
                        .getDataProductFromFirebaseDatabaseReference()
                        .get().await()
                    if (mListProduct != null) mListProduct!!.clear()
                    else mListProduct = mutableListOf()

                    for (dataSnapshotChildren in dataSnapshot.children) {
                        val productChildren: Product? =
                            dataSnapshotChildren.getValue(Product::class.java)
                        productChildren?.let {
                            if (it.shopUserId == userCurrent.uid) {
                                mListProduct!!.add(0, it)
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setQuantityProduct() {
        var totalQuantityBuy = 0
        var totalQuantityProduct = 0
        if (mListProduct!!.isNotEmpty()) {
            for (item in mListProduct!!) {
                totalQuantityBuy += item.soldQuantity
                totalQuantityProduct += item.quantity
            }
        }
        shopMainBinding!!.txtTotalQuantityProductShop.text = totalQuantityProduct.toString()
        shopMainBinding!!.txtNumberSold.text = "đã bán $totalQuantityBuy"

    }

    private fun setListProductToRecyclerView() {
        if (mListProduct!!.isEmpty()) {
            shopMainBinding?.rcvProductShop?.visibility = View.GONE
            shopMainBinding?.constraintLayoutNoProduct?.visibility = View.VISIBLE
        } else {
            shopMainBinding?.rcvProductShop?.visibility = View.VISIBLE
            shopMainBinding?.constraintLayoutNoProduct?.visibility = View.GONE
        }
        productShopAdapter = ProductShopAdapter(mListProduct, object : IOnClickProductShop {
            override fun onClickProduct(product: Product) {
            }

            override fun onClickBuyProduct(product: Product) {
            }

            override fun onClickAddToCart(product: Product) {
            }

            override fun onClickUpdateProduct(product: Product, pos: Int) {
                val intent = Intent(this@ShopMainActivity, AddOrUpdateProductActivity::class.java)
                val pairs: Array<Pair<View, String>> = arrayOf(
                    Pair(shopMainBinding?.drawerLayout, "transition_drawer")
                )
                val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@ShopMainActivity,
                    *pairs
                )
                option.update(
                    ActivityOptionsCompat.makeCustomAnimation(
                        this@ShopMainActivity,
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                )
                intent.putExtra(ConstantKey.KEY_PRODUCT, product)
                activityResultLauncherProduct.launch(intent, option)
            }

            override fun onClickDeleteProduct(product: Product, position: Int) {
            }
        })
        shopMainBinding?.rcvProductShop?.setHasFixedSize(false)
        shopMainBinding?.rcvProductShop?.layoutManager = LinearLayoutManager(this)
        shopMainBinding?.rcvProductShop?.adapter = productShopAdapter
    }

    private fun setNavigationView() {
        shopMainBinding?.navigationViewApp?.itemIconTintList = null
        shopMainBinding?.bottomNavigationView?.background = null
        shopMainBinding?.bottomNavigationView?.itemIconTintList = null
        shopMainBinding?.bottomNavigationView?.menu!!.getItem(2).isEnabled = false
        shopMainBinding?.bottomNavigationView?.menu!!.getItem(0).isChecked = true
    }

    private fun eventNavigationView() {
        shopMainBinding?.imgOpenNavigationApp?.setOnClickListener {
            shopMainBinding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    private fun eventNavigationViewApp() {
        shopMainBinding?.navigationViewApp?.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    showDialog(true)
                    lifecycleScope.launch(Dispatchers.IO) {
                        FirebaseAuth.getInstance().signOut()
                        withContext(Dispatchers.Main)
                        {
                            DataStoreManager.setUser(null)
                            startActivity(Intent(this@ShopMainActivity, SignInActivity::class.java))
                            finishAffinity()
                            showDialog(false)
                        }
                    }
                }
                R.id.nav_follow_order_product -> {
                    TransitionHelper.navigateWithTransition(
                        this@ShopMainActivity,
                        FollowOrderProductActivity::class.java,
                        shopMainBinding!!.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_history_order_product_success -> {
                    TransitionHelper.navigateWithTransition(
                        this@ShopMainActivity,
                        HistoryProductOrderSuccessActivity::class.java,
                        shopMainBinding!!.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_article -> {
                    TransitionHelper.navigateWithTransition(
                        this@ShopMainActivity,
                        ArticleMarketGreenActivity::class.java,
                        shopMainBinding!!.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_revenue -> {
                    TransitionHelper.navigateWithTransition(
                        this@ShopMainActivity,
                        ShopRevenueActivity::class.java,
                        shopMainBinding!!.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_product_order_transaction_fail -> {
                    TransitionHelper.navigateWithTransition(
                        this@ShopMainActivity,
                        ProductOrderCancelActivity::class.java,
                        shopMainBinding!!.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
            }
            shopMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun eventBottomNavigationView() {
        shopMainBinding?.bottomNavigationView?.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_profile -> {
                    TransitionHelper.navigateWithTransition(
                        this@ShopMainActivity,
                        ProfileShopActivity::class.java,
                        shopMainBinding!!.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_article -> {
                    TransitionHelper.navigateWithTransition(
                        this@ShopMainActivity,
                        ArticleMarketGreenActivity::class.java,
                        shopMainBinding!!.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_order -> {
                    TransitionHelper.navigateWithTransition(
                        this@ShopMainActivity,
                        FollowOrderProductActivity::class.java,
                        shopMainBinding!!.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
            }
            true
        }
        shopMainBinding?.fabAddProduct?.setOnClickListener {
            val intent = Intent(this@ShopMainActivity, AddOrUpdateProductActivity::class.java)
            val pairs: Array<Pair<View, String>> = arrayOf(
                Pair(shopMainBinding?.drawerLayout, "transition_drawer")
            )
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@ShopMainActivity,
                *pairs
            )
            option.update(
                ActivityOptionsCompat.makeCustomAnimation(
                    this@ShopMainActivity,
                    R.anim.slide_in_right,
                    R.anim.slide_no_anim
                )
            )
            activityResultLauncherProduct.launch(intent, option)
        }
    }

    private fun solutionAddProduct(product: Product) {
        if (mListProduct == null) mListProduct = mutableListOf()
        mListProduct!!.add(0, product)
        setListProductToRecyclerView()
        //productShopAdapter.notifyItemInserted(0)
    }

    private fun solutionUpdateProduct(product: Product) {
        mListProduct?.let { it ->
            val index = it.indexOfFirst { it.productId == product.productId }
            if (index != -1) {
                it[index] = product
                productShopAdapter.notifyItemChanged(index)
            }
        }
    }


    private fun solutionSearchView() {
        shopMainBinding!!.searchInformation.setOnQueryTextListener(object :
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
        mListProductFilter = mutableListOf()
        if (TextUtils.isEmpty(query)) {
            mListProductFilter = mListProduct
            productShopAdapter.setDataFilter(mListProduct)
        } else {
            mListProductFilter = mutableListOf()
            val listFilter = mutableListOf<Product>()
            for (item in mListProduct!!) {
                if (FunctionGlobal.getTextSearch(item.titleProduct).lowercase(Locale.getDefault())
                        .trim()
                        .contains(
                            FunctionGlobal.getTextSearch(query.toString())
                                .lowercase(Locale.getDefault()).trim()
                        )
                )
                    listFilter.add(item)
            }
            mListProductFilter = listFilter
            if (mListProductFilter?.isEmpty() == false) {
                productShopAdapter.setDataFilter(mListProductFilter)
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