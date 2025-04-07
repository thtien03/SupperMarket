package com.example.marketgreenapp.shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.marketgreenapp.ArticleMarketGreenActivity
import com.example.marketgreenapp.R
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityProfileShopBinding
import com.example.marketgreenapp.databinding.ActivityProfileUser2Binding
import com.example.marketgreenapp.databinding.ActivityProfileUserBinding
import com.example.marketgreenapp.references.DataStoreManager

class ProfileShopActivity : AppCompatActivity() {
    private var profileBinding: ActivityProfileShopBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(profileBinding == null)
            profileBinding = ActivityProfileShopBinding.inflate(layoutInflater)
        setContentView(profileBinding!!.root)
        setUserProfile()

        profileBinding?.imgBackMain?.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                this@ProfileShopActivity,
                ShopMainActivity::class.java,
                profileBinding!!.layoutRoot,
                "transition_drawer",
                R.anim.slide_in_left,
                R.anim.slide_no_anim
            )
            this.finish()
        }
        profileBinding?.linearLayoutLogout?.setOnClickListener {

        }
        profileBinding?.linearLayoutChangePassword?.setOnClickListener {

        }
        profileBinding?.linearLayoutFeedback?.setOnClickListener {

        }
        profileBinding?.linearLayoutArticleTym?.setOnClickListener {

        }
        profileBinding?.linearLayoutProfileArticle?.setOnClickListener {

        }
        profileBinding?.linearLayoutProfileFollowers?.setOnClickListener {

        }
        profileBinding?.linearLayoutProfileOrder?.setOnClickListener {

        }

        profileBinding?.cardViewCreateArticle?.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                this@ProfileShopActivity,
                ArticleMarketGreenActivity::class.java,
                profileBinding!!.layoutRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        }
    }
    private fun setUserProfile()
    {
        val userDataStore = DataStoreManager.getUser()
        if(userDataStore != null)
        {
            profileBinding?.txtNameUser?.text = userDataStore.fullname
            if(userDataStore.image == null)
            {
                profileBinding?.imgAvatarUser?.setImageResource(R.drawable.img_14)
            }
            else GlideImageURL.loadImageURL(userDataStore.image,profileBinding?.imgAvatarUser)
        }
    }
}