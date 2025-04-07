package com.example.marketgreenapp.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.FollowOrderProductActivity
import com.example.marketgreenapp.ProductDetailOrderSuccessActivity
import com.example.marketgreenapp.R
import com.example.marketgreenapp.adapter.ProductAwaitProcessAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.FragmentAwaitForDeliveryBinding
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.model.User
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AwaitForDeliveryProductFragment : Fragment() {

    private lateinit var productAwaitProcessAdapter: ProductAwaitProcessAdapter
    private var mListProductStatus: MutableList<ProductStatus>? = null

    private var awaitForDeliveryProductFragment: FragmentAwaitForDeliveryBinding? = null
    private val binding get() = awaitForDeliveryProductFragment!!
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
        if (awaitForDeliveryProductFragment == null)
            awaitForDeliveryProductFragment =
                FragmentAwaitForDeliveryBinding.inflate(layoutInflater)
        awaitForDeliveryProductFragment!!.lottieLoadingData.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Main) {
            val currentUserDataStoreManager = DataStoreManager.getUser()
            if (currentUserDataStoreManager != null) {
                getDataProductAwaitProcess(currentUserDataStoreManager)
                awaitForDeliveryProductFragment!!.lottieLoadingData.cancelAnimation()
                awaitForDeliveryProductFragment!!.lottieLoadingData.visibility = View.GONE
                setDataToRecyclerView()
            }
        }
        return binding.root
    }

    private suspend fun getDataProductAwaitProcess(currentUserDataStoreManager: User) {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot = MarketGreenFirebaseApp[requireContext()]
                    .getDataProductOrderFromFirebaseDatabaseReference()
                    .get().await()
                if (mListProductStatus != null) mListProductStatus!!.clear()
                else mListProductStatus = mutableListOf()

                if (currentUserDataStoreManager.role.equals("user")) {
                    for (snapshotChildren in dataSnapshot.children) {
                        val productStatus: ProductStatus? =
                            snapshotChildren.getValue(ProductStatus::class.java)
                        productStatus?.let {
                            if (it.nameUserId.equals(currentUserDataStoreManager.uid)) {
                                if (it.orderStatus.equals(OrderStatus.IN_TRANSIT)) {
                                    mListProductStatus!!.add(it)
                                }
                            }
                        }
                    }
                }
                if (currentUserDataStoreManager.role.equals("shop")) {
                    for (snapshotChildren in dataSnapshot.children) {
                        val productStatus: ProductStatus? =
                            snapshotChildren.getValue(ProductStatus::class.java)
                        productStatus?.let {
                            if (it.shopUserId.equals(currentUserDataStoreManager.uid)) {
                                if (it.orderStatus.equals(OrderStatus.IN_TRANSIT)) {
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
                    shopSolutionUpdateProductStatus(productStatus, position)
                }
            },requireContext(), object : ProductAwaitProcessAdapter.IOnClickProductStatusDetail {
                override fun onClickProductDetail(product: ProductStatus, position: Int) {
                    val bundle = Bundle()
                    bundle.putSerializable(ConstantKey.KEY_PRODUCT_DETAIL_ORDER_SUCCESS, product)
                    TransitionHelper.navigateWithTransition(
                        requireActivity(),
                        ProductDetailOrderSuccessActivity::class.java,
                        awaitForDeliveryProductFragment!!.layoutRoot,
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
        awaitForDeliveryProductFragment!!.rcvProductAwaitForDelivery.setHasFixedSize(false)
        awaitForDeliveryProductFragment!!.rcvProductAwaitForDelivery.layoutManager =
            LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
        awaitForDeliveryProductFragment!!.rcvProductAwaitForDelivery.addItemDecoration(
            dividerItemDecoration
        )
        awaitForDeliveryProductFragment!!.rcvProductAwaitForDelivery.adapter =
            productAwaitProcessAdapter
    }

    private fun shopSolutionUpdateProductStatus(productStatus: ProductStatus, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (productStatus.orderStatus.equals(OrderStatus.IN_TRANSIT)) {
                    val productOrderRef = MarketGreenFirebaseApp[requireContext()]
                        .getDataProductOrderFromFirebaseDatabaseReference()
                        .child(productStatus.productStatusId.toString())
                    productOrderRef.child("orderStatus").setValue(OrderStatus.WAITING_FOR_PICKUP)
                        .await()
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(
                            requireContext(),
                            "Xác nhận đơn hàng thành công", Toast.LENGTH_SHORT
                        ).show()
                        if (mListProductStatus!!.isNotEmpty()) {
                            mListProductStatus!!.remove(productStatus)
                            productAwaitProcessAdapter.notifyItemRemoved(position)
                            productAwaitProcessAdapter.notifyItemRangeRemoved(
                                position,
                                mListProductStatus!!.size
                            )
                        }
                        (activity as? FollowOrderProductActivity)?.let {
                            it.followOrderBinding.viewPager2.currentItem =
                                3
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        awaitForDeliveryProductFragment = null
    }
}