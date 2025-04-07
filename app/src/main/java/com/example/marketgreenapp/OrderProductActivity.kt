package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.ProductOrderAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityOrderProductBinding
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

class OrderProductActivity : AppCompatActivity() {
    private lateinit var orderProductActivity: ActivityOrderProductBinding

    private var mListProductOrder: MutableList<ProductOrder>? = null
    private var stringBack: String? = null
    private lateinit var productOrderAdapter: ProductOrderAdapter

    private val mProductSelected = mutableListOf<ProductOrder>()

    private var totalAmount: Long = 0
    private var totalQuantity = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderProductActivity = ActivityOrderProductBinding.inflate(layoutInflater)
        setContentView(orderProductActivity.root)


        getDataIntent()
        orderProductActivity.lottieLoadingData.visibility = View.VISIBLE
        lifecycleScope.launch {
            getListProductOrderFB()
            orderProductActivity.lottieLoadingData.cancelAnimation()
            orderProductActivity.lottieLoadingData.visibility = View.GONE
            setListProductOrderToRecyclerView()
        }
        orderProductActivity.imgBack.setOnClickListener {
            if (stringBack != null) {
                if (stringBack.equals("main")) {
                    TransitionHelper.navigateWithTransition(
                        this@OrderProductActivity,
                        MainActivity::class.java,
                        orderProductActivity.orderCartRoot,
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
        orderProductActivity.btnPayment.setOnClickListener {
            val bundle = Bundle()
            val mListProductOrderFilter: MutableList<ProductOrder> =
                mListProductOrder!!.filter { it.isChecked } as MutableList<ProductOrder>
            if (mListProductOrderFilter.isEmpty()) {
                showNotificationDialog(Gravity.CENTER)
                return@setOnClickListener
            }
            val productOrderChoose = ProductChooseList(mListProductOrderFilter)
            bundle.putSerializable(ConstantKey.KEY_PRODUCT_CHOOSE_LIST, productOrderChoose)
            bundle.putLong(ConstantKey.KEY_PRODUCT_TOTAL_PRICE, totalAmount)
            TransitionHelper.navigateWithTransition(
                this@OrderProductActivity,
                PaymentOrderActivity::class.java,
                orderProductActivity.orderCartRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_no_anim,
                bundle
            )
        }
    }

    private fun getDataIntent() {
        val bundle = intent.extras ?: return
        stringBack = bundle.getString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "")
    }

    @SuppressLint("SetTextI18n")
    private fun showNotificationDialog(gravity: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_item_dialog_warning)

        val btnAccept: Button = dialog.findViewById(R.id.btn_accept)
        val txtContent: TextView = dialog.findViewById(R.id.txt_content)
        txtContent.text =
            "Vui lòng chọn ít nhất 1 sản phẩm để thanh toán"
        btnAccept.setOnClickListener {
            dialog.dismiss()
        }
        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(0))
        val windowAttribute = window.attributes
        windowAttribute.gravity = gravity

        window.attributes = windowAttribute
        dialog.setCancelable(false)
        dialog.show()
    }

    private suspend fun getListProductOrderFB() {
        return withContext(Dispatchers.IO)
        {
            try {
                val userCurrent = DataStoreManager.getUser()
                if (userCurrent?.uid != null) {
                    val dataSnapshot = MarketGreenFirebaseApp[this@OrderProductActivity]
                        .getDataProductAddToCartFromFirebaseDatabaseReference()
                        .child(userCurrent.uid!!)
                        .get().await()
                    if (mListProductOrder != null) mListProductOrder!!.clear()
                    else mListProductOrder = mutableListOf()

                    for (dataSnapshotChildren in dataSnapshot.children) {
                        val productChildren: ProductOrder? =
                            dataSnapshotChildren.getValue(ProductOrder::class.java)
                        productChildren?.let {
                            mListProductOrder!!.add(0, it)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setListProductOrderToRecyclerView() {
        if (mListProductOrder!!.isEmpty()) {
            orderProductActivity.rcvProductOrder.visibility = View.GONE
            orderProductActivity.constraintLayoutNoProduct.visibility = View.VISIBLE
        } else {
            orderProductActivity.rcvProductOrder.visibility = View.VISIBLE
            orderProductActivity.constraintLayoutNoProduct.visibility = View.GONE
        }

        productOrderAdapter = ProductOrderAdapter(mListProductOrder,
            object : ProductOrderAdapter.IOnClickProductOrder {
                override fun onClickDelete(product: ProductOrder, position: Int) {
                    AlertDialog.Builder(this@OrderProductActivity)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Xóa sản phẩm ra khỏi giỏ hàng?")
                        .setPositiveButton("Đồng ý") { _: DialogInterface?, _: Int ->
                            solutionDeleteProductOrder(product, position)
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                }

                override fun onClickAccessShop(product: ProductOrder) {
                    val products = Product(
                        product.productId,
                        product.imagePrimary,
                        product.titleProduct,
                        product.shopUserId,
                        product.shopUserName
                    )
                    val bundle = Bundle()
                    bundle.putSerializable(ConstantKey.KEY_INTENT_PRODUCT_HOME, products)
                    bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "order")
                    TransitionHelper.navigateWithTransition(
                        this@OrderProductActivity,
                        ShopActivity::class.java,
                        orderProductActivity.orderCartRoot,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim,
                        bundle
                    )
                }

                override fun onQuantityChanged(product: ProductOrder, position: Int) {
                    updateQuantityBuyToFb(product)
                }

                override fun onCheckBoxChanged(product: ProductOrder, isChecked: Boolean) {
                    product.isChecked = isChecked
                    if (isChecked) {
                        mProductSelected.removeAll { it.productId == product.productId }
                        mProductSelected.add(product)
                    } else {
                        mProductSelected.removeAll { it.productId == product.productId }
                    }
                    calculator()
                }
            })
        orderProductActivity.rcvProductOrder.setHasFixedSize(false)
        orderProductActivity.rcvProductOrder.layoutManager = LinearLayoutManager(this)
        orderProductActivity.rcvProductOrder.adapter = productOrderAdapter
    }

    private fun solutionDeleteProductOrder(product: ProductOrder, position: Int) {
        lifecycleScope.launch {
            try {
                val userCurrent = DataStoreManager.getUser()
                if (userCurrent?.uid != null) {
                    if (product.sizeColor?.isNotEmpty() == true) {
                        val input = product.sizeColor
                        val parts = input!!.split(",").map { it.trim() }
                        val size = parts[0].substringAfter("size :").trim()
                        val color = parts[1].substringAfter("color :").trim()

                        MarketGreenFirebaseApp[this@OrderProductActivity]
                            .getDataProductAddToCartFromFirebaseDatabaseReference()
                            .child(userCurrent.uid!!)
                            .child("${product.productId}_${size}_${color}").removeValue().await()
                    } else {
                        MarketGreenFirebaseApp[this@OrderProductActivity]
                            .getDataProductAddToCartFromFirebaseDatabaseReference()
                            .child(userCurrent.uid!!)
                            .child(product.productId.toString()).removeValue().await()
                    }
                    withContext(Dispatchers.Main)
                    {
                        mListProductOrder!!.remove(product)
                        productOrderAdapter.notifyItemRemoved(position)
                        Toast.makeText(
                            this@OrderProductActivity,
                            "Xóa thành công ! ",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (mListProductOrder!!.isEmpty()) {
                            orderProductActivity.rcvProductOrder.visibility = View.GONE
                            orderProductActivity.constraintLayoutNoProduct.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateQuantityBuyToFb(product: ProductOrder) {
        lifecycleScope.launch(Dispatchers.IO) {

            try {
                val userCurrent = DataStoreManager.getUser()
                if (userCurrent?.uid != null) {
                    MarketGreenFirebaseApp[this@OrderProductActivity]
                        .getDataProductAddToCartFromFirebaseDatabaseReference()
                        .child(userCurrent.uid!!)
                        .child(product.productId.toString()).child("quantityBuy")
                        .setValue(product.quantityBuy).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculator() {
        totalAmount = 0
        totalQuantity = 0
        for (product in mProductSelected) {
            totalAmount += (product.price!!.toLong() * product.quantityBuy)
            totalQuantity += product.quantityBuy
        }
        orderProductActivity.txtNumberProduct.text = totalQuantity.toString()
        val formattedPrice =
            NumberFormat.getNumberInstance(Locale.GERMANY).format(totalAmount)
        orderProductActivity.txtTotalPrice.text = "${formattedPrice}.000VNĐ"
    }
}