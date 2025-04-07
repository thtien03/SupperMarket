package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.VoucherAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.databinding.ActivitySaleBinding
import com.example.marketgreenapp.listener.IOnClickItemVoucher
import com.example.marketgreenapp.model.CodeVoucher
import com.example.marketgreenapp.model.Voucher
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

class SaleActivity : AppCompatActivity() {
    private var saleBinding: ActivitySaleBinding? = null
    private var mListVoucher: MutableList<Voucher>? = null
    private var mCodeListVoucher: MutableList<CodeVoucher>? = null
    private lateinit var voucherAdapter: VoucherAdapter

    private var totalPrice: Long = 0
    private var flagCode = false
    private var mCodeVoucher: CodeVoucher? = null
    private var priceVoucher = 0
    private var priceCodeVoucher = 0

    private var mVoucherSelected: Voucher? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (saleBinding == null)
            saleBinding = ActivitySaleBinding.inflate(layoutInflater)
        setContentView(saleBinding!!.root)

        lifecycleScope.launch {
            getDataVoucherFB()
            getDataCodeVoucherFB()
            setVoucherToRecyclerView()
            getDataIntent()
        }
        saleBinding?.imgBack?.setOnClickListener {
            this.finish()
        }
        setVoucherToRecyclerView()
        saleBinding?.btnApply?.setOnClickListener {
            solutionCodeVoucher()
        }
        saleBinding?.btnDone?.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(
                ConstantKey.KEY_PRICE_SALE_VOUCHER,
                priceVoucher + priceCodeVoucher
            )
            resultIntent.putExtra(ConstantKey.KEY_VOUCHER_SELECTED, mVoucherSelected)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun getDataIntent() {
        val extras = intent.extras
        if (extras != null) {
            totalPrice = extras.getLong(ConstantKey.KEY_PRODUCT_TOTAL_PRICE)
            val selectedVoucher =
                extras.getSerializable(ConstantKey.KEY_VOUCHER_SELECTED) as Voucher?
            if (selectedVoucher != null) {
                mVoucherSelected = selectedVoucher
                voucherAdapter.setSelectedVoucher(selectedVoucher)
            }
        }
    }

    private suspend fun getDataCodeVoucherFB() {
        return withContext(Dispatchers.IO)
        {
            val snapshot = MarketGreenFirebaseApp[this@SaleActivity]
                .getDataCodeVoucherFromFirebaseDatabaseReference().get().await()
            if (mCodeListVoucher != null) mCodeListVoucher!!.clear()
            else mCodeListVoucher = mutableListOf()
            for (snapshotChildren in snapshot.children) {
                val voucherChildren: CodeVoucher? =
                    snapshotChildren.getValue(CodeVoucher::class.java)
                voucherChildren?.let {
                    mCodeListVoucher!!.add(0, it)
                }
            }
        }
    }

    private suspend fun getDataVoucherFB() {
        return withContext(Dispatchers.IO)
        {
            val snapshot = MarketGreenFirebaseApp[this@SaleActivity]
                .getDataVoucherFromFirebaseDatabaseReference().get().await()
            if (mListVoucher != null) mListVoucher!!.clear()
            else mListVoucher = mutableListOf()
            for (snapshotChildren in snapshot.children) {
                val voucherChildren: Voucher? = snapshotChildren.getValue(Voucher::class.java)
                voucherChildren?.let {
                    mListVoucher!!.add(0, it)
                }
            }
        }
    }

    private fun checkCodeVoucher(codeVoucher: String): Boolean {
        if (TextUtils.isEmpty(codeVoucher)) {
            Toast.makeText(this@SaleActivity, "hãy nhập mã voucher giảm giá", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun solutionCodeVoucher() {
        val codeVoucher = saleBinding?.edtCodeVoucher?.text.toString().trim { it <= ' ' }
        if (checkCodeVoucher(codeVoucher)) {
            for (item in mCodeListVoucher!!) {
                if (item.code.toString().trim() == codeVoucher) {
                    Toast.makeText(this@SaleActivity, item.cost_code.toString(), Toast.LENGTH_SHORT)
                        .show()
                    mCodeVoucher = item
                    flagCode = true
                    break
                } else {
                    flagCode = false
                }
            }
            if (!flagCode) {
                showNotificationDialog(Gravity.CENTER)
                saleBinding!!.edtCodeVoucher.setText("")
            } else {
                saleBinding!!.txtBottomVoucher.text = mCodeVoucher!!.code.toString()
                priceCodeVoucher = mCodeVoucher!!.cost_code.toInt()
                val formattedPrice =
                    NumberFormat.getNumberInstance(Locale.GERMANY)
                        .format(priceCodeVoucher + priceVoucher)
                saleBinding!!.txtBottomSale.text = "-${formattedPrice}.000VNĐ"
            }
        }
    }

    private fun setVoucherToRecyclerView() {
        voucherAdapter = VoucherAdapter(
            mListVoucher,
            object : IOnClickItemVoucher {
                @SuppressLint("SetTextI18n")
                override fun onClickItemVoucher(voucher: Voucher?) {

                    if (voucher == null) {
                        val formattedPrice =
                            NumberFormat.getNumberInstance(Locale.GERMANY).format(priceCodeVoucher)
                        saleBinding!!.txtBottomSale.text = "-${formattedPrice}.000VNĐ"
                    }
                    //vd voucher.content của tôi là áp dụng cho đơn hàng từ 300k đến 500k , hoặc áp dụng cho đơn hàng tối thiểu 100k
                    else {
                        val regex = "\\d+".toRegex()
                        val matches = regex.findAll(voucher.content.toString())
                        val matchesPrice = regex.findAll(voucher.title.toString())
                        val priceSale = matchesPrice.map { it.value.toInt() }.toList()
                        val numbers = matches.map { it.value.toInt() }.toList()
                        var isValid = false

                        if (numbers.isNotEmpty()) {
                            if (numbers.size == 1) {
                                isValid = totalPrice >= numbers[0]
                            } else if (numbers.size == 2) {
                                isValid = totalPrice >= numbers[0] && totalPrice <= numbers[1]
                            }
                        } else {
                            isValid = true
                        }
                        if (isValid) {
                            mVoucherSelected = voucher
                            updatePriceSale(priceSale[0])
                        } else {
                            Toast.makeText(
                                this@SaleActivity,
                                "Không đủ điều kiện để áp dụng voucher này",
                                Toast.LENGTH_SHORT
                            ).show()
                            voucherAdapter.setInvalidSelection()
                        }
                    }
                }
            })
        saleBinding?.rcvVoucher?.setHasFixedSize(false)
        saleBinding?.rcvVoucher?.layoutManager = LinearLayoutManager(this@SaleActivity)
        saleBinding?.rcvVoucher?.adapter = voucherAdapter

        voucherAdapter.setSelectedVoucher(mVoucherSelected)
    }

    @SuppressLint("SetTextI18n")
    private fun updatePriceSale(priceSale: Int) {
        val formattedPrice =
            NumberFormat.getNumberInstance(Locale.GERMANY).format(priceSale + priceCodeVoucher)
        priceVoucher = priceSale
        saleBinding!!.txtBottomSale.text = "-${formattedPrice}.000VNĐ"
    }

    @SuppressLint("SetTextI18n")
    private fun showNotificationDialog(gravity: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_item_dialog_warning)

        val btnAccept: Button = dialog.findViewById(R.id.btn_accept)
        val txtContent: TextView = dialog.findViewById(R.id.txt_content)
        txtContent.text =
            "Mã code voucher không hợp lệ"
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
}