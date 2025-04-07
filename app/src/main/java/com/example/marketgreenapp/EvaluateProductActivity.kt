package com.example.marketgreenapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.ActivityEvaluateProductBinding
import com.example.marketgreenapp.model.Evaluate
import com.example.marketgreenapp.model.ProductStatus
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EvaluateProductActivity : AppCompatActivity() {
    private lateinit var evaluateProductBinding: ActivityEvaluateProductBinding
    private var mProductStatus: ProductStatus? = null
    private var nameEvaluate: String = ""
    private var loadingDialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        evaluateProductBinding = ActivityEvaluateProductBinding.inflate(layoutInflater)
        setContentView(evaluateProductBinding.root)

        getDataIntent()
        solutionEvaluate()

        evaluateProductBinding.imgBack.setOnClickListener {
            this.finish()
        }
    }

    private fun getDataIntent() {
        val extras = intent.extras
        if (extras != null) {
            mProductStatus = extras.getSerializable(ConstantKey.KEY_PRODUCT) as ProductStatus?
            mProductStatus?.let {
                bindData()
            }
        }
    }

    private fun bindData() {
        GlideImageURL.loadImageURL(mProductStatus!!.imagePrimary, evaluateProductBinding.imgProduct)
        evaluateProductBinding.txtTitleProduct.text = mProductStatus!!.titleProduct.toString()
        evaluateProductBinding.txtPrice.text =
            mProductStatus!!.productDetail!!.priceProduct.toString()
    }

    private fun solutionEvaluate() {
        var numberStart = 0f
        evaluateProductBinding.ratingbar.setOnRatingBarChangeListener { _, rating, _ ->
            numberStart = rating
        }
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val user = DataStoreManager.getUser()
        if (user != null) {
            nameEvaluate = user.fullname.toString()
        }
        val evaluateId = System.currentTimeMillis()
        evaluateProductBinding.btnEvaluate.setOnClickListener {
            showDialog(true)
            val messageEvaluate: String =
                evaluateProductBinding.edtInputEvaluate.text.toString().trim()
            if (checkData(messageEvaluate)) {
                val evaluate =
                    Evaluate(
                        evaluateId,
                        messageEvaluate,
                        nameEvaluate,
                        numberStart,
                        currentDate.format(formatter)
                    )
                lifecycleScope.launch(Dispatchers.IO) {
                    val evaluatedRef =
                        MarketGreenFirebaseApp[this@EvaluateProductActivity].getDataEvaluateProductFromFirebaseDatabaseReference()
                            .child(
                                mProductStatus!!.productId.toString()
                            ).child("evaluates")
                    evaluatedRef.child(evaluateId.toString()).setValue(evaluate).await()
                    withContext(Dispatchers.Main)
                    {
                        showDialog(false)
                        Toast.makeText(
                            this@EvaluateProductActivity,
                            "Đánh giá đã được gửi!",
                            Toast.LENGTH_SHORT
                        ).show()
                        evaluateProductBinding.edtInputEvaluate.setText("")
                        evaluateProductBinding.ratingbar.rating = 0f

                        this@EvaluateProductActivity.finish()
                    }
                }
            }
        }
    }

    private fun checkData(messageEvaluate: String): Boolean {
        if (TextUtils.isEmpty(messageEvaluate)) {
            Toast.makeText(this, "Bạn chưa viết đánh giá", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
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