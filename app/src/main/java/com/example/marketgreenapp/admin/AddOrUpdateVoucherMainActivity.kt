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
import com.example.marketgreenapp.databinding.ActivityAddOrUpdateVoucherMainBinding
import com.example.marketgreenapp.model.Voucher
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class AddOrUpdateVoucherMainActivity : AppCompatActivity() {
    private var addOrUpdateVoucherBinding: ActivityAddOrUpdateVoucherMainBinding? = null
    private var mVoucher: Voucher? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (addOrUpdateVoucherBinding == null)
            addOrUpdateVoucherBinding =
                ActivityAddOrUpdateVoucherMainBinding.inflate(layoutInflater)
        setContentView(addOrUpdateVoucherBinding!!.root)


        getDataFromBundle()
        addOrUpdateVoucherBinding?.imgBack?.setOnClickListener {
            this.finish()
        }
        addOrUpdateVoucherBinding?.textInputLayoutDateValid?.setEndIconOnClickListener {
            showDatePicker()
        }
        addOrUpdateVoucherBinding?.edtValid?.setOnClickListener {
            showDatePicker()
        }
        addOrUpdateVoucherBinding?.btnAddOrEdit?.setOnClickListener {
            addOrUpdateVoucher()
        }
    }
    private fun getDataFromBundle()
    {
        val bundle: Bundle = intent.extras ?: return
        mVoucher = bundle.getSerializable(ConstantKey.KEY_VOUCHER_ADMIN) as Voucher?
        initViews()
    }
    @SuppressLint("SetTextI18n")
    private fun  initViews()
    {
        if(mVoucher != null)
        {
            addOrUpdateVoucherBinding!!.apply {
                this.tvTitle.text = "Chỉnh sửa voucher"
                this.edtNameVoucher.setText(mVoucher?.title)
                this.edtContentRequiredVoucher.setText(mVoucher?.content.toString())
                this.edtValid.setText(mVoucher?.date_valid.toString())
            }
        }
        else
        {
            addOrUpdateVoucherBinding!!.tvTitle.text="Thêm voucher khuyến mãi"
        }
    }

    private fun addOrUpdateVoucher() {
        var nameVoucher: String? = null
        var contentRequiredVoucher: String? = null
        var imageVoucher: String? = null
        var dateValidVoucher: String? = null
        addOrUpdateVoucherBinding?.let { binding ->
            nameVoucher = binding.edtNameVoucher.text.toString().trim { it <= ' ' }
            contentRequiredVoucher =
                binding.edtContentRequiredVoucher.text.toString().trim { it <= ' ' }
            imageVoucher = binding.edtLinkImageVoucher.text.toString().trim { it <= ' ' }
            dateValidVoucher = binding.edtValid.text.toString().trim { it <= ' ' }
        }
        if (checkData(nameVoucher, contentRequiredVoucher, imageVoucher, dateValidVoucher)) {
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.add_or_update_voucher_2))
            addOrUpdateVoucherBinding?.btnAddOrEdit?.backgroundTintList = colorStateList
            lifecycleScope.launch(Dispatchers.IO){
                try {
                    if(mVoucher != null)
                    {
                        val map:MutableMap<String,Any> = HashMap()
                        map["title"] = nameVoucher.toString()
                        map["content"] = contentRequiredVoucher.toString()
                        map["date_valid"] = dateValidVoucher.toString()
                        MarketGreenFirebaseApp[this@AddOrUpdateVoucherMainActivity]
                            .getDataVoucherFromFirebaseDatabaseReference().child(mVoucher!!.id.toString()).updateChildren(map).await()
                        withContext(Dispatchers.Main)
                        {
                            resetData()
                            Toast.makeText(this@AddOrUpdateVoucherMainActivity,
                                "Cập nhật voucher thành công",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    else
                    {
                        val voucherId = System.currentTimeMillis()
                        val voucher = Voucher(voucherId,nameVoucher,contentRequiredVoucher,imageVoucher,dateValidVoucher)
                        MarketGreenFirebaseApp[this@AddOrUpdateVoucherMainActivity]
                            .getDataVoucherFromFirebaseDatabaseReference().child(voucherId.toString()).setValue(voucher).await()
                        withContext(Dispatchers.Main)
                        {
                            resetData()
                            Toast.makeText(this@AddOrUpdateVoucherMainActivity,
                                "Thêm voucher thành công",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
                catch (e:Exception)
                {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(this@AddOrUpdateVoucherMainActivity,
                        "error ${e.message}",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else
        {
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.divider_color))
            addOrUpdateVoucherBinding?.btnAddOrEdit?.backgroundTintList = colorStateList
        }
    }

    private fun resetData()
    {
        addOrUpdateVoucherBinding?.let {binding ->
            binding.edtNameVoucher.setText("")
            binding.edtContentRequiredVoucher.setText("")
            binding.edtValid.setText("")
        }
    }
    private fun checkData(
        nameVoucher: String?,
        contentRequiredVoucher: String?,
        imageVoucher: String?,
        dateValidVoucher: String?
    ): Boolean {
        if (TextUtils.isEmpty(nameVoucher)) {
            addOrUpdateVoucherBinding?.edtNameVoucher?.error = "*Vui lòng nhập tên voucher"
            return false
        } else if (TextUtils.isEmpty(contentRequiredVoucher)) {
            addOrUpdateVoucherBinding?.edtContentRequiredVoucher?.error = "*nội dung áp dụng voucher"
            return false
        } else if (TextUtils.isEmpty(imageVoucher)) {
            addOrUpdateVoucherBinding?.edtLinkImageVoucher?.error = "*link ảnh voucher"
            return false
        } else if (TextUtils.isEmpty(dateValidVoucher)) {
            addOrUpdateVoucherBinding?.edtValid?.error = "*Ngày có hiệu lực"
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
                addOrUpdateVoucherBinding!!.edtValid.setText(selectedDate)
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