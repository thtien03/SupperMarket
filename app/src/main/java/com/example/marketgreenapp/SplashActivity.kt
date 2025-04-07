package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.marketgreenapp.activity_register.SignInActivity
import com.example.marketgreenapp.activity_register.SignUpActivity
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivitySplashBinding
import com.example.marketgreenapp.references.DataStoreManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private var splashActivityMainBinding: ActivitySplashBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(splashActivityMainBinding == null)
        {
            splashActivityMainBinding = ActivitySplashBinding.inflate(layoutInflater)
        }
        setContentView(splashActivityMainBinding!!.root)


        splashActivityMainBinding!!.lottieSplash.visibility = View.VISIBLE
        splashActivityMainBinding!!.lottieSplash.playAnimation()
        splashActivityMainBinding!!.lottieSplash.progress =  0.5f

        Handler(Looper.getMainLooper()).postDelayed({
            splashActivityMainBinding!!.lottieSplash.visibility = View.GONE
            splashActivityMainBinding!!.constraintLayoutSplash.visibility = View.VISIBLE
            val zoomInAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
            val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out)

            splashActivityMainBinding!!.imgSplashScreen.startAnimation(zoomInAnimation)
            splashActivityMainBinding!!.txtSplash.startAnimation(zoomInAnimation)

            zoomInAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }
                override fun onAnimationEnd(p0: Animation?) {
                    splashActivityMainBinding!!.imgSplashScreen.startAnimation(fadeOutAnimation)
                    splashActivityMainBinding!!.txtSplash.startAnimation(fadeOutAnimation)
                    Handler(Looper.myLooper()!!).postDelayed({
                        goToNextActivity()
                    }, 500)
                }
                override fun onAnimationRepeat(p0: Animation?) {
                }
            })
        },3000)
    }
    private fun goToNextActivity()
    {
        if(DataStoreManager.getUser() != null && !TextUtils.isEmpty(DataStoreManager.getUser()?.email))
        {
            FunctionGlobal.gotoMainActivity(this)
            this.finish()
        }
        else
        {
            startActivity(Intent(this@SplashActivity,SignInActivity::class.java))
            this.finish()
        }
    }
}