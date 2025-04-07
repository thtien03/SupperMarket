package com.example.marketgreenapp.activity_register

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.marketgreenapp.R
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.FunctionGlobal.isPasswordValid
import com.example.marketgreenapp.constant.FunctionGlobal.isPhoneNumberValid
import com.example.marketgreenapp.constant.FunctionGlobal.validateUsername
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivitySignUpBinding
import com.example.marketgreenapp.model.User
import com.example.marketgreenapp.references.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class SignUpActivity : AppCompatActivity() {
    private var signUpActivityBinding: ActivitySignUpBinding? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var loadingDialog: Dialog? = null
    private var isRole: String = "shop"

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (signUpActivityBinding == null) {
            signUpActivityBinding = ActivitySignUpBinding.inflate(layoutInflater)
        }

        setContentView(signUpActivityBinding!!.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        signUpActivityBinding!!.tvShop.setOnClickListener {
            isRole = "shop"
            signUpActivityBinding!!.tvShop.background =
                resources.getDrawable(R.drawable.switch_trcks, null)
            signUpActivityBinding!!.textInputLayoutCodeAdmin.visibility = View.INVISIBLE
            signUpActivityBinding!!.tvAdmin.background = null
            signUpActivityBinding!!.tvShop.setTextColor(resources.getColor(R.color.white, null))
            signUpActivityBinding!!.tvAdmin.setTextColor(resources.getColor(R.color.black, null))
            resetData()
        }
        signUpActivityBinding!!.tvAdmin.setOnClickListener {
            isRole = "admin"
            signUpActivityBinding!!.tvAdmin.background =
                resources.getDrawable(R.drawable.switch_trcks, null)
            signUpActivityBinding!!.textInputLayoutCodeAdmin.visibility = View.VISIBLE
            signUpActivityBinding!!.tvShop.background = null
            signUpActivityBinding!!.tvAdmin.setTextColor(resources.getColor(R.color.white, null))
            signUpActivityBinding!!.tvShop.setTextColor(resources.getColor(R.color.black, null))
            resetData()
        }

        signUpActivityBinding!!.imgBack.setOnClickListener {
            backSignInActivity()
        }
        signUpActivityBinding!!.layoutSignIn.setOnClickListener {
            backSignInActivity()
        }
        signUpActivityBinding!!.textInputLayoutDateOfBirth.setEndIconOnClickListener {
            showDatePicker()
        }
        signUpActivityBinding!!.edtDateOfBirth.setOnClickListener {
            showDatePicker()
        }
        signUpActivityBinding!!.btnSignUp.setOnClickListener {
            registerAccount()
        }
    }

    private fun resetData() {
        signUpActivityBinding?.let {
            it.edtEmail.setText("")
            it.edtPassword.setText("")
            it.edtConfirmPassword.setText("")
            it.edtUserName.setText("")
            it.edtPhone.setText("")
            it.edtDateOfBirth.setText("")
            it.edtCodeAdmin.setText("")
        }
    }

    private fun backSignInActivity() {
        TransitionHelper.navigateWithTransition(
            this@SignUpActivity,
            SignInActivity::class.java,
            signUpActivityBinding!!.layoutSignUpRoot,
            "transition_register",
            R.anim.slide_in_left,
            R.anim.slide_no_anim
        )
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)


        val maxDateCalendar = Calendar.getInstance()
        maxDateCalendar.set(year, month, day)
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = "${dayOfMonth}/${month + 1}/$year"
                signUpActivityBinding!!.edtDateOfBirth.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis - 1
        datePickerDialog.show()
    }

    private fun checkData(
        email: String,
        password: String,
        confirmPassword: String,
        username: String,
        phone: String,
        birthday: String,
        strCode: String
    ): Boolean {
        if (TextUtils.isEmpty(email)) {
            signUpActivityBinding?.edtEmail?.error = "*Vui lòng nhập email"
            return false
        } else if (TextUtils.isEmpty(password)) {
            signUpActivityBinding?.edtPassword?.error = "*Vui lòng nhập password"
            return false
        } else if (TextUtils.isEmpty(confirmPassword)) {
            signUpActivityBinding?.edtConfirmPassword?.error = "*Vui lòng nhập lại password"
            return false
        } else if (TextUtils.isEmpty(username)) {
            signUpActivityBinding?.edtConfirmPassword?.error = "*Vui lòng nhập họ tên"
            return false
        } else if (TextUtils.isEmpty(phone)) {
            signUpActivityBinding?.edtConfirmPassword?.error = "*Vui lòng nhập số điện thoại"
            return false
        } else if (TextUtils.isEmpty(birthday)) {
            signUpActivityBinding?.edtConfirmPassword?.error = "*Vui lòng chọn ngày sinh"
            return false
        } else if ((isRole == "admin") && TextUtils.isEmpty(strCode)) {
            signUpActivityBinding?.edtCodeAdmin?.error = "*Nhập mã quản trị viên"
            return false
        }
        return true
    }

    private fun AppCompatActivity.showMessage(strMessage: String) {
        Toast.makeText(this@SignUpActivity, strMessage, Toast.LENGTH_SHORT).show()
    }


    private suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            val querySnapshot = firebaseFirestore.collection("Users")
                .whereEqualTo(ConstantKey.CLOUD_KEY_EMAIL, email)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showMessage("Lỗi kết nối, vui lòng thử lại sau.")
            }
            false
        }
    }

    private fun registerAccount() {
        val strEmail: String = signUpActivityBinding!!.edtEmail.text.toString().trim { it <= ' ' }
        val strPassword: String =
            signUpActivityBinding!!.edtPassword.text.toString().trim { it <= ' ' }
        val strConfirmPassword: String =
            signUpActivityBinding!!.edtConfirmPassword.text.toString().trim { it <= ' ' }
        val strPhone: String = signUpActivityBinding!!.edtPhone.text.toString().trim { it <= ' ' }
        val strUsername: String =
            signUpActivityBinding!!.edtUserName.text.toString().trim { it <= ' ' }
        val strBirthday: String =
            signUpActivityBinding!!.edtDateOfBirth.text.toString().trim { it <= ' ' }
        val strCode: String =
            signUpActivityBinding?.edtCodeAdmin?.text.toString().trim { it <= ' ' }
        if (checkData(
                strEmail,
                strPassword,
                strConfirmPassword,
                strUsername,
                strPhone,
                strBirthday,
                strCode
            )
        ) {
            if (FunctionGlobal.isEmailValid2(strEmail)) {
                if (isPasswordValid(strPassword)) {
                    if (strPassword == strConfirmPassword) {
                        if (isPhoneNumberValid(strPhone)) {
                            if (validateUsername(strUsername)) {
                                lifecycleScope.launch {
                                    val isRegistered = withContext(Dispatchers.IO) {
                                        isEmailRegistered(strEmail)
                                    }
                                    if (isRegistered) {
                                        showMessage("Email này đã được sử dụng, vui lòng chọn email khác.")
                                    } else {
                                        withContext(Dispatchers.Main)
                                        {
                                            if (isRole == "shop") {
                                                registerUser(
                                                    strEmail,
                                                    strPassword,
                                                    strUsername,
                                                    strPhone,
                                                    strBirthday,
                                                    isRole
                                                )
                                                return@withContext

                                            } else {
                                                if (strCode.isNotEmpty() && strCode.trim() == ConstantKey.KEY_CODE_ADMIN) {
                                                    registerUser(
                                                        strEmail,
                                                        strPassword,
                                                        strUsername,
                                                        strPhone,
                                                        strBirthday,
                                                        isRole
                                                    )
                                                    return@withContext
                                                } else {
                                                    //showMessage("Xác nhận mã admin không hợp lệ")
                                                    showNotificationDialog(Gravity.CENTER)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else showMessage("Họ tên không hợp lệ")
                        } else showMessage("Số điện thoại không hợp lệ")
                    } else showMessage("Mật khẩu không tương ứng")
                } else showMessage("Mật khẩu có ít nhất 8 kí tự, bao gồm chữ số, chữ cái in hoa,in thường,và ít nhất 1 kí tự đặc biệt")
            } else showMessage("Email không hợp lệ")
        }
    }

    private fun registerUser(
        email: String,
        password: String,
        username: String,
        phone: String,
        birthday: String,
        isRole: String
    ) {
        showDialog(true)
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            showDialog(false)
            if (it.isSuccessful) {
                val user: FirebaseUser? = firebaseAuth.currentUser
                if (user != null) {
                    val salt = FunctionGlobal.generateSalt()
                    val hashPassword = FunctionGlobal.hashPasswordWithSalt(password, salt)
                    val df = firebaseFirestore.collection("Users").document(user.uid)
                    val mapUserInfo: MutableMap<String, Any> = hashMapOf()
                    mapUserInfo[ConstantKey.CLOUD_KEY_USERNAME] = username
                    mapUserInfo[ConstantKey.CLOUD_KEY_EMAIL] = email
                    mapUserInfo[ConstantKey.CLOUD_KEY_IS_UID] = user.uid
                    mapUserInfo[ConstantKey.CLOUD_KEY_PHONE] = phone
                    mapUserInfo[ConstantKey.CLOUD_KEY_BIRTHDAY] = birthday
                    mapUserInfo[ConstantKey.CLOUD_KEY_PASSWORD] = hashPassword
                    mapUserInfo[ConstantKey.CLOUD_KEY_IS_ROLE] = isRole
                    mapUserInfo[ConstantKey.CLOUD_KEY_IS_SALT] = salt
                    mapUserInfo[ConstantKey.CLOUD_KEY_IS_ENABLE] = false
                    df.set(mapUserInfo)
                    val newUser = User(
                        user.uid,
                        email,
                        password,
                        username,
                        phone,
                        birthday,
                        false,
                        isRole
                    )
                    DataStoreManager.setUser(newUser)
                    FunctionGlobal.gotoMainActivity(this@SignUpActivity)
                }
            } else {
                showMessage("Đăng ký không thành công, vui lòng thử lại!")
            }
        }
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
    @SuppressLint("SetTextI18n")
    private fun showNotificationDialog(gravity: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_item_dialog_warning)

        val btnAccept: Button = dialog.findViewById(R.id.btn_accept)
        val txtContent: TextView = dialog.findViewById(R.id.txt_content)
        txtContent.text =
            "Xác nhận mã admin không hợp lệ!"
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

    override fun onPause() {
        super.onPause()
        loadingDialog?.dismiss()
    }

}