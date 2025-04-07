package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.marketgreenapp.adapter.EvaluateAdapter
import com.example.marketgreenapp.adapter.ImageDetailAdapter
import com.example.marketgreenapp.adapter.ProductAdapter
import com.example.marketgreenapp.adapter.ProductOtherAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityProductDetailBinding
import com.example.marketgreenapp.fragment.ProductPaymentBottomSheetDialog
import com.example.marketgreenapp.listener.IOnClickProduct
import com.example.marketgreenapp.model.Evaluate
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.model.ProductChooseList
import com.example.marketgreenapp.model.ProductOrder
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var productDetailBinding: ActivityProductDetailBinding
    private var stringBack: String? = null
    private var mListProduct: MutableList<Product>? = null
    private var mListProductOther: MutableList<Product>? = null
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productOtherAdapter: ProductOtherAdapter
    private lateinit var imageDetailAdapter: ImageDetailAdapter
    private var mListImageDetail: MutableList<String>? = null

    private var mListEvaluates: MutableList<Evaluate>? = null
    private lateinit var evaluateAdapter: EvaluateAdapter

    private var mProduct: Product? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productDetailBinding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(productDetailBinding.root)

        getDataIntent()

        productDetailBinding.imgBack.setOnClickListener {
            if (stringBack != null) {
                if (stringBack.equals("home")) {
                    TransitionHelper.navigateWithTransition(
                        this@ProductDetailActivity,
                        MainActivity::class.java,
                        productDetailBinding.productDetailRoot,
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
        productDetailBinding.txtAccessShop.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME, mProduct)
            bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "detail")
            TransitionHelper.navigateWithTransition(
                this@ProductDetailActivity,
                ShopActivity::class.java,
                productDetailBinding.productDetailRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_no_anim,
                bundle
            )
        }
        productDetailBinding.imgShopUser.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME, mProduct)
            bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "detail")
            TransitionHelper.navigateWithTransition(
                this@ProductDetailActivity,
                ShopActivity::class.java,
                productDetailBinding.productDetailRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_no_anim,
                bundle
            )
        }
        productDetailBinding.btnAddToCart.setOnClickListener {
            if (mProduct!!.variations.isNotEmpty()) {
                if (mProduct == null) {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Product null", Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
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
                                gotoProductPayActivity(this@ProductDetailActivity, productOrder)
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
                solutionAddToCartProduct()
            }
        }
        productDetailBinding.btnPayment.setOnClickListener {
            if (mProduct == null) {
                Toast.makeText(
                    this@ProductDetailActivity,
                    "Product null", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val productOrder = ProductOrder(
                mProduct!!.productId,
                mProduct!!.imagePrimary,
                mProduct!!.titleProduct,
                mProduct!!.price,
                1,
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
                    "Payment",
                    object : ProductPaymentBottomSheetDialog.OnProductOrderListener {
                        override fun onProductOrder(productOrder: ProductOrder) {
                            gotoProductPayActivity(this@ProductDetailActivity, productOrder)
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

        productDetailBinding.constraintLayoutCart.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "detail")
            TransitionHelper.navigateWithTransition(
                this@ProductDetailActivity,
                OrderProductActivity::class.java,
                productDetailBinding.productDetailRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_no_anim,
                bundle
            )
        }
        productDetailBinding.tvDescription.setOnClickListener {
            productDetailBinding.tvDescription.background =
                resources.getDrawable(R.drawable.switch_trcks, null)
            productDetailBinding.tvReviews.background = null
            productDetailBinding.constraintLayoutDescription.visibility = View.VISIBLE
            productDetailBinding.constraintLayoutEvaluate.visibility = View.GONE
            productDetailBinding.tvDescription.setTextColor(resources.getColor(R.color.white, null))
            productDetailBinding.tvReviews.setTextColor(resources.getColor(R.color.black, null))
        }
        productDetailBinding.tvReviews.setOnClickListener {
            productDetailBinding.tvReviews.background =
                resources.getDrawable(R.drawable.switch_trcks, null)
            productDetailBinding.tvDescription.background = null
            productDetailBinding.constraintLayoutDescription.visibility = View.GONE
            productDetailBinding.constraintLayoutEvaluate.visibility = View.VISIBLE
            productDetailBinding.tvReviews.setTextColor(resources.getColor(R.color.white, null))
            productDetailBinding.tvDescription.setTextColor(resources.getColor(R.color.black, null))
        }
    }

    private fun getDataIntent() {
        val bundle = intent.extras ?: return
        val product: Product? =
            bundle.getSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME) as Product?
        stringBack = bundle.getString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "")
        if (product != null) {
            //setDataViewPager2(product.imagesDetails)
            lifecycleScope.launch {
                getProductInformationId(product.productId)
                getProductOtherShopUserId(product.shopUserId)
                getEvaluateProduct(product.productId)
                setDataToView()
                setDataProductOtherToRecyclerview()
                setDataProductOtherShopToRecyclerView()
                setDataEvaluateToRecycleView()
            }
        }
    }

    private suspend fun getEvaluateProduct(productId: Long?) {
        return withContext(Dispatchers.IO)
        {
            try {
                val snapshot =
                    MarketGreenFirebaseApp[this@ProductDetailActivity]
                        .getDataEvaluateProductFromFirebaseDatabaseReference()
                        .child(productId.toString()).child("evaluates")
                        .get().await()
                if (mListEvaluates != null) mListEvaluates!!.clear()
                else mListEvaluates = mutableListOf()
                for (snapshotChildren in snapshot.children) {
                    val evaluateChildren: Evaluate? =
                        snapshotChildren.getValue(Evaluate::class.java)
                    evaluateChildren?.let {
                        mListEvaluates!!.add(it)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setDataEvaluateToRecycleView() {
        evaluateAdapter = EvaluateAdapter(mListEvaluates)
        productDetailBinding.rcvEvaluates.setHasFixedSize(false)
        productDetailBinding.rcvEvaluates.layoutManager = LinearLayoutManager(this)
        productDetailBinding.rcvEvaluates.adapter = evaluateAdapter
    }

    private suspend fun getProductInformationId(productId: Long?) {
        return withContext(Dispatchers.IO)
        {
            try {
                val snapshot =
                    MarketGreenFirebaseApp[this@ProductDetailActivity]
                        .getDataProductFromFirebaseDatabaseReference().child(productId.toString())
                        .get().await()
                mProduct = snapshot.getValue(Product::class.java)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getProductOtherShopUserId(shopUserId: String?) {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[this@ProductDetailActivity]
                    .getDataProductFromFirebaseDatabaseReference()
                    .get().await()
                if (mListProduct != null) mListProduct!!.clear()
                else mListProduct = mutableListOf()
                if (mListProductOther != null) mListProductOther!!.clear()
                else mListProductOther = mutableListOf()

                for (dataSnapshotChildren in dataSnapshot.children) {
                    val productChildren: Product? =
                        dataSnapshotChildren.getValue(Product::class.java)
                    productChildren?.let {
                        if (it.shopUserId == shopUserId) {
                            mListProduct!!.add(0, it)
                        } else {
                            mListProductOther!!.add(0, it)
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDataToView() {
        if (mListImageDetail == null) mListImageDetail = mutableListOf()
        if (mProduct != null) {
            productDetailBinding.txtTitleProduct.text = mProduct!!.titleProduct
            productDetailBinding.txtNameProductDetail.text = mProduct!!.titleProduct
            productDetailBinding.txtNumberProductSold.text = mProduct!!.soldQuantity.toString()
            productDetailBinding.txtNumberQuantityProduct.text = mProduct!!.quantity.toString()
            val formattedPrice =
                NumberFormat.getNumberInstance(Locale.GERMANY).format(mProduct!!.price!!.toInt())
            productDetailBinding.txtPrice.text = "$formattedPrice.000VNĐ"
            GlideImageURL.loadImageURL(
                mProduct!!.imageDescription,
                productDetailBinding.imgDescription
            )
            val formattedContent = mProduct!!.description!!.replace("\\n", "\n")
            productDetailBinding.txtContentDescription.text = formattedContent

            productDetailBinding.txtRedMore.setOnClickListener {
                if (productDetailBinding.txtRedMore.text.toString() == "Xem thêm") {
                    productDetailBinding.txtContentDescription.maxLines = Int.MAX_VALUE
                    productDetailBinding.txtContentDescription.ellipsize = null
                    productDetailBinding.txtRedMore.text = "Ẩn bớt"
                } else {
                    productDetailBinding.txtContentDescription.maxLines = 4
                    productDetailBinding.txtContentDescription.ellipsize = null
                    productDetailBinding.txtRedMore.text = "Xem thêm"
                }
            }

            productDetailBinding.txtNameShop.text = mProduct!!.shopUserName
            mListImageDetail!!.addAll(mProduct!!.imagesDetails)
            setDataViewPager2(mListImageDetail!!)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDataViewPager2(mListImageDetail: MutableList<String>) {
        imageDetailAdapter = ImageDetailAdapter(mListImageDetail)
        productDetailBinding.viewPager2DetailProduct.adapter = imageDetailAdapter
        productDetailBinding.viewPager2DetailProduct.clipToPadding = false
        productDetailBinding.viewPager2DetailProduct.clipChildren = false
        productDetailBinding.viewPager2DetailProduct.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER

        productDetailBinding.viewPager2DetailProduct.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                productDetailBinding.txtNumberViewpager2.text =
                    "${position + 1}/${mListImageDetail.size}"
            }
        })
        productDetailBinding.txtNumberViewpager2.text = "1/${mListImageDetail.size}"
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
            productDetailBinding.productDetailRoot, "transition_drawer",
            R.anim.slide_in_right,
            R.anim.slide_out_right,
            bundle
        )
    }

    private fun setDataProductOtherToRecyclerview() {
        productOtherAdapter = ProductOtherAdapter(mListProduct, object : IOnClickProduct {
            override fun onClickProduct(product: Product) {
                val bundle = Bundle()
                bundle.putSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME, product)
                bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "detail")
                TransitionHelper.navigateWithTransition(
                    this@ProductDetailActivity,
                    ProductDetailActivity::class.java,
                    productDetailBinding.productDetailRoot,
                    "transition_drawer",
                    R.anim.slide_in_right,
                    R.anim.slide_no_anim,
                    bundle
                )
            }
        })
        productDetailBinding.rcvProductOtherShop.setHasFixedSize(false)
        productDetailBinding.rcvProductOtherShop.layoutManager =
            LinearLayoutManager(this@ProductDetailActivity, RecyclerView.HORIZONTAL, false)
        productDetailBinding.rcvProductOtherShop.adapter = productOtherAdapter
    }

    private fun setDataProductOtherShopToRecyclerView() {
        productAdapter =
            ProductAdapter(mListProductOther, object : ProductAdapter.IOnClickProductShop {
                override fun onClickProductShop(product: Product) {
                    val bundle = Bundle()
                    bundle.putSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME, product)
                    bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "detail")
                    TransitionHelper.navigateWithTransition(
                        this@ProductDetailActivity,
                        ShopActivity::class.java,
                        productDetailBinding.productDetailRoot,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim,
                        bundle
                    )
                }
            }, object : IOnClickProduct {
                override fun onClickProduct(product: Product) {
                    val bundle = Bundle()
                    bundle.putSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME, product)
                    bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "detail")
                    TransitionHelper.navigateWithTransition(
                        this@ProductDetailActivity,
                        ProductDetailActivity::class.java,
                        productDetailBinding.productDetailRoot,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim,
                        bundle
                    )
                }
            })
        productDetailBinding.rcvIDoLike.setHasFixedSize(false)
        val gridLayoutManager = GridLayoutManager(this@ProductDetailActivity, 2)
        productDetailBinding.rcvIDoLike.layoutManager = gridLayoutManager
        productDetailBinding.rcvIDoLike.adapter = productAdapter
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
                                this@ProductDetailActivity,
                                "Sản phẩm đã hết hàng",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                    val productRef = MarketGreenFirebaseApp[this@ProductDetailActivity]
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
                                    this@ProductDetailActivity,
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
                            this@ProductDetailActivity,
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

    private fun solutionAddToCartProduct() {
        val currentUser = DataStoreManager.getUser()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (mProduct != null && currentUser?.uid != null) {
                    val productRef = MarketGreenFirebaseApp[this@ProductDetailActivity]
                        .getDataProductAddToCartFromFirebaseDatabaseReference()
                        .child(currentUser.uid!!)
                        .child(mProduct!!.productId.toString())
                    val snapshot = productRef.get().await()
                    val existingProductOrder = snapshot.getValue(ProductOrder::class.java)
                    if (existingProductOrder != null) {
                        val updatedQuantity = existingProductOrder.quantityBuy + 1
                        productRef.child("quantityBuy").setValue(updatedQuantity).await()
                    } else {
                        val productOrder = ProductOrder(
                            mProduct!!.productId,
                            mProduct!!.imagePrimary,
                            mProduct!!.titleProduct,
                            mProduct!!.price,
                            1,
                            mProduct!!.quantity,
                            mProduct!!.shopUserId,
                            mProduct!!.shopUserName,
                            ""
                        )
                        productRef.setValue(productOrder).await()
                    }

                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(
                            this@ProductDetailActivity,
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
}