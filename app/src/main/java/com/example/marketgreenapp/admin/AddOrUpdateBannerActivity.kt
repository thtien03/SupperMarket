package com.example.marketgreenapp.admin

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.marketgreenapp.R
import com.example.marketgreenapp.databinding.ActivityAddOrUpdateBannerBinding
import com.example.marketgreenapp.model.Banner
import com.example.marketgreenapp.model.Voucher
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.HashMap

class AddOrUpdateBannerActivity : AppCompatActivity() {
    private var addOrUpdateBinding: ActivityAddOrUpdateBannerBinding? = null
    private var mBanner: Banner? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (addOrUpdateBinding == null)
            addOrUpdateBinding = ActivityAddOrUpdateBannerBinding.inflate(layoutInflater)
        setContentView(addOrUpdateBinding!!.root)
        addOrUpdateBinding?.btnAddOrEdit?.setOnClickListener {
            solutionAddOrEditBanner()
        }
    }

    private fun solutionAddOrEditBanner() {
        var title: String? = null
        var description: String? = null
        var imageUrl: String? = null
        var linkUrl: String? = null
        addOrUpdateBinding?.let { binding ->
            title = binding.edtTitleBanner.text.toString().trim { it <= ' ' }
            description =
                binding.edtContentBanner.text.toString().trim { it <= ' ' }
            imageUrl = binding.edtImgBanner.text.toString().trim { it <= ' ' }
            linkUrl = binding.edtUrlLink.text.toString().trim { it <= ' ' }
        }
        if (checkData(title, description, imageUrl, linkUrl)) {
            val colorStateList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this,
                    R.color.add_or_update_voucher_2
                )
            )
            addOrUpdateBinding?.btnAddOrEdit?.backgroundTintList = colorStateList
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    /*if(mBanner != null)
                    {
                        /*val map:MutableMap<String,Any> = HashMap()
                        map["title"] = nameVoucher.toString()
                        map["content"] = contentRequiredVoucher.toString()
                        map["date_valid"] = dateValidVoucher.toString()
                        MarketGreenFirebaseApp[this@AddOrUpdateVoucherMainActivity]
                            .getDataVoucherFromFirebaseDatabaseReference().child(mVoucher!!.id.toString()).updateChildren(map).await()
                        withContext(Dispatchers.Main)
                        {
                            resetData()
                            Toast.makeText(this@AddOrUpdateVoucherMainActivity,
                                "Cập nhật voucher thành công", Toast.LENGTH_SHORT).show()
                            finish()
                        }*/
                    }
                    else
                    {*/
                    val bannerId = System.currentTimeMillis()
                    val banner = Banner(bannerId, title, description, imageUrl, linkUrl)
                    MarketGreenFirebaseApp[this@AddOrUpdateBannerActivity]
                        .getDataBannerFromFirebaseDatabaseReference().child(bannerId.toString())
                        .setValue(banner).await()
                    withContext(Dispatchers.Main)
                    {
                        resetData()
                        Toast.makeText(
                            this@AddOrUpdateBannerActivity,
                            "Thêm voucher thành công", Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    //}
                } catch (e: Exception) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(
                            this@AddOrUpdateBannerActivity,
                            "error ${e.message}", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            val colorStateList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.divider_color))
            addOrUpdateBinding?.btnAddOrEdit?.backgroundTintList = colorStateList
        }
    }

    private fun resetData() {
        addOrUpdateBinding?.let {
            it.edtTitleBanner.setText("")
            it.edtContentBanner.setText("")
            it.edtImgBanner.setText("")
            it.edtUrlLink.setText("")
        }
    }

    private fun checkData(
        title: String?,
        description: String?,
        imageUrl: String?,
        linkUrl: String?
    ): Boolean {
        if (TextUtils.isEmpty(title)) {
            addOrUpdateBinding?.edtTitleBanner?.error = "*Vui lòng nhập "
            return false
        } else if (TextUtils.isEmpty(description)) {
            addOrUpdateBinding?.edtContentBanner?.error = "*Vui lòng nhập"
            return false
        } else if (TextUtils.isEmpty(imageUrl)) {
            addOrUpdateBinding?.edtImgBanner?.error = "*Link ảnh banner"
            return false
        } else if (TextUtils.isEmpty(linkUrl)) {
            addOrUpdateBinding?.edtUrlLink?.error = "*Link url"
            return false
        }
        return true
    }
}