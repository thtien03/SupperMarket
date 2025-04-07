package com.example.marketgreenapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.ProductTransactionAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityTransactionBinding
import com.example.marketgreenapp.model.HistoryOrderUser
import com.example.marketgreenapp.model.ProductChooseList

class TransactionActivity : AppCompatActivity() {
    private lateinit var transactionBinding: ActivityTransactionBinding
    private lateinit var productTransactionAdapter: ProductTransactionAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactionBinding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(transactionBinding.root)

        getDataIntent()
        transactionBinding.btnFollowOrderProduct.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                this@TransactionActivity,
                FollowOrderProductActivity::class.java,
                transactionBinding.layoutRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
            this.finish()
        }
        transactionBinding.imgBack.setOnClickListener {
            /* val intent = Intent(this, MainActivity::class.java)
             th1 intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             th2  intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
             startActivity(intent)
             finish()*/
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun getDataIntent() {
        val extras = intent.extras
        if (extras != null) {
            val mListProductPaymentSuccess =
                extras.getSerializable(ConstantKey.KEY_PRODUCT_PAYMENT_SUCCESS) as? HistoryOrderUser
            mListProductPaymentSuccess?.let {
                bindData(it)
            }
        }
    }

    private fun bindData(productOrder: HistoryOrderUser) {
        transactionBinding.txtDataCodeTransaction.text = productOrder.productOrderId.toString()
        transactionBinding.txtDataDateTransaction.text = productOrder.dateOrder.toString()
        transactionBinding.txtDataMethodPayment.text = productOrder.methodPayment.toString()
        transactionBinding.txtDataTotalPrice.text = productOrder.priceTotal.toString()

        productTransactionAdapter = ProductTransactionAdapter(productOrder.productOrders)
        transactionBinding.rcvProductOrder.setHasFixedSize(false)
        transactionBinding.rcvProductOrder.layoutManager = LinearLayoutManager(this)
        transactionBinding.rcvProductOrder.adapter = productTransactionAdapter
    }
}