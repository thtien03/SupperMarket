package com.example.marketgreenapp.admin

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.marketgreenapp.R
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.databinding.ActivityAddOrUpdateCodeVoucherBinding
import com.example.marketgreenapp.databinding.ActivityCodeVoucherBinding
import com.example.marketgreenapp.model.CodeVoucher
import com.example.marketgreenapp.model.Voucher
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class AddOrUpdateCodeVoucherActivity : AppCompatActivity() {
    private var codeVoucherBinding:ActivityAddOrUpdateCodeVoucherBinding? = null
    private var mCodeVoucher:CodeVoucher? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(codeVoucherBinding == null)
            codeVoucherBinding = ActivityAddOrUpdateCodeVoucherBinding.inflate(layoutInflater)
        setContentView(codeVoucherBinding!!.root)

        getDataFromBundle()
        codeVoucherBinding?.imgBack?.setOnClickListener {
            this.finish()
        }
        codeVoucherBinding?.textInputLayoutDateValid?.setEndIconOnClickListener {
            showDatePicker()
        }
        codeVoucherBinding?.edtValid?.setOnClickListener {
            showDatePicker()
        }
        codeVoucherBinding?.btnAddOrEdit?.setOnClickListener {
            addOrUpdateVoucher()
        }
    }
    private fun getDataFromBundle()
    {
        val bundle: Bundle = intent.extras ?: return
        mCodeVoucher = bundle.getSerializable(ConstantKey.KEY_CODE_VOUCHER_ADMIN) as CodeVoucher?
        initViews()
    }
    @SuppressLint("SetTextI18n")
    private fun  initViews()
    {
        if(mCodeVoucher != null)
        {
            codeVoucherBinding!!.apply {
                this.tvTitle.text = "Chỉnh sửa code voucher"
                this.edtCodeVoucher.setText(mCodeVoucher?.code)
                this.edtCostCodeVoucher.setText(mCodeVoucher?.cost_code.toString())
                this.edtValid.setText(mCodeVoucher?.date_valid.toString())
            }
        }
        else
        {
            codeVoucherBinding!!.tvTitle.text="Thêm code voucher khuyến mãi"
        }
    }
    private fun addOrUpdateVoucher() {
        var codeVoucher: String? = null
        var costCode: String? = null
        var dateValidVoucher: String? = null
        codeVoucherBinding?.let { binding ->
            codeVoucher = binding.edtCodeVoucher.text.toString().trim { it <= ' ' }
            costCode =
                binding.edtCostCodeVoucher.text.toString().trim { it <= ' ' }.replace("k", "", ignoreCase = true)
            dateValidVoucher = binding.edtValid.text.toString().trim { it <= ' ' }
        }
        if (checkData(codeVoucher, codeVoucher, dateValidVoucher)) {
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.add_or_update_voucher_2))
            codeVoucherBinding?.btnAddOrEdit?.backgroundTintList = colorStateList
            lifecycleScope.launch(Dispatchers.IO){
                try {
                    if(mCodeVoucher != null)
                    {
                        val map:MutableMap<String,Any> = HashMap()
                        map["code"] = codeVoucher.toString()
                        map["cost_code"] = costCode!!.toLong()
                        map["date_valid"] = dateValidVoucher.toString()
                        MarketGreenFirebaseApp[this@AddOrUpdateCodeVoucherActivity]
                            .getDataCodeVoucherFromFirebaseDatabaseReference().child(mCodeVoucher!!.id.toString()).updateChildren(map).await()
                        withContext(Dispatchers.Main)
                        {
                            resetData()
                            Toast.makeText(this@AddOrUpdateCodeVoucherActivity,
                                "Cập nhật voucher thành công", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    else
                    {
                        val codevoucherId = System.currentTimeMillis()
                        val voucher = CodeVoucher(codevoucherId,codeVoucher,dateValidVoucher,costCode!!.toLong())
                        MarketGreenFirebaseApp[this@AddOrUpdateCodeVoucherActivity]
                            .getDataCodeVoucherFromFirebaseDatabaseReference().child(codevoucherId.toString()).setValue(voucher).await()
                        withContext(Dispatchers.Main)
                        {
                            resetData()
                            Toast.makeText(this@AddOrUpdateCodeVoucherActivity,
                                "Thêm voucher thành công", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
                catch (e:Exception)
                {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(this@AddOrUpdateCodeVoucherActivity,
                            "error ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else
        {
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.divider_color))
            codeVoucherBinding?.btnAddOrEdit?.backgroundTintList = colorStateList
        }
    }

    private fun resetData()
    {
        codeVoucherBinding?.let {binding ->
            binding.edtCodeVoucher.setText("")
            binding.edtCostCodeVoucher.setText("")
            binding.edtValid.setText("")
        }
    }
    private fun checkData(
        codeVoucher: String?,
        costCode: String?,
        dateValidVoucher: String?
    ): Boolean {
        if (TextUtils.isEmpty(codeVoucher)) {
            codeVoucherBinding?.edtCodeVoucher?.error = "*Vui lòng nhập code voucher"
            return false
        } else if (TextUtils.isEmpty(costCode)) {
            codeVoucherBinding?.edtCostCodeVoucher?.error = "*Giá voucher"
            return false
        }  else if (TextUtils.isEmpty(dateValidVoucher)) {
            codeVoucherBinding?.edtValid?.error = "*Ngày có hiệu lực"
            return false
        }
        return true
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = "${dayOfMonth}/${month + 1}/$year"
                codeVoucherBinding!!.edtValid.setText(selectedDate)
            },
            year,
            month,
            day
        )
        // Đặt minDate để không cho phép chọn ngày trong quá khứ
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }
}