package com.example.marketgreenapp.activity_user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.marketgreenapp.ArticleMarketGreenActivity
import com.example.marketgreenapp.R
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityProfileUser2Binding
import com.example.marketgreenapp.databinding.ActivityProfileUserBinding
import com.example.marketgreenapp.references.DataStoreManager

class ProfileUserActivity : AppCompatActivity() {
    private var profileBinding: ActivityProfileUser2Binding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(profileBinding == null)
            profileBinding = ActivityProfileUser2Binding.inflate(layoutInflater)
        setContentView(profileBinding!!.root)

        setUserProfile()

        profileBinding?.imgBackMain?.setOnClickListener {
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
                this@ProfileUserActivity,
                ArticleMarketGreenActivity::class.java,
                profileBinding!!.layoutRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        }
        profileBinding?.cardViewPurchase?.setOnClickListener {

        }
    }
    private fun setUserProfile()
    {
        val userDataStore = DataStoreManager.getUser()
        if(userDataStore != null)
        {
            profileBinding?.txtNameUser?.text = userDataStore.fullname
            GlideImageURL.loadImageURL(userDataStore.image,profileBinding?.imgAvatarUser)
        }
    }
}