package com.example.marketgreenapp.constant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract.Data
import android.util.Patterns
import android.view.inputmethod.InputMethodManager
import com.example.marketgreenapp.MainActivity
import com.example.marketgreenapp.R
import com.example.marketgreenapp.admin.AdminMainActivity
import com.example.marketgreenapp.model.AnswerHelp
import com.example.marketgreenapp.model.Category
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.shipper.TransactionProductActivity
import com.example.marketgreenapp.shop.ShopMainActivity
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

object FunctionGlobal {
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

     fun isEmailValid2(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@gmail\\.com$"
        return email.matches(emailRegex.toRegex())
    }

     fun isPasswordValid(password: String): Boolean {
        val passwordRegex =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z0-9@$!%*?&]{8,}$"
        return password.matches(passwordRegex.toRegex())
    }

     fun isPhoneNumberValid(phoneNumber: String): Boolean {
        val phoneNumberRegex = "^(03|05|07|08)\\d{8}$".toRegex()
        return phoneNumber.matches(phoneNumberRegex)
    }

    fun validateUsername(username: String): Boolean {
        val usernameRegex = "^[\\w\\s]{6,20}$".toRegex()
        return username.matches(usernameRegex)
    }
    fun  gotoMainActivity(context: Context?)
    {
        if(DataStoreManager.getUser() != null)
        {
            if(DataStoreManager.getUser()!!.role.equals("admin"))
                context!!.startActivity(Intent(context, AdminMainActivity::class.java))
            else if(DataStoreManager.getUser()!!.role.equals("user"))
                context!!.startActivity(Intent(context, MainActivity::class.java))
            else if(DataStoreManager.getUser()!!.role.equals("shop"))
                context!!.startActivity(Intent(context, ShopMainActivity::class.java))
            else
                context!!.startActivity(Intent(context,TransactionProductActivity::class.java))
        }
    }

    fun getTextSearch(input:String?):String
    {
        val nfdNormalizedString = Normalizer.normalize(input,Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCOMBINING_DIACRITICAL_MARKS}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
    }
    fun hideSoftKeyboard(activity:Activity?)
    {
        try {
            val inputMethodManager = activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken,0)
        }catch (ex:NullPointerException)
        {
            ex.printStackTrace()
        }
    }
    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }
    fun hashPasswordWithSalt(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = password + salt
        val hashedBytes = digest.digest(saltedPassword.toByteArray())
        return hashedBytes.joinToString("") { String.format("%02x", it) }
    }

    fun getDataAnswerHelp(context: Context?):MutableList<AnswerHelp>
    {
        return mutableListOf<AnswerHelp>().apply {
            this.add(AnswerHelp(1,"Tôi cần theo dõi quá trình đơn đặt hàng ở đâu ?",context?.getString(R.string.help1),R.drawable.img_help_detail))
            this.add(AnswerHelp(2,"Tại sao nhà bán hàng lại hủy đơn hàng của tôi ?",context?.getString(R.string.help1),R.drawable.img_help_detail))
            this.add(AnswerHelp(3,"Nền tảng có những khuyến mãi nào cho khách hàng ?",context?.getString(R.string.help1),R.drawable.img_help_detail))
            this.add(AnswerHelp(4,"Làm thế nào để tôi có thể khiếu nại nhà bán hàng ?",context?.getString(R.string.help1),R.drawable.img_help_detail))
            this.add(AnswerHelp(5,"Làm thế nào tôi có thể thay đổi mật khẩu ?",context?.getString(R.string.help1),R.drawable.img_help_detail))
            this.add(AnswerHelp(6,"Làm thế nào để tôi có thể yêu cầu trả hàng ?",context?.getString(R.string.help1),R.drawable.img_help_detail))
            this.add(AnswerHelp(7,"Tôi quên mật khẩu , tôi phải làm gì ?",context?.getString(R.string.help1),R.drawable.img_help_detail))
        }
    }

    fun getDataCategories(context: Context?):MutableList<Category>
    {
        return mutableListOf<Category>().apply {
            this.add(Category(1,context?.getString(R.string.books),R.drawable.img_books))
            this.add(Category(2,context?.getString(R.string.drink),R.drawable.img_drink))
            this.add(Category(3,context?.getString(R.string.smartphone),R.drawable.img_smart_phone))
            this.add(Category(7,context?.getString(R.string.clothes),R.drawable.img_27))
            this.add(Category(10,context?.getString(R.string.milk),R.drawable.img_17))
            this.add(Category(9,context?.getString(R.string.motorbike),R.drawable.img_16))
            this.add(Category(4,context?.getString(R.string.fruit),R.drawable.img_fruit))
            this.add(Category(8,context?.getString(R.string.shoes),R.drawable.img_18))
            this.add(Category(5,context?.getString(R.string.home_appliances),R.drawable.img_home_appliances))
            this.add(Category(6,context?.getString(R.string.laptop),R.drawable.img_laptop))
        }
    }

    val sizeShoes = listOf("37", "38", "39", "40","41")
    val sizeClothes = listOf("S", "M", "L", "XL","XXL")
    val colors = listOf("Hồng", "Đỏ", "Cam","Be","Vàng","Xanh lá","Nhiều màu","Xanh đậm","Xám","Nâu","Trắng","Đen")
}