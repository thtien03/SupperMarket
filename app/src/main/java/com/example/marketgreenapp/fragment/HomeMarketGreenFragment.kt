package com.example.marketgreenapp.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.marketgreenapp.*
import com.example.marketgreenapp.adapter.BannerAdapter
import com.example.marketgreenapp.adapter.CategoryAdapter
import com.example.marketgreenapp.adapter.ProductAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.FragmentHomeMarketGreenBinding
import com.example.marketgreenapp.listener.IOnClickItemBanner
import com.example.marketgreenapp.listener.IOnClickItemCategory
import com.example.marketgreenapp.listener.IOnClickProduct
import com.example.marketgreenapp.model.Banner
import com.example.marketgreenapp.model.Category
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeMarketGreenFragment : Fragment() {
    private var homeMarketBinding: FragmentHomeMarketGreenBinding? = null
    private val binding get() = homeMarketBinding!!


    private var mListProduct: MutableList<Product>? = null
    private lateinit var productAdapter: ProductAdapter


    private var listCategory: MutableList<Category>? = null
    private var listBannerFirebase: MutableList<Banner>? = null
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var bannerAdapter: BannerAdapter
    private val handlerBanner = Handler(Looper.myLooper()!!)


    private var runnableBanner: Runnable =
        Runnable { // Runnable sd để định nghĩa 1 đoạn mã thực thi, được sd để lên lịch thực thi 1 đoạn mã sau 1 khoảng time
            if (listBannerFirebase!!.isEmpty())
                return@Runnable
            if (homeMarketBinding?.viewPager2BannerHomeMarket?.currentItem == listBannerFirebase!!.size - 1) {
                homeMarketBinding?.viewPager2BannerHomeMarket?.currentItem = 0
                return@Runnable
            }
            homeMarketBinding?.viewPager2BannerHomeMarket?.currentItem =
                homeMarketBinding?.viewPager2BannerHomeMarket!!.currentItem + 1
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (homeMarketBinding == null)
            homeMarketBinding = FragmentHomeMarketGreenBinding.inflate(inflater, container, false)

        setCategoryMarketGreenToRecyclerView()
        setEvent()
        lifecycleScope.launch(Dispatchers.Main) {
            getDataBanner()
            setDataBannerToViewPager2()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            getListProductFB()
            setListProductToRecyclerView()
        }
        return binding.root
    }

    private fun setCategoryMarketGreenToRecyclerView() {
        listCategory = mutableListOf()
        listCategory = FunctionGlobal.getDataCategories(requireContext())
        categoryAdapter = CategoryAdapter(listCategory, object : IOnClickItemCategory {
            override fun onClickItem(category: Category) {
                val bundle = Bundle()
                bundle.putSerializable(ConstantKey.KEY_INTENT_CATEGORY_HOME, category)
                TransitionHelper.navigateWithTransition(
                    requireActivity(),
                    CategoryProductActivity::class.java,
                    homeMarketBinding!!.homeRoot,
                    "transition_drawer",
                    R.anim.slide_in_right,
                    R.anim.slide_no_anim,
                    bundle
                )
            }
        })
        homeMarketBinding?.rcvCategory?.setHasFixedSize(false)
        val gridLayoutManager =
            GridLayoutManager(requireContext(), 2, RecyclerView.HORIZONTAL, false)
        homeMarketBinding?.rcvCategory?.layoutManager = gridLayoutManager
        homeMarketBinding?.rcvCategory?.adapter = categoryAdapter

    }

    private fun setEvent() {
        homeMarketBinding?.linearLayoutSupport?.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                requireActivity(),
                HelpCustomerActivity::class.java,
                homeMarketBinding!!.homeRoot,
                "transition_market",
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        }
        homeMarketBinding?.linearLayoutDetailPost?.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                requireActivity(),
                ArticleMarketGreenActivity::class.java,
                homeMarketBinding!!.homeRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        }

    }

    private suspend fun getDataBanner() {
        return withContext(Dispatchers.IO)
        {
            val snapshot =
                MarketGreenFirebaseApp[requireContext()].getDataBannerFromFirebaseDatabaseReference()
                    .get().await()
            if (listBannerFirebase == null) listBannerFirebase = mutableListOf()
            else listBannerFirebase!!.clear()
            for (snapshotChildren in snapshot.children) {
                val bannerChildren: Banner? = snapshotChildren.getValue(Banner::class.java)
                bannerChildren?.let {
                    listBannerFirebase!!.add(0, it)
                }
            }
        }
    }

    private fun setDataBannerToViewPager2() {
        bannerAdapter =
            BannerAdapter(mListBanners = listBannerFirebase, object : IOnClickItemBanner {
                override fun onClickItemBanner(banner: Banner) {
                    val bundle = Bundle()
                    bundle.putSerializable(ConstantKey.KEY_INTENT_BANNER, banner)
                    TransitionHelper.navigateWithTransition(
                        requireActivity(),
                        BannerDetailActivity::class.java,
                        homeMarketBinding!!.homeRoot,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim,
                        bundle
                    )
                }
            })
        homeMarketBinding?.viewPager2BannerHomeMarket?.apply {
            this.clipToPadding = false
            this.clipChildren = false
            this.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        homeMarketBinding?.viewPager2BannerHomeMarket?.adapter = bannerAdapter
        homeMarketBinding?.indicatorBannerHomeUser?.setViewPager(homeMarketBinding?.viewPager2BannerHomeMarket)
        homeMarketBinding?.viewPager2BannerHomeMarket?.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handlerBanner.removeCallbacks(runnableBanner)
                handlerBanner.postDelayed(runnableBanner, 3000)
            }
        })
    }

    private suspend fun getListProductFB() {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[requireContext()]
                    .getDataProductFromFirebaseDatabaseReference()
                    .get().await()
                if (mListProduct != null) mListProduct!!.clear()
                else mListProduct = mutableListOf()

                for (dataSnapshotChildren in dataSnapshot.children) {
                    val productChildren: Product? =
                        dataSnapshotChildren.getValue(Product::class.java)
                    productChildren?.let {
                        mListProduct!!.add(0, it)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setListProductToRecyclerView() {
        productAdapter = ProductAdapter(mListProduct, object : ProductAdapter.IOnClickProductShop {
            override fun onClickProductShop(product: Product) {
                val bundle = Bundle()
                bundle.putSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME, product)
                bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "home")
                TransitionHelper.navigateWithTransition(
                    requireActivity(),
                    ShopActivity::class.java,
                    homeMarketBinding!!.homeRoot,
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
                bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "home")
                TransitionHelper.navigateWithTransition(
                    requireActivity(),
                    ProductDetailActivity::class.java,
                    homeMarketBinding!!.homeRoot,
                    "transition_drawer",
                    R.anim.slide_in_right,
                    R.anim.slide_no_anim,
                    bundle
                )
            }
        })
        homeMarketBinding?.rcvProductPopular?.setHasFixedSize(false)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        homeMarketBinding?.rcvProductPopular?.layoutManager = gridLayoutManager
        homeMarketBinding?.rcvProductPopular?.adapter = productAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        homeMarketBinding = null
    }

}