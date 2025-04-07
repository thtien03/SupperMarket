package com.example.marketgreenapp.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.DeliveryAddressActivity
import com.example.marketgreenapp.FollowOrderProductActivity
import com.example.marketgreenapp.ProductDetailOrderSuccessActivity
import com.example.marketgreenapp.R
import com.example.marketgreenapp.adapter.ProductAwaitProcessAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.FragmentAwaitProcessProductBinding
import com.example.marketgreenapp.databinding.FragmentAwaitToPickUpBinding
import com.example.marketgreenapp.model.DeliveryAddress
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.model.User
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AwaitToPickUpProductFragment : Fragment() {
    private lateinit var productAwaitProcessAdapter: ProductAwaitProcessAdapter
    private var mListProductStatus: MutableList<ProductStatus>? = null
    private var awaitToPickUpProductFragmentBinding: FragmentAwaitToPickUpBinding? = null
    private val binding get() = awaitToPickUpProductFragmentBinding!!

    private var mCurrentPositionUpdateDeliveryAddress = -1
    private var mProductStatus:ProductStatus? = null
    private var activityResultLauncherDelivery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            if (result.data != null) {
                val delivery =
                    result.data!!.getSerializableExtra(ConstantKey.KEY_DELIVERY) as DeliveryAddress?
                delivery?.let {
                    updateDeliveryAddress(it)
                }
            }
        }
    }
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
        if (awaitToPickUpProductFragmentBinding == null)
            awaitToPickUpProductFragmentBinding =
                FragmentAwaitToPickUpBinding.inflate(layoutInflater)
        awaitToPickUpProductFragmentBinding!!.lottieLoadingData.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Main) {
            val currentDataStoreManager = DataStoreManager.getUser()
            if (currentDataStoreManager != null) {
                getDataProductAwaitProcess(currentDataStoreManager)
                awaitToPickUpProductFragmentBinding!!.lottieLoadingData.cancelAnimation()
                awaitToPickUpProductFragmentBinding!!.lottieLoadingData.visibility = View.GONE
                setDataToRecyclerView()
            }
        }
        return binding.root
    }
    private fun updateDeliveryAddress(delivery: DeliveryAddress) {
        if(mCurrentPositionUpdateDeliveryAddress != -1 && mListProductStatus != null)
        {
            mListProductStatus!![mCurrentPositionUpdateDeliveryAddress].deliveryAddress = delivery
            productAwaitProcessAdapter.notifyItemChanged(mCurrentPositionUpdateDeliveryAddress)
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if(mProductStatus != null)
                {
                    val productRef = MarketGreenFirebaseApp[requireContext()]
                        .getDataProductOrderFromFirebaseDatabaseReference()
                        .child(mProductStatus!!.productStatusId.toString())
                    productRef.child("deliveryAddress").setValue(delivery).await()
                }
            }
            catch (e:java.lang.Exception)
            {
                e.printStackTrace()
            }
        }
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
                                if (it.orderStatus.equals(OrderStatus.WAITING_FOR_PICKUP)) {
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
                                if (it.orderStatus.equals(OrderStatus.WAITING_FOR_PICKUP)) {
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
                override fun onClickDelivery(productStatus: ProductStatus, position:Int) {
                    mProductStatus = productStatus
                    mCurrentPositionUpdateDeliveryAddress = position
                    val intent = Intent(requireContext(), DeliveryAddressActivity::class.java)
                    val pairs: Array<Pair<View, String>> = arrayOf(
                        Pair(awaitToPickUpProductFragmentBinding!!.layoutRoot, "transition_drawer")
                    )
                    val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        *pairs
                    )
                    option.update(
                        ActivityOptionsCompat.makeCustomAnimation(
                            requireContext(),
                            R.anim.slide_in_right,
                            R.anim.slide_no_anim
                        )
                    )
                    activityResultLauncherDelivery.launch(intent, option)
                }

                override fun onClickUpdateStatusOrderProduct(
                    productStatus: ProductStatus,
                    position: Int
                ) {
                    shopSolutionUpdateProductStatus(productStatus, position)
                }
            },requireContext(),object : ProductAwaitProcessAdapter.IOnClickProductStatusDetail{
                override fun onClickProductDetail(product: ProductStatus, position: Int) {
                    val bundle = Bundle()
                    bundle.putSerializable(ConstantKey.KEY_PRODUCT_DETAIL_ORDER_SUCCESS,product)
                    TransitionHelper.navigateWithTransition(
                        requireActivity(),
                        ProductDetailOrderSuccessActivity::class.java,
                        awaitToPickUpProductFragmentBinding!!.layoutRoot,
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
            },object : ProductAwaitProcessAdapter.IOnClickCancelProductOrder{
                override fun onClickCancelProductOrder(product: ProductStatus, position: Int) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận hủy")
                        .setMessage("Bạn có muốn tiếp tục hủy đơn hàng không?")
                        .setPositiveButton("Đồng ý") { _: DialogInterface?, _: Int ->
                            solutionCancelProduct(product, position)
                        }
                        .setNegativeButton("Không", null)
                        .show()
                }
            })
        awaitToPickUpProductFragmentBinding!!.rcvProductAwaitToPickUp.setHasFixedSize(false)
        awaitToPickUpProductFragmentBinding!!.rcvProductAwaitToPickUp.layoutManager =
            LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
        awaitToPickUpProductFragmentBinding!!.rcvProductAwaitToPickUp.addItemDecoration(
            dividerItemDecoration
        )
        awaitToPickUpProductFragmentBinding!!.rcvProductAwaitToPickUp.adapter =
            productAwaitProcessAdapter
    }

    private fun solutionCancelProduct(product: ProductStatus, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val productOrderRef = MarketGreenFirebaseApp[requireContext()]
                    .getDataProductOrderFromFirebaseDatabaseReference()
                    .child(product.productStatusId.toString())


                val quantityOrdered = product.quantityBuy
                val shopProductRef = MarketGreenFirebaseApp[requireContext()]
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
                productOrderRef.child("orderStatus").setValue(OrderStatus.CANCEL)
                    .await()
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(
                        requireContext(),
                        "Hủy đơn hàng thành công", Toast.LENGTH_SHORT
                    ).show()
                    if (mListProductStatus!!.isNotEmpty()) {
                        mListProductStatus!!.remove(product)
                        productAwaitProcessAdapter.notifyItemRemoved(position)
                        productAwaitProcessAdapter.notifyItemRangeRemoved(
                            position,
                            mListProductStatus!!.size
                        )
                    }
                    (activity as? FollowOrderProductActivity)?.let {
                        it.followOrderBinding.viewPager2.currentItem =
                            4
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun shopSolutionUpdateProductStatus(productStatus: ProductStatus, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (productStatus.orderStatus.equals(OrderStatus.WAITING_FOR_PICKUP)) {
                    val productOrderRef = MarketGreenFirebaseApp[requireContext()]
                        .getDataProductOrderFromFirebaseDatabaseReference()
                        .child(productStatus.productStatusId.toString())
                    productOrderRef.child("orderStatus").setValue(OrderStatus.IN_TRANSIT)
                        .await()
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(
                            requireContext(),
                            "Lấy đơn hàng thành công", Toast.LENGTH_SHORT
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
                                2
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
        awaitToPickUpProductFragmentBinding = null
    }
}