package com.example.marketgreenapp

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.example.marketgreenapp.activity_register.SignInActivity
import com.example.marketgreenapp.activity_user.ProfileUserActivity
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityMainBinding
import com.example.marketgreenapp.fragment.HomeMarketGreenFragment
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.model.ProductOrder
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private var googleSignInClient: GoogleSignInClient? = null
    private var mListProductOrder: MutableList<ProductOrder>? = null
    private var loadingDialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        setNavigationView()
        eventNavigationView()
        eventNavigationViewApp()

        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(R.id.frame_layout, HomeMarketGreenFragment())
        fragmentTransition.commit()

        mainBinding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    TransitionHelper.navigateWithTransition(
                        this@MainActivity,
                        ProfileUserActivity::class.java,
                        mainBinding.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_article -> {
                    TransitionHelper.navigateWithTransition(
                        this@MainActivity,
                        ArticleMarketGreenActivity::class.java,
                        mainBinding.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
            }
            true
        }
        mainBinding.constraintLayoutCart.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "main")
            TransitionHelper.navigateWithTransition(
                this@MainActivity,
                OrderProductActivity::class.java,
                mainBinding.drawerLayout,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_no_anim,
                bundle
            )
        }
        lifecycleScope.launch {
            getDataNumberProductCart()
            mainBinding.txtNumberProductCart.text = mListProductOrder!!.size.toString()
        }
    }

    private suspend fun getDataNumberProductCart() {
        val currentUser = DataStoreManager.getUser()
        return withContext(Dispatchers.IO)
        {
            try {
                if (currentUser?.uid != null) {
                    val dataSnapshot = MarketGreenFirebaseApp[this@MainActivity]
                        .getDataProductAddToCartFromFirebaseDatabaseReference()
                        .child(currentUser.uid!!).get().await()
                    if (mListProductOrder != null) mListProductOrder!!.clear()
                    else mListProductOrder = mutableListOf()

                    for (dataSnapshotChildren in dataSnapshot.children) {
                        val productChildren: ProductOrder? =
                            dataSnapshotChildren.getValue(ProductOrder::class.java)
                        productChildren?.let {
                            mListProductOrder!!.add(0, it)
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setNavigationView() {
        mainBinding.navigationViewApp.itemIconTintList = null
        mainBinding.bottomNavigationView.background = null
        mainBinding.bottomNavigationView.itemIconTintList = null
        mainBinding.bottomNavigationView.menu.getItem(2).isEnabled = false
    }

    private fun eventNavigationView() {
        mainBinding.imgOpenNavigationApp.setOnClickListener {
            mainBinding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun eventNavigationViewApp() {
        mainBinding.navigationViewApp.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    logoutGoogle()
                }
                R.id.nav_community -> {
                    TransitionHelper.navigateWithTransition(
                        this@MainActivity,
                        DeliveryAddressActivity::class.java,
                        mainBinding.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_saving_history -> {
                    TransitionHelper.navigateWithTransition(
                        this@MainActivity,
                        PaymentOrderActivity::class.java,
                        mainBinding.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_history_order_product ->
                {
                    TransitionHelper.navigateWithTransition(
                        this@MainActivity,
                        HistoryOrderProductActivity::class.java,
                        mainBinding.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }

                R.id.nav_history_order_product_success ->
                {
                    TransitionHelper.navigateWithTransition(
                        this@MainActivity,
                        HistoryProductOrderSuccessActivity::class.java,
                        mainBinding.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_follow_order_product ->
                {
                    TransitionHelper.navigateWithTransition(
                        this@MainActivity,
                        FollowOrderProductActivity::class.java,
                        mainBinding.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
                R.id.nav_article ->
                {
                    TransitionHelper.navigateWithTransition(
                        this@MainActivity,
                        ArticleMarketGreenActivity::class.java,
                        mainBinding.drawerLayout,
                        "transition_drawer",
                        R.anim.slide_in_right,
                        R.anim.slide_no_anim
                    )
                }
            }
            mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun logoutGoogle() {
        showDialog(true)
        val googleOptions: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_google))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this@MainActivity, googleOptions)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                /*FirebaseAuth.getInstance().addAuthStateListener {
                    if (it.currentUser == null) {
                        googleSignInClient!!.signOut().addOnSuccessListener {
                            Toast.makeText(this@MainActivity, "bạn đã đăng xuất", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }*/
                googleSignInClient?.signOut()?.await()
                FirebaseAuth.getInstance().signOut()
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(this@MainActivity, "Bạn đã đăng xuất", Toast.LENGTH_SHORT).show()
                    showDialog(false)
                    DataStoreManager.setUser(null)
                    startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                    finishAffinity()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) mainBinding.drawerLayout.closeDrawer(
            GravityCompat.START
        )
        else if (mainBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) mainBinding.drawerLayout.closeDrawer(
            GravityCompat.END
        )
        else super.onBackPressed()
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
//https://alabaster-drop-b52.notion.site/TinCoder-Full-Source-Code-Android-c1f9d9d224004932823b6d5061c80dbc?p=11e822f49992808eafedef58489a5425&pm=c
//https://www.youtube.com/watch?v=u2cA5Ml56eg
//https://www.apporio.com/product/gojek-clone-app?https://www.apporio.com/product/gojek-clone-app?utm_source=Discovery&utm_medium=CPC&utm_campaign=All%20In%20One%20Discovery%20Competitor&gclid=EAIaIQobChMI7urY0P2DigMVYEA4BR21bAjmEAEYAiAAEgIIP_D_BwE