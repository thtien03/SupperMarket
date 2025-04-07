package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.ProductOrderPaymentAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.OrderStatus
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityPaymentOrderBinding
import com.example.marketgreenapp.model.*
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class PaymentOrderActivity : AppCompatActivity() {
    private lateinit var paymentOrderBinding: ActivityPaymentOrderBinding
    private var checkBoxs: Array<CheckBox> = arrayOf()
    private var deliveryAddressDefault: DeliveryAddress? = null
    private var historyOrder: HistoryOrderUser? = null
    private var mListProductOrder: MutableList<ProductStatus> = mutableListOf()
    private var mListProductOrderChoose: MutableList<ProductOrder> = mutableListOf()
    private var totalPrice: Long = 0
    private var loadingDialog: Dialog? = null
    private var isDeliveryAddress = false

    private var priceSaleOke = 0
    private var mVoucherSelected: Voucher? = null

    private var methodPayment = ""
    private var userUidOrder = ""
    private var stringKeyBuyProduct = ""
    private lateinit var productOrderPaymentAdapter: ProductOrderPaymentAdapter
    private var activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val delivery =
                    result.data!!.getSerializableExtra(ConstantKey.KEY_DELIVERY) as DeliveryAddress?
                delivery?.let {
                    deliveryAddressDefault = delivery
                    setDeliveryAddressDefault()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private var activityResultLauncherVoucher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val priceSale = result.data?.getIntExtra(ConstantKey.KEY_PRICE_SALE_VOUCHER, 0)
                mVoucherSelected =
                    result.data?.getSerializableExtra(ConstantKey.KEY_VOUCHER_SELECTED) as Voucher?
                if (priceSale != null) {
                    if (priceSale > 0) {
                        priceSaleOke = priceSale
                        val formattedPrice =
                            NumberFormat.getNumberInstance(Locale.GERMANY).format(priceSale)
                        paymentOrderBinding.txtContentPriceVoucher.visibility = View.VISIBLE
                        paymentOrderBinding.txtContentPriceVoucher.text =
                            "-${formattedPrice}.000VNĐ"
                        paymentOrderBinding.txtContentVoucher.text = "voucher khuyến mãi giảm : "
                        paymentOrderBinding.txtPriceVoucher.text = "-${formattedPrice}.000VNĐ"

                        updateCalculatorTotalPrice()
                    } else {
                        paymentOrderBinding.txtContentVoucher.text =
                            "Chưa áp dụng voucher khuyến mãi"
                        paymentOrderBinding.txtPriceVoucher.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentOrderBinding = ActivityPaymentOrderBinding.inflate(layoutInflater)
        setContentView(paymentOrderBinding.root)

        val currentUserDataStoreManager = DataStoreManager.getUser()
        if (currentUserDataStoreManager != null) {
            userUidOrder = currentUserDataStoreManager.uid!!
        }
        paymentOrderBinding.chkPaymentMoney.isChecked = true
        methodPayment = paymentOrderBinding.txtPayment1.text.toString()

        lifecycleScope.launch {
            getDeliveryAddressDefault()
            setDeliveryAddressDefault()
            getDataIntent()
        }
        paymentOrderBinding.imgExpandedDelivery.setOnClickListener {
            solutionDeliveryAddress()
        }
        paymentOrderBinding.imgExpandedVoucher.setOnClickListener {
            solutionVoucher()
        }
        initUI()
        paymentOrderBinding.btnOrder.setOnClickListener {
            solutionOrderProduct()
        }
        paymentOrderBinding.imgBackMain.setOnClickListener {
            this.finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDataIntent() {
        val extras = intent.extras
        if (extras != null) {
            val mListProductOrderChoose =
                extras.getSerializable(ConstantKey.KEY_PRODUCT_CHOOSE_LIST) as? ProductChooseList
            totalPrice = extras.getLong(ConstantKey.KEY_PRODUCT_TOTAL_PRICE)
            stringKeyBuyProduct = extras.getString(ConstantKey.KEY_PRODUCT_BUY_AGAIN, "")
            val formattedPrice =
                NumberFormat.getNumberInstance(Locale.GERMANY).format(totalPrice)

            paymentOrderBinding.txtTotalPrice.text = "${formattedPrice}.000VNĐ"
            updateCalculatorTotalPrice()
            if (mListProductOrderChoose != null) {
                this.mListProductOrderChoose = mListProductOrderChoose.mListProductOrder
                bindDataToProductStatus(mListProductOrderChoose.mListProductOrder)
                paymentOrderBinding.rcvProductOrderPayment.visibility = View.VISIBLE
                paymentOrderBinding.rcvProductOrderPayment.layoutManager =
                    LinearLayoutManager(this)
                paymentOrderBinding.rcvProductOrderPayment.setHasFixedSize(false)
                productOrderPaymentAdapter =
                    ProductOrderPaymentAdapter(mListProductOrderChoose.mListProductOrder)
                paymentOrderBinding.rcvProductOrderPayment.adapter = productOrderPaymentAdapter
            } else {
                paymentOrderBinding.rcvProductOrderPayment.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCalculatorTotalPrice() {
        val calculator = totalPrice + 30 - priceSaleOke
        val formattedPriceTotal =
            NumberFormat.getNumberInstance(Locale.GERMANY).format(calculator)
        paymentOrderBinding.txtTotalPricePayment.text = "${formattedPriceTotal}.000VNĐ"
        paymentOrderBinding.txtBottomTotalPrice.text = "${formattedPriceTotal}.000VNĐ"
    }

    private suspend fun getDeliveryAddressDefault() {
        val currentUser = DataStoreManager.getUser()
        if (currentUser?.uid != null) {
            try {
                return withContext(Dispatchers.IO)
                {
                    val snapshot =
                        MarketGreenFirebaseApp[this@PaymentOrderActivity].getDataDeliveryUserFromFirebaseDatabaseReference()
                            .child(currentUser.uid.toString()).get().await()
                    for (snapshotChildren in snapshot.children) {
                        val addressChildren: DeliveryAddress? =
                            snapshotChildren.getValue(DeliveryAddress::class.java)
                        addressChildren?.let {
                            if (it.isDefault) {
                                deliveryAddressDefault = it
                                return@withContext
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setDeliveryAddressDefault() {
        if (deliveryAddressDefault != null) {
            paymentOrderBinding.apply {
                this.txtNameUser.text = deliveryAddressDefault!!.name_receive
                this.txtPhoneNumber.text = deliveryAddressDefault!!.phone
                this.txtDeliveryAddress.text = deliveryAddressDefault!!.address
            }
            paymentOrderBinding.constraintLayoutInvalid.visibility = View.GONE
            paymentOrderBinding.constraintLayoutValid.visibility = View.VISIBLE
            isDeliveryAddress = true
        } else {
            isDeliveryAddress = false
            paymentOrderBinding.constraintLayoutInvalid.visibility = View.VISIBLE
            paymentOrderBinding.constraintLayoutValid.visibility = View.GONE
        }
    }

    private fun solutionDeliveryAddress() {
        val intent = Intent(this@PaymentOrderActivity, DeliveryAddressActivity::class.java)
        val pairs: Array<Pair<View, String>> = arrayOf(
            Pair(paymentOrderBinding.paymentOrderRoot, "transition_drawer")
        )
        val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@PaymentOrderActivity,
            *pairs
        )
        option.update(
            ActivityOptionsCompat.makeCustomAnimation(
                this@PaymentOrderActivity,
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        )
        activityResultLauncher.launch(intent, option)
    }

    private fun solutionVoucher() {
        val bundle = Bundle()
        bundle.putLong(ConstantKey.KEY_PRODUCT_TOTAL_PRICE, totalPrice)
        mVoucherSelected?.let {
            bundle.putSerializable(ConstantKey.KEY_VOUCHER_SELECTED, it)
        }
        val intent = Intent(this@PaymentOrderActivity, SaleActivity::class.java)
        intent.putExtras(bundle)
        val pairs: Array<Pair<View, String>> = arrayOf(
            Pair(paymentOrderBinding.paymentOrderRoot, "transition_drawer")
        )
        val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@PaymentOrderActivity,
            *pairs
        )
        option.update(
            ActivityOptionsCompat.makeCustomAnimation(
                this@PaymentOrderActivity,
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        )
        activityResultLauncherVoucher.launch(intent, option)
    }

    private fun initUI() {
        setColorRules()
        checkBoxs = arrayOf(
            paymentOrderBinding.chkPaymentMomo,
            paymentOrderBinding.chkPaymentMoney,
            paymentOrderBinding.chkPaymentPaypal
        )
        checkBoxs.forEach { checkBox ->
            checkBox.setOnClickListener {
                uncheckOtherCheckBoxes(checkBox)
            }
        }
    }

    private fun uncheckOtherCheckBoxes(clickedCheckBox: CheckBox) {

        checkBoxs.forEach { checkbox ->
            if (checkbox != clickedCheckBox) {
                checkbox.isChecked = checkbox == clickedCheckBox
            }
            if (clickedCheckBox == paymentOrderBinding.chkPaymentPaypal) {
                methodPayment = paymentOrderBinding.txtPayment2.text.toString()
            } else if (clickedCheckBox == paymentOrderBinding.chkPaymentMomo) {
                methodPayment = paymentOrderBinding.txtPayment3.text.toString()
            }
        }
    }


    private fun setColorRules() {
        val checkBoxText =
            getString(R.string.accept_rules)
        val spannable = SpannableString(checkBoxText)

        val startIndex = checkBoxText.indexOf("Điều kiện & Điều khoản") // 5
        val endIndex = startIndex + "Điều kiện & Điều khoản".length // 5 + 22 = 27

        spannable.setSpan(
            ForegroundColorSpan(Color.RED),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        paymentOrderBinding.chkAcceptRules.text = spannable
    }

    private fun bindDataToProductStatus(mListProduct: MutableList<ProductOrder>) {
        if (mListProduct.size > 0) {
            for (productOrderChoose in mListProduct) {
                val productStatusId = System.currentTimeMillis()
                val productDetailId = System.currentTimeMillis()
                val priceTotal =
                    (productOrderChoose.price!!.toInt() * productOrderChoose.quantityBuy + 30 - parsePrice(
                        paymentOrderBinding.txtPriceVoucher.text.toString()
                    ))
                val formattedPrice =
                    NumberFormat.getNumberInstance(Locale.GERMANY).format(priceTotal)
                val priceProduct =
                    (productOrderChoose.price!!.toInt())
                val formattedPriceProduct =
                    NumberFormat.getNumberInstance(Locale.GERMANY).format(priceProduct)

                val priceTotalPayment =
                    (productOrderChoose.price!!.toInt() * productOrderChoose.quantityBuy)
                val formattedPricePayment =
                    NumberFormat.getNumberInstance(Locale.GERMANY).format(priceTotalPayment)
                val currentDate = LocalDateTime.now()
                val formattedDate =
                    currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                val totalQuantityBuy = getQuantityProduct()
                val productDetail = ProductDetail(
                    productDetailId,
                    productOrderChoose.productId,
                    productOrderChoose.imagePrimary!!,
                    productOrderChoose.titleProduct,
                    formattedDate,
                    methodPayment,
                    "${formattedPriceProduct}.000VNĐ",
                    paymentOrderBinding.txtPriceTransport.text.toString(),
                    paymentOrderBinding.txtPriceVoucher.text.toString(),
                    paymentOrderBinding.txtTotalPricePayment.text.toString(),
                    totalQuantityBuy,
                    deliveryAddressDefault,
                    userUidOrder,
                    productOrderChoose.sizeColor
                )
                val productStatus = ProductStatus(
                    productStatusId,
                    productOrderChoose.productId,
                    productOrderChoose.imagePrimary,
                    productOrderChoose.titleProduct,
                    "${formattedPrice}.000VNĐ",
                    productOrderChoose.quantityBuy,
                    productOrderChoose.shopUserId,
                    productOrderChoose.shopUserName,
                    userUidOrder,
                    methodPayment,
                    deliveryAddressDefault,
                    OrderStatus.PENDING,
                    mutableListOf(),
                    "",
                    productDetail
                )
                println("========================================${deliveryAddressDefault}")
                mListProductOrder.add(productStatus)
            }
        }
    }

    fun parsePrice(priceString: String): Long {
        // Loại bỏ ký tự không cần thiết như "VNĐ" và "."
        val sanitizedPrice = priceString.replace(".", "").replace("VNĐ", "").trim()
        // Chuyển chuỗi sang số và chia cho 1000
        return sanitizedPrice.toLong() / 1000
    }


    private suspend fun removeProductCart(mListProduct: MutableList<ProductOrder>) {
        return withContext(Dispatchers.IO)
        {
            try {
                var productIdOr: String
                val currentUserDataManager = DataStoreManager.getUser()
                if (currentUserDataManager != null) {
                    for (item in mListProduct) {
                        productIdOr = if (item.sizeColor?.isNotEmpty() == true) {
                            val input = item.sizeColor
                            val parts = input!!.split(",").map { it.trim() }
                            val size = parts[0].substringAfter("size :").trim()
                            val color = parts[1].substringAfter("color :").trim()

                            "${item.productId.toString()}_${size}_${color}"
                        } else {
                            item.productId.toString()
                        }

                        MarketGreenFirebaseApp[this@PaymentOrderActivity]
                            .getDataProductAddToCartFromFirebaseDatabaseReference()
                            .child(currentUserDataManager.uid!!)
                            .child(productIdOr)
                            .removeValue().await()
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun setDataProductOrderHistory() {
        val currentDate = LocalDateTime.now()
        val formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val historyOrderId = System.currentTimeMillis()
        val totalQuantityBuy = getQuantityProduct()
        historyOrder = HistoryOrderUser(
            historyOrderId,
            formattedDate,
            methodPayment,
            paymentOrderBinding.txtTotalPrice.text.toString(),
            paymentOrderBinding.txtPriceTransport.text.toString(),
            paymentOrderBinding.txtPriceVoucher.text.toString(),
            paymentOrderBinding.txtTotalPricePayment.text.toString(),
            totalQuantityBuy,
            deliveryAddressDefault,
            userUidOrder,
            this.mListProductOrderChoose
        )
        return withContext(Dispatchers.IO)
        {
            try {
                MarketGreenFirebaseApp[this@PaymentOrderActivity]
                    .getDataHistoryFromFirebaseDatabaseReference()
                    .child(historyOrderId.toString()).setValue(historyOrder)
                    .await()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getQuantityProduct(): Int {
        var count = 0
        if (mListProductOrderChoose.size > 0) {
            for (item in mListProductOrderChoose) {
                count += item.quantityBuy
            }
            return count
        }
        return 0
    }

    private suspend fun setDataProductStatusToFb(mListProductStatus: MutableList<ProductStatus>) {
        return withContext(Dispatchers.IO)
        {
            try {
                for (item in mListProductStatus) {
                    MarketGreenFirebaseApp[this@PaymentOrderActivity]
                        .getDataProductOrderFromFirebaseDatabaseReference()
                        .child(item.productStatusId.toString()).setValue(item)
                        .await()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun updateQuantityProduct() {
        return withContext(Dispatchers.IO)
        {
            try {
                if (mListProductOrderChoose.isNotEmpty()) {
                    val updateJobs = mListProductOrderChoose.map { item ->
                        async {
                            val productRef = MarketGreenFirebaseApp[this@PaymentOrderActivity]
                                .getDataProductFromFirebaseDatabaseReference()
                                .child(item.productId.toString())

                            if (item.sizeColor?.isNotEmpty() == true) {
                                val input = item.sizeColor
                                val parts = input!!.split(",").map { it.trim() }
                                val size = parts[0].substringAfter("size :").trim()
                                val color = parts[1].substringAfter("color :").trim()

                                // Lấy danh sách variations
                                val variationsSnapshot =
                                    productRef.child("variations").get().await()
                                val variations = variationsSnapshot.children.toList()

                                // Tìm variation phù hợp với size và color
                                val matchedVariation = variations.firstOrNull { snapshot ->
                                    snapshot.child("size").value.toString() == size &&
                                            snapshot.child("color").value.toString() == color
                                }
                                if (matchedVariation != null) {
                                    val variationKey = matchedVariation.key!!
                                    val currentQuantity =
                                        matchedVariation.child("quantity").getValue(Int::class.java)
                                            ?: 0
                                    val updatedQuantity = currentQuantity - item.quantityBuy

                                    // Cập nhật lại quantity của variation
                                    productRef.child("variations").child(variationKey)
                                        .child("quantity").setValue(updatedQuantity).await()
                                }
                                // Cập nhật tổng số lượng bán được (soldQuantity)
                                val currentSoldQuantity = productRef.child("soldQuantity").get()
                                    .await()
                                    .getValue(Int::class.java) ?: 0
                                val updatedSoldQuantity = currentSoldQuantity + item.quantityBuy
                                productRef.child("soldQuantity").setValue(updatedSoldQuantity)
                                    .await()
                            } else {
                                // Cập nhật số lượng tồn kho và số lượng đã bán
                                val quantityItem = item.quantityWareHouse - item.quantityBuy
                                productRef.child("quantity").setValue(quantityItem).await()
                                val currentSoldQuantity = productRef.child("soldQuantity").get()
                                    .await()
                                    .getValue(Int::class.java) ?: 0
                                val updatedSoldQuantity = currentSoldQuantity + item.quantityBuy
                                productRef.child("soldQuantity").setValue(updatedSoldQuantity)
                                    .await()
                            }
                        }
                    }
                    // Đợi tất cả các công việc hoàn thành
                    updateJobs.awaitAll()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun solutionOrderProduct() {
        if (paymentOrderBinding.chkAcceptRules.isChecked) {
            if (isDeliveryAddress) {
                showDialog(true)
                lifecycleScope.launch {
                    /*setDataProductOrderHistory()
                    removeProductCart(mListProductOrderChoose)
                    if(mListProductOrder.size > 0) setDataProductStatusToFb(mListProductOrder)*/
                    try {
                        // Chạy song song các công việc không phụ thuộc
                        val updateQuantityJob = async { updateQuantityProduct() }
                        val historyJob = async { setDataProductOrderHistory() }
                        if (stringKeyBuyProduct != "product_buy_again") {
                            val removeCartJob = async { removeProductCart(mListProductOrderChoose) }
                            removeCartJob.await()
                        }
                        val setStatusJob = async {
                            if (mListProductOrder.size > 0) setDataProductStatusToFb(
                                mListProductOrder
                            )
                        }
                        // Đợi tất cả các công việc hoàn thành
                        updateQuantityJob.await()
                        historyJob.await()
                        setStatusJob.await()
                        withContext(Dispatchers.Main)
                        {
                            showDialog(false)
                            Toast.makeText(
                                this@PaymentOrderActivity,
                                "Thanh toán thành công", Toast.LENGTH_SHORT
                            ).show()
                            val bundle = Bundle()
                            bundle.putSerializable(
                                ConstantKey.KEY_PRODUCT_PAYMENT_SUCCESS,
                                historyOrder
                            )
                            TransitionHelper.navigateWithTransition(
                                this@PaymentOrderActivity,
                                TransactionActivity::class.java,
                                paymentOrderBinding.paymentOrderRoot,
                                "transaction_drawer",
                                R.anim.slide_in_right,
                                R.anim.slide_no_anim,
                                bundle
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showMessage("Đã xảy ra lỗi, vui lòng thử lại sau!")
                    }

                }
            } else {
                showMessage("Bạn chưa thêm địa chỉ nhận hàng , vui lòng thêm giúp mình nha !")
            }
        } else {
            showMessage("Bạn chưa đồng ý với điều khoản của chúng tôi")
        }
    }

    private fun Activity.showMessage(strMessage: String) {
        Toast.makeText(this@showMessage, strMessage, Toast.LENGTH_SHORT).show()
    }

    private fun showDialog(isTrue: Boolean) {
        if (loadingDialog == null) {
            loadingDialog = Dialog(this)
            loadingDialog!!.setContentView(R.layout.dialog_loading)
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