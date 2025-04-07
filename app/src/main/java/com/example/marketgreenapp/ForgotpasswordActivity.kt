package com.example.marketgreenapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.marketgreenapp.activity_register.SignInActivity
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityForgotpasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotpasswordActivity : AppCompatActivity() {
    private var forgotBinding: ActivityForgotpasswordBinding? = null
    private lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(forgotBinding == null)
        {
            forgotBinding = ActivityForgotpasswordBinding.inflate(layoutInflater)
        }
        setContentView(forgotBinding!!.root)
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_loading)

        forgotBinding!!.imgBackSignIn.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                this@ForgotpasswordActivity,
                SignInActivity::class.java,
                forgotBinding!!.layoutForgot,
                "transition_register",
                R.anim.slide_in_left,
                R.anim.slide_no_anim
            )
            finishAffinity()
        }
        forgotBinding!!.btnResetPassword.setOnClickListener {
            solutionForgotPassword()
        }
    }
    private fun checkData(email:String):Boolean
    {
        if(TextUtils.isEmpty(email))
        {
            forgotBinding!!.edtEmailForgotPassword.error = "vui lòng nhập email của bạn"
            return false
        }
        else if(!FunctionGlobal.isEmailValid2(email))
        {
            forgotBinding!!.edtEmailForgotPassword.error = "Email không hợp lệ!"
            return false
        }
        return true
    }
    private fun AppCompatActivity.showMessage(message: String) {
        Toast.makeText(this@ForgotpasswordActivity, message, Toast.LENGTH_SHORT).show()
    }
    private fun solutionForgotPassword()
    {
        val strEmail = forgotBinding!!.edtEmailForgotPassword.text.toString().trim()
        if(checkData(strEmail))
        {
            resetPassword(strEmail)
        }
    }
    private fun  resetPassword(email:String)
    {
        showDialog(true)
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                showDialog(false)
                if(it.isSuccessful)
                {
                    showMessage("Đặt lại mật khẩu thành công , vui lòng kiểm tra email của bạn")
                    forgotBinding!!.edtEmailForgotPassword.setText("")
                }
                else showMessage("reset password không thành công")
            }
    }
    private fun showDialog(isTrue:Boolean)
    {
        if(isTrue)
            dialog.show()
        else
            dialog.dismiss()
    }
}