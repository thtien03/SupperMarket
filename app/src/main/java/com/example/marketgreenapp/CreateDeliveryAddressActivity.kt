package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityCreateDeliveryAddressBinding
import com.example.marketgreenapp.model.DeliveryAddress
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CreateDeliveryAddressActivity : AppCompatActivity() {
    private var deliveryAddressBinding: ActivityCreateDeliveryAddressBinding? = null
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var firebase: FirebaseDatabase
    private var mDeliveryAddress:DeliveryAddress? = null
    private var defaultDelivery = false
    private var loadingDialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (deliveryAddressBinding == null)
            deliveryAddressBinding = ActivityCreateDeliveryAddressBinding.inflate(layoutInflater)
        setContentView(deliveryAddressBinding!!.root)

        deliveryAddressBinding!!.switchSetDefault.setOnCheckedChangeListener { buttonView, isChecked ->
            //defaultDelivery = buttonView!!.isChecked
            if (!isChecked) {
                if (mDeliveryAddress?.isDefault == true) {
                    Toast.makeText(
                        this,
                        "Cần phải có ít nhất một địa chỉ được đặt mặc định!",
                        Toast.LENGTH_SHORT
                    ).show()
                    deliveryAddressBinding!!.switchSetDefault.isChecked = true
                }
            } else {
                defaultDelivery = true
            }
        }
        getDataFromBundle()
        deliveryAddressBinding!!.imgBack.setOnClickListener {
            finish()
        }
        deliveryAddressBinding!!.btnDone.setOnClickListener {
            solutionDoneDeliveryAddress()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDataFromBundle()
    {
        val bundle: Bundle = intent.extras ?: return
        mDeliveryAddress = bundle.getSerializable(ConstantKey.KEY_DELIVERY_UPDATE) as DeliveryAddress?
        if(mDeliveryAddress != null)
        {
            initViews()
        }
        else deliveryAddressBinding?.txtTitle?.text = "Thêm địa chỉ nhận hàng"
    }
    private fun initViews()
    {
        deliveryAddressBinding?.let { delivery ->
            delivery.txtTitle.text = "Cập nhật địa chỉ nhận hàng"
            delivery.edtNameOrder.setText(mDeliveryAddress?.name_receive.toString().trim { it <= ' ' })
            delivery.edtPhoneOrder.setText(mDeliveryAddress?.phone.toString().trim { it <= ' ' })
            val address = mDeliveryAddress?.address.toString()
            val parts = address.split(",").map { it.trim() }
            delivery.edtCity.setText(parts[3])
            delivery.edtDistrict.setText(parts[2])
            delivery.edtWard.setText(parts[1])
            delivery.edtStreetName.setText(parts[0])

            delivery.switchSetDefault.isChecked = mDeliveryAddress!!.isDefault
        }
    }
    private fun solutionDoneDeliveryAddress() {
        val nameReceive = deliveryAddressBinding?.edtNameOrder?.text.toString().trim { it <= ' ' }
        val phone = deliveryAddressBinding?.edtPhoneOrder?.text.toString().trim { it <= ' ' }
        val city = deliveryAddressBinding?.edtCity?.text.toString().trim { it <= ' ' }
        val district = deliveryAddressBinding?.edtDistrict?.text.toString().trim { it <= ' ' }
        val ward = deliveryAddressBinding?.edtWard?.text.toString().trim { it <= ' ' }
        val streetName = deliveryAddressBinding?.edtStreetName?.text.toString().trim { it <= ' ' }

        val id = System.currentTimeMillis()
        val address = "${streetName},${ward},${district},${city}"
        if (checkDataInput(nameReceive, phone, city, district, ward, streetName)) {
            if (FunctionGlobal.isPhoneNumberValid(phone)) {
                val delivery: DeliveryAddress = if (defaultDelivery)
                    DeliveryAddress(id, nameReceive, phone, address, true)
                else
                    DeliveryAddress(id, nameReceive, phone, address, false)
                val userCurrent = DataStoreManager.getUser()
                showDialog(true)
                if (userCurrent != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val dataSnapshot = MarketGreenFirebaseApp[this@CreateDeliveryAddressActivity]
                                .getDataDeliveryUserFromFirebaseDatabaseReference()
                                .child(userCurrent.uid.toString()).get().await()
                            val updates = mutableMapOf<String, Any>()
                            for (dataSnapshotChildren in dataSnapshot.children) {
                                val addressChildren: DeliveryAddress? = dataSnapshotChildren.getValue(DeliveryAddress::class.java)
                                addressChildren?.let {
                                    if (it.id != mDeliveryAddress!!.id && defaultDelivery) {
                                        updates["${dataSnapshotChildren.key}/default"] = false
                                    }
                                }
                            }
                            MarketGreenFirebaseApp[this@CreateDeliveryAddressActivity]
                                .getDataDeliveryUserFromFirebaseDatabaseReference()
                                .child(userCurrent.uid.toString())
                                .updateChildren(updates).await()
                            if(mDeliveryAddress != null)
                            {
                                delivery.id = mDeliveryAddress!!.id
                                val map:MutableMap<String,Any> = HashMap()
                                map["address"] = address
                                map["default"] = defaultDelivery
                                map["name_receive"] = nameReceive
                                map["phone"] = phone

                                MarketGreenFirebaseApp[this@CreateDeliveryAddressActivity].getDataDeliveryUserFromFirebaseDatabaseReference()
                                    .child(userCurrent.uid.toString()).child(mDeliveryAddress!!.id.toString())
                                    .updateChildren(map).await()
                                withContext(Dispatchers.Main)
                                {
                                    showDialog(false)
                                    resetData()
                                    Toast.makeText(
                                        this@CreateDeliveryAddressActivity,
                                        "Cập nhật địa chỉ nhận hàng thành công",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val resultIntent = Intent()
                                    resultIntent.putExtra(ConstantKey.KEY_DELIVERY, delivery)
                                    resultIntent.putExtra(ConstantKey.KEY_ACTION_DELIVERY, ConstantKey.VALUE_EDIT_DELIVERY)
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                }
                            }
                            else
                            {
                                MarketGreenFirebaseApp[this@CreateDeliveryAddressActivity].getDataDeliveryUserFromFirebaseDatabaseReference()
                                    .child(userCurrent.uid.toString()).child(id.toString())
                                    .setValue(delivery).await()
                                withContext(Dispatchers.Main)
                                {
                                    showDialog(false)
                                    resetData()
                                    Toast.makeText(
                                        this@CreateDeliveryAddressActivity,
                                        "Thêm địa chỉ nhận hàng thành công",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val resultIntent = Intent()
                                    resultIntent.putExtra(ConstantKey.KEY_DELIVERY, delivery)
                                    resultIntent.putExtra(ConstantKey.KEY_ACTION_DELIVERY, ConstantKey.VALUE_ADD_DELIVERY)
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                }
                            }
                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main)
                            {
                                Toast.makeText(
                                    this@CreateDeliveryAddressActivity,
                                    "Thêm thất bại ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                println("KAKAKA ${e.message}")
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@CreateDeliveryAddressActivity,
                        "thêm thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@CreateDeliveryAddressActivity,
                    "Số điện thoại không hợp lệ!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun resetData() {
        deliveryAddressBinding?.let {
            it.edtNameOrder.setText("")
            it.edtPhoneOrder.setText("")
            it.edtCity.setText("")
            it.edtDistrict.setText("")
            it.edtWard.setText("")
            it.edtStreetName.setText("")
        }
    }

    private fun checkDataInput(
        nameReceive: String,
        phone: String,
        city: String,
        district: String,
        ward: String,
        streetName: String
    ): Boolean {
        if (TextUtils.isEmpty(nameReceive)) {
            deliveryAddressBinding?.edtNameOrder?.error = "*Tên không được bỏ trống"
            return false
        } else if (TextUtils.isEmpty(phone)) {
            deliveryAddressBinding?.edtPhoneOrder?.error = "*Số điện thoại không được bỏ trống"
        } else if (TextUtils.isEmpty(city)) {
            deliveryAddressBinding?.edtCity?.error = "*không được bỏ trống"
            return false
        } else if (TextUtils.isEmpty(district)) {
            deliveryAddressBinding?.edtDistrict?.error = "*không được bỏ trống"
            return false
        } else if (TextUtils.isEmpty(ward)) {
            deliveryAddressBinding?.edtWard?.error = "*không được bỏ trống"
            return false
        } else if (TextUtils.isEmpty(streetName)) {
            deliveryAddressBinding?.edtStreetName?.error = "*không được bỏ trống"
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