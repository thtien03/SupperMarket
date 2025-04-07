package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.ProductShopAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityShopBinding
import com.example.marketgreenapp.fragment.ProductPaymentBottomSheetDialog
import com.example.marketgreenapp.listener.IOnClickProductShop
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.model.ProductChooseList
import com.example.marketgreenapp.model.ProductOrder
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class ShopActivity : AppCompatActivity() {
    private lateinit var shopBinding: ActivityShopBinding
    private var stringBack: String? = null

    private var mListProduct: MutableList<Product>? = null
    private var mListProductFilter: MutableList<Product>? = null
    private lateinit var productShopAdapter: ProductShopAdapter
    // private lateinit var targetActivityClass: Class<out Activity>

    private var soldQuantity = 0

    private var mProduct: Product? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shopBinding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(shopBinding.root)
        solutionSearchView()
        getDataIntent()
        //targetActivityClass = MainActivity::class.java
        shopBinding.imgBackMain.setOnClickListener {
            if (stringBack != null) {
                if (stringBack.equals("home")) {
                    TransitionHelper.navigateWithTransition(
                        this@ShopActivity,
                        MainActivity::class.java,
                        shopBinding.shopRoot,
                        "transition_drawer",
                        R.anim.slide_in_left,
                        R.anim.slide_no_anim
                    )
                    this.finish()
                } else if (stringBack.equals("article")) {
                    TransitionHelper.navigateWithTransition(
                        this@ShopActivity,
                        ArticleMarketGreenActivity::class.java,
                        shopBinding.shopRoot,
                        "transition_drawer",
                        R.anim.slide_in_left,
                        R.anim.slide_no_anim
                    )
                    this.finish()
                } else {
                    this.finish()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDataIntent() {
        val bundle = intent.extras ?: return
        val product: Product? =
            bundle.getSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME) as Product?
        val strUserIdShop: String? = bundle.getString(ConstantKey.KEY_INTENT_POST_USERID, "")
        stringBack = bundle.getString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "")
        if (product != null) {
            mProduct = product
            shopBinding.txtNameShop.text = product.shopUserName

            shopBinding.lottieLoadingData.visibility = View.VISIBLE
            lifecycleScope.launch {
                getProductInformationId(product.shopUserId!!)
                shopBinding.lottieLoadingData.cancelAnimation()
                shopBinding.lottieLoadingData.visibility = View.GONE
                shopBinding.txtNumberSold.text = "đã bán ${getSoldQuantity()}"
                setDataProductToRecyclerview()
            }
        }
        if (strUserIdShop!!.isNotEmpty()) {
            shopBinding.lottieLoadingData.visibility = View.VISIBLE
            lifecycleScope.launch {
                getProductInformationId(strUserIdShop)
                shopBinding.lottieLoadingData.cancelAnimation()
                shopBinding.lottieLoadingData.visibility = View.GONE
                setDataProductToRecyclerview()
            }
        }
    }


    private fun getSoldQuantity(): Int {
        var soldQuantity = 0
        for (item in mListProduct!!) {
            soldQuantity += item.soldQuantity
        }
        return soldQuantity
    }

    private suspend fun getProductInformationId(shopUserId: String) {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[this@ShopActivity]
                    .getDataProductFromFirebaseDatabaseReference()
                    .get().await()
                if (mListProduct != null) mListProduct!!.clear()
                else mListProduct = mutableListOf()

                for (dataSnapshotChildren in dataSnapshot.children) {
                    val productChildren: Product? =
                        dataSnapshotChildren.getValue(Product::class.java)
                    productChildren?.let {
                        if (it.shopUserId == shopUserId) {
                            mListProduct!!.add(0, it)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setDataProductToRecyclerview() {
        productShopAdapter = ProductShopAdapter(mListProduct, object : IOnClickProductShop {
            override fun onClickProduct(product: Product) {
                val bundle = Bundle()
                bundle.putSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME, product)
                bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "shop")
                TransitionHelper.navigateWithTransition(
                    this@ShopActivity,
                    ProductDetailActivity::class.java,
                    shopBinding.shopRoot,
                    "transition_drawer",
                    R.anim.slide_in_right,
                    R.anim.slide_no_anim,
                    bundle
                )
            }

            override fun onClickBuyProduct(product: Product) {
                solutionPaymentProduct(product)
            }

            override fun onClickAddToCart(product: Product) {
                if (mProduct!!.variations.isNotEmpty()) {
                    if (mProduct == null) {
                        Toast.makeText(
                            this@ShopActivity,
                            "Product null", Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    val productOrder = ProductOrder(
                        mProduct!!.productId,
                        mProduct!!.imagePrimary,
                        mProduct!!.titleProduct,
                        mProduct!!.price,
                        0,
                        mProduct!!.quantity,
                        mProduct!!.shopUserId,
                        mProduct!!.shopUserName,
                        ""
                        //variation = null
                    )
                    val bottomSheetDialogFragment =
                        ProductPaymentBottomSheetDialog.newInstance(
                            productOrder,
                            mProduct!!,
                            "AddToCart",
                            object : ProductPaymentBottomSheetDialog.OnProductOrderListener {
                                override fun onProductOrder(productOrder: ProductOrder) {
                                    gotoProductPayActivity(this@ShopActivity, productOrder)
                                }

                                override fun onProductOrderAddToCart(
                                    size: String,
                                    color: String,
                                    productOrder: ProductOrder
                                ) {
                                    solutionAddToCartProduct2(size, color, productOrder)
                                }
                            }
                        )
                    bottomSheetDialogFragment.show(
                        supportFragmentManager,
                        bottomSheetDialogFragment.tag
                    )
                    bottomSheetDialogFragment.isCancelable = false
                } else {
                    solutionAddToCartProduct(product)
                }
            }

            override fun onClickUpdateProduct(product: Product, pos: Int) {
            }

            override fun onClickDeleteProduct(product: Product, position: Int) {
            }
        })
        shopBinding.rcvProductShop.setHasFixedSize(false)
        shopBinding.rcvProductShop.layoutManager = LinearLayoutManager(this)
        shopBinding.rcvProductShop.adapter = productShopAdapter
    }

    private fun solutionAddToCartProduct2(
        size: String,
        color: String,
        productOrderParam: ProductOrder
    ) {
        val currentUser = DataStoreManager.getUser()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (mProduct != null && currentUser?.uid != null) {

                    val selectedVariation = mProduct!!.variations.find {
                        it.size == size && it.color == color
                    }
                    if (selectedVariation == null || selectedVariation.quantity <= 0) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ShopActivity,
                                "Sản phẩm đã hết hàng",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                    val productRef = MarketGreenFirebaseApp[this@ShopActivity]
                        .getDataProductAddToCartFromFirebaseDatabaseReference()
                        .child(currentUser.uid!!)
                        .child("${mProduct!!.productId}_${size}_${color}")

                    val snapshot = productRef.get().await()
                    val existingProductOrder = snapshot.getValue(ProductOrder::class.java)

                    if (existingProductOrder != null) {
                        val updatedQuantity = existingProductOrder.quantityBuy + 1
                        if (updatedQuantity > selectedVariation.quantity) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@ShopActivity,
                                    "Số lượng vượt quá tồn kho",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            productRef.child("quantityBuy").setValue(updatedQuantity).await()
                        }
                    } else {
                        val productOrder = ProductOrder(
                            mProduct!!.productId,
                            mProduct!!.imagePrimary,
                            mProduct!!.titleProduct,
                            mProduct!!.price,
                            productOrderParam.quantityBuy,
                            mProduct!!.quantity,
                            mProduct!!.shopUserId,
                            mProduct!!.shopUserName,
                            productOrderParam.sizeColor
                        )
                        productRef.setValue(productOrder).await()
                    }

                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(
                            this@ShopActivity,
                            "Đã thêm sản phẩm vào giỏ hàng",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun solutionPaymentProduct(product: Product) {
        val productOrder = ProductOrder(
            product.productId,
            product.imagePrimary,
            product.titleProduct,
            product.price,
            1,
            product.quantity,
            product.shopUserId,
            product.shopUserName,
            ""
        )
        val bottomSheetDialogFragment =
            ProductPaymentBottomSheetDialog.newInstance(
                productOrder,
                product,
                "Payment",
                object : ProductPaymentBottomSheetDialog.OnProductOrderListener {
                    override fun onProductOrder(productOrder: ProductOrder) {
                        gotoProductPayActivity(this@ShopActivity, productOrder)
                    }

                    override fun onProductOrderAddToCart(
                        size: String,
                        color: String,
                        productOrder: ProductOrder
                    ) {
                    }
                }
            )
        bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag)
        bottomSheetDialogFragment.isCancelable = false
    }

    fun gotoProductPayActivity(activity: Activity, mProductOrder: ProductOrder) {
        val bundle = Bundle()
        val mListProductOrder: MutableList<ProductOrder> = mutableListOf()
        mListProductOrder.add(mProductOrder)
        val mListProductChooseList = ProductChooseList(mListProductOrder)

        val totalAmount = (mProductOrder.price!!.toLong() * mProductOrder.quantityBuy)
        bundle.putLong(ConstantKey.KEY_PRODUCT_TOTAL_PRICE, totalAmount)
        bundle.putSerializable(ConstantKey.KEY_PRODUCT_CHOOSE_LIST, mListProductChooseList)
        TransitionHelper.navigateWithTransition(
            activity, PaymentOrderActivity::class.java,
            shopBinding.shopRoot, "transition_drawer",
            R.anim.slide_in_right,
            R.anim.slide_out_right,
            bundle
        )
    }

    private fun solutionAddToCartProduct(product: Product) {
        val currentUser = DataStoreManager.getUser()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (currentUser?.uid != null) {
                    val productRef = MarketGreenFirebaseApp[this@ShopActivity]
                        .getDataProductAddToCartFromFirebaseDatabaseReference()
                        .child(currentUser.uid!!)
                        .child(product.productId.toString())
                    val snapshot = productRef.get().await()
                    val existingProductOrder = snapshot.getValue(ProductOrder::class.java)
                    if (existingProductOrder != null) {
                        val updatedQuantity = existingProductOrder.quantityBuy + 1
                        productRef.child("quantityBuy").setValue(updatedQuantity).await()
                    } else {
                        val productOrder = ProductOrder(
                            product.productId,
                            product.imagePrimary,
                            product.titleProduct,
                            product.price,
                            1,
                            product.quantity,
                            product.shopUserId,
                            product.shopUserName,
                            ""
                        )
                        productRef.setValue(productOrder).await()
                    }

                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(
                            this@ShopActivity,
                            "Đã thêm sản phẩm vào giỏ hàng",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun solutionSearchView() {
        shopBinding.searchInformation.setOnQueryTextListener(object :
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
}