package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.marketgreenapp.adapter.ProductAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityCategoryProductBinding
import com.example.marketgreenapp.listener.IOnClickProduct
import com.example.marketgreenapp.model.Category
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class CategoryProductActivity : AppCompatActivity() {
    private lateinit var categoryProductBinding: ActivityCategoryProductBinding
    private var stringBack: String? = null


    private var mListProduct: MutableList<Product>? = null
    private var mListProductFilter: MutableList<Product>? = null
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryProductBinding = ActivityCategoryProductBinding.inflate(layoutInflater)
        setContentView(categoryProductBinding.root)

        getDataIntent()
        solutionSearchView()

        categoryProductBinding.imgBackMain.setOnClickListener {
            this.finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDataIntent() {
        val bundle = intent.extras ?: return
        val category: Category? =
            bundle.getSerializable(ConstantKey.KEY_INTENT_CATEGORY_HOME) as Category?
        stringBack = bundle.getString(ConstantKey.KEY_INTENT_CATEGORY_PRODUCT_KEY, "")
        if (category != null) {
            categoryProductBinding.imgCategory.setImageResource(category.image!!)
            categoryProductBinding.txtCategory.text = category.name.toString().trim()
            categoryProductBinding.txtTitleProuctShop.text = "Sản phẩm ${category.name}"
            categoryProductBinding.lottieLoadingData.visibility = View.VISIBLE
            lifecycleScope.launch {
                getProductInformationCategoryId(category)
                categoryProductBinding.lottieLoadingData.cancelAnimation()
                categoryProductBinding.lottieLoadingData.visibility = View.GONE
                setDataProductToRecyclerview()
            }
        }
    }

    private suspend fun getProductInformationCategoryId(category: Category) {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[this@CategoryProductActivity]
                    .getDataProductFromFirebaseDatabaseReference()
                    .get().await()
                if (mListProduct != null) mListProduct!!.clear()
                else mListProduct = mutableListOf()

                for (dataSnapshotChildren in dataSnapshot.children) {
                    val productChildren: Product? =
                        dataSnapshotChildren.getValue(Product::class.java)
                    productChildren?.let {
                        if (it.category == category.id) {
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
        if (mListProduct!!.isEmpty()) {
            categoryProductBinding.rcvProductCategory.visibility = View.GONE
            categoryProductBinding.constraintLayoutNoProduct.visibility = View.VISIBLE
        } else {
            categoryProductBinding.rcvProductCategory.visibility = View.VISIBLE
            categoryProductBinding.constraintLayoutNoProduct.visibility = View.GONE
        }

        productAdapter = ProductAdapter(mListProduct, object : ProductAdapter.IOnClickProductShop {
            override fun onClickProductShop(product: Product) {
                val bundle = Bundle()
                bundle.putSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME, product)
                bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "category")
                TransitionHelper.navigateWithTransition(
                    this@CategoryProductActivity,
                    ShopActivity::class.java,
                    categoryProductBinding.categoryRoot,
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
                bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "category")
                TransitionHelper.navigateWithTransition(
                    this@CategoryProductActivity,
                    ProductDetailActivity::class.java,
                    categoryProductBinding.categoryRoot,
                    "transition_drawer",
                    R.anim.slide_in_right,
                    R.anim.slide_no_anim,
                    bundle
                )
            }
        })
        categoryProductBinding.rcvProductCategory.setHasFixedSize(false)
        val gridLayoutManager = GridLayoutManager(this@CategoryProductActivity, 2)
        categoryProductBinding.rcvProductCategory.layoutManager = gridLayoutManager
        categoryProductBinding.rcvProductCategory.adapter = productAdapter
    }

    private fun solutionSearchView() {
        categoryProductBinding.searchInformation.setOnQueryTextListener(object :
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
            productAdapter.setDataFilter(mListProduct)
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
                productAdapter.setDataFilter(mListProductFilter)
            }
        }
    }
}