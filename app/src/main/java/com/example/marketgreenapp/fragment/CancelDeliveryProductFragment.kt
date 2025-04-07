package com.example.marketgreenapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.EvaluateProductActivity
import com.example.marketgreenapp.PaymentOrderActivity
import com.example.marketgreenapp.ProductDetailOrderSuccessActivity
import com.example.marketgreenapp.R
import com.example.marketgreenapp.adapter.ProductAwaitProcessAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.FragmentCancelDeliveryTransactionProductBinding
import com.example.marketgreenapp.databinding.FragmentCompleteDeliveryProductsBinding
import com.example.marketgreenapp.model.ProductChooseList
import com.example.marketgreenapp.model.ProductOrder
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.model.User
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CancelDeliveryProductFragment : Fragment() {

    private lateinit var productAwaitProcessAdapter: ProductAwaitProcessAdapter
    private var mListProductStatus: MutableList<ProductStatus>? = null


    private var cancelDeliveryProductFragment: FragmentCancelDeliveryTransactionProductBinding? =
        null
    private val binding get() = cancelDeliveryProductFragment!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.Main) {
            val currentDataStoreManager = DataStoreManager.getUser()
            if (currentDataStoreManager != null) {
                getDataProductAwaitProcess(currentDataStoreManager)
                setDataToRecyclerView() // Cập nhật RecyclerView
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (cancelDeliveryProductFragment == null)
            cancelDeliveryProductFragment =
                FragmentCancelDeliveryTransactionProductBinding.inflate(layoutInflater)
        cancelDeliveryProductFragment!!.lottieLoadingData.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Main) {
            val currentDataStoreManager = DataStoreManager.getUser()
            if (currentDataStoreManager != null) {
                getDataProductAwaitProcess(currentDataStoreManager)
                cancelDeliveryProductFragment!!.lottieLoadingData.cancelAnimation()
                cancelDeliveryProductFragment!!.lottieLoadingData.visibility = View.GONE
                setDataToRecyclerView()
            }
        }
        return binding.root
    }

    private suspend fun getDataProductAwaitProcess(currentDataStoreManager: User) {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[requireContext()]
                    .getDataProductOrderFromFirebaseDatabaseReference()
                    .get().await()
                if (mListProductStatus != null) mListProductStatus!!.clear()
                else mListProductStatus = mutableListOf()

                if (currentDataStoreManager.role.equals("user")) {
                    for (snapshotChildren in dataSnapshot.children) {
                        val productStatus: ProductStatus? =
                            snapshotChildren.getValue(ProductStatus::class.java)
                        productStatus?.let {
                            if (it.nameUserId.equals(currentDataStoreManager.uid)) {
                                if (it.orderStatus.equals(OrderStatus.CANCEL)) {
                                    mListProductStatus!!.add(it)
                                }
                            }
                        }
                    }
                }
                if (currentDataStoreManager.role.equals("shop")) {
                    for (snapshotChildren in dataSnapshot.children) {
                        val productStatus: ProductStatus? =
                            snapshotChildren.getValue(ProductStatus::class.java)
                        productStatus?.let {
                            if (it.shopUserId.equals(currentDataStoreManager.uid)) {
                                if (it.orderStatus.equals(OrderStatus.CANCEL)) {
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
            },requireContext(), object : ProductAwaitProcessAdapter.IOnClickProductStatusDetail {
                override fun onClickProductDetail(product: ProductStatus, position: Int) {
                    val bundle = Bundle()
                    bundle.putSerializable(ConstantKey.KEY_PRODUCT_DETAIL_ORDER_SUCCESS, product)
                    TransitionHelper.navigateWithTransition(
                        requireActivity(),
                        ProductDetailOrderSuccessActivity::class.java,
                        cancelDeliveryProductFragment!!.layoutRoot,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim,
                        bundle
                    )
                }

                override fun onClickBuyAgainProduct(product: ProductStatus) {
                    val productBuyAgain = ProductOrder(
                        product.productId,
                        product.imagePrimary,
                        product.titleProduct,
                        parsePrice(product.productDetail!!.priceProduct!!).toString(),
                        product.quantityBuy,
                        0,
                        product.shopUserId,
                        product.shopUserName,
                        ""
                    )
                    val totalAmount =
                        (parsePrice(product.productDetail!!.priceProduct!!) * product.quantityBuy)
                    val mListProduct: MutableList<ProductOrder> = mutableListOf()
                    mListProduct.add(productBuyAgain)
                    val mListProductBuyAgain = ProductChooseList(mListProduct)
                    val bundle = Bundle()
                    bundle.putSerializable(
                        ConstantKey.KEY_PRODUCT_CHOOSE_LIST,
                        mListProductBuyAgain
                    )
                    bundle.putString(ConstantKey.KEY_PRODUCT_BUY_AGAIN, "product_buy_again")
                    bundle.putLong(ConstantKey.KEY_PRODUCT_TOTAL_PRICE, totalAmount)
                    TransitionHelper.navigateWithTransition(
                        requireActivity(),
                        PaymentOrderActivity::class.java,
                        cancelDeliveryProductFragment!!.layoutRoot,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim,
                        bundle
                    )
                }

                override fun onClickEvaluateProduct(product: ProductStatus) {
                }
            }, object : ProductAwaitProcessAdapter.IOnClickCancelProductOrder {
                override fun onClickCancelProductOrder(product: ProductStatus, position: Int) {
                }
            })
        cancelDeliveryProductFragment!!.rcvProductCancelDelivery.setHasFixedSize(false)
        cancelDeliveryProductFragment!!.rcvProductCancelDelivery.layoutManager =
            LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
        cancelDeliveryProductFragment!!.rcvProductCancelDelivery.addItemDecoration(
            dividerItemDecoration
        )
        cancelDeliveryProductFragment!!.rcvProductCancelDelivery.adapter =
            productAwaitProcessAdapter
    }

    fun parsePrice(priceString: String): Long {
        // Loại bỏ ký tự không cần thiết như "VNĐ" và "."
        val sanitizedPrice = priceString.replace(".", "").replace("VNĐ", "").trim()
        // Chuyển chuỗi sang số và chia cho 1000
        return sanitizedPrice.toLong() / 1000
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelDeliveryProductFragment = null
    }
}