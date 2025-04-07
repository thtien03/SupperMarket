package com.example.marketgreenapp.activity_register

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cloudinary.api.exceptions.ApiException
import com.example.marketgreenapp.ForgotpasswordActivity
import com.example.marketgreenapp.R
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivitySignInBinding
import com.example.marketgreenapp.model.User
import com.example.marketgreenapp.references.DataStoreManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {
    private var signInActivityBinding: ActivitySignInBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private var googleSignInClient: GoogleSignInClient? = null
    private lateinit var dialog: Dialog
    private var loadingDialog: Dialog? = null

    private var activityResultLauncherSignInGoogle =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                showDialog(true)
                val accountTask: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val signInAccount: GoogleSignInAccount =
                        accountTask.getResult(ApiException::class.java)
                    val authCredential: AuthCredential =
                        GoogleAuthProvider.getCredential(signInAccount.idToken, null)
                    firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener {
                        showDialog(false)
                        if (it.isSuccessful) {
                            val userFbCurrent = firebaseAuth.currentUser
                            if (userFbCurrent != null) {
                                val userUid = userFbCurrent.uid
                                val email = userFbCurrent.email
                                val name = userFbCurrent.displayName
                                val image = userFbCurrent.photoUrl.toString()
                                val user = User(userUid, email, image, name, false, "user")
                                if (user.uid != null) {
                                    val df = firebaseStore.collection("Users").document(user.uid!!)
                                    val mapUserInfo: MutableMap<String, Any> = hashMapOf()
                                    mapUserInfo[ConstantKey.CLOUD_KEY_USERNAME] =
                                        user.fullname.toString()
                                    mapUserInfo[ConstantKey.CLOUD_KEY_EMAIL] = user.email.toString()
                                    mapUserInfo[ConstantKey.CLOUD_KEY_IS_UID] = user.uid.toString()
                                    mapUserInfo[ConstantKey.CLOUD_KEY_PHONE] = ""
                                    mapUserInfo[ConstantKey.CLOUD_KEY_BIRTHDAY] = ""
                                    mapUserInfo[ConstantKey.CLOUD_KEY_PASSWORD] = ""
                                    mapUserInfo[ConstantKey.CLOUD_KEY_IS_ROLE] = "user"
                                    mapUserInfo[ConstantKey.CLOUD_KEY_IS_SALT] = ""
                                    df.set(mapUserInfo)

                                    DataStoreManager.setUser(user)
                                    FunctionGlobal.gotoMainActivity(this@SignInActivity)
                                    finishAffinity()
                                }
                            }
                            Toast.makeText(
                                this@SignInActivity,
                                firebaseAuth.currentUser?.displayName,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@SignInActivity,
                                "Signed to failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (signInActivityBinding == null) {
            signInActivityBinding = ActivitySignInBinding.inflate(layoutInflater)
        }
        setContentView(signInActivityBinding!!.root)

        initObj()
        signInActivityBinding!!.layoutSignUp.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                this@SignInActivity,
                SignUpActivity::class.java,
                signInActivityBinding!!.layoutSignIn,
                "transition_register",
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        }

        signInActivityBinding!!.btnSignIn.setOnClickListener {
            solutionSignInCarbonTracker()
        }
        signInActivityBinding!!.btnForgotPassword.setOnClickListener {
            /*TransitionHelper.navigateWithTransition(
                this@SignInActivity,
                ForgotpasswordActivity::class.java,
                signInActivityBinding!!.layoutSignIn,
                "transition_register",
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )*/
        }
        signInActivityBinding!!.relativeLayoutGoogle.setOnClickListener {
            solutionSignInUserByGoogle()
        }
        signInActivityBinding!!.btnForgotPassword.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                this@SignInActivity,
                ForgotpasswordActivity::class.java,
                signInActivityBinding!!.layoutSignIn,
                "transition_register",
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        }
    }

    private fun initObj() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()

        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_loading)
    }

    private fun solutionSignInUserByGoogle() {
        val googleOptions: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_google))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this@SignInActivity, googleOptions)
        firebaseAuth = FirebaseAuth.getInstance()

        if (googleSignInClient != null) {
            val intent = googleSignInClient?.signInIntent
            activityResultLauncherSignInGoogle.launch(intent)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this@SignInActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkData(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            signInActivityBinding!!.edtEmail.error = "*Vui lòng nhập email đăng nhập"
            return false
        } else if (TextUtils.isEmpty(password)) {
            signInActivityBinding!!.edtPassword.error = "*Vui lòng nhập mật khẩu"
            return false
        }
        return true
    }

    private fun solutionSignInCarbonTracker() {
        val strEmail = signInActivityBinding!!.edtEmail.text.toString().trim()
        val strPassword = signInActivityBinding!!.edtPassword.text.toString().trim()
        if (checkData(strEmail, strPassword)) {
            if (FunctionGlobal.isEmailValid2(strEmail)) {
                signInUser(strEmail, strPassword)
            } else
                showMessage("Email không hợp lệ!")
        }

    }

    private suspend fun getInformationUserByEmail(email: String): MutableMap<String, Any>? {
        val documentSnapshot = firebaseStore.collection("Users")
            .whereEqualTo(ConstantKey.CLOUD_KEY_EMAIL, email)
            .get()
            .await()
        return documentSnapshot.firstOrNull()?.data
    }

    private fun signInUser(email: String, strPassword: String) {
        showDialog(true)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userInfo = getInformationUserByEmail(email)
                if (userInfo != null) {
                    val salt = userInfo[ConstantKey.CLOUD_KEY_IS_SALT] as? String
                    val hashedPassword = salt?.let {
                        FunctionGlobal.hashPasswordWithSalt(strPassword, it)
                    }

                    if (hashedPassword == userInfo[ConstantKey.CLOUD_KEY_PASSWORD].toString()) {
                        withContext(Dispatchers.Main) {
                            firebaseAuth.signInWithEmailAndPassword(email, strPassword)
                                .addOnCompleteListener(this@SignInActivity) { task ->
                                    showDialog(false)
                                    if (task.isSuccessful) {
                                        val uid = task.result.user?.uid
                                        if (uid != null) {
                                            if (hashedPassword != userInfo[ConstantKey.CLOUD_KEY_PASSWORD]) {
                                                // Cập nhật mật khẩu đã mã hóa trong Firestore
                                                firebaseStore.collection("Users").document(uid)
                                                    .update(
                                                        ConstantKey.CLOUD_KEY_PASSWORD,
                                                        hashedPassword
                                                    )
                                                    .addOnFailureListener {
                                                        showMessage("Cập nhật mật khẩu không thành công.")
                                                    }
                                            }
                                            val getUser = firebaseAuth.currentUser
                                            if (getUser != null) {
                                                val phone =
                                                    userInfo[ConstantKey.CLOUD_KEY_PHONE].toString()
                                                val date =
                                                    userInfo[ConstantKey.CLOUD_KEY_BIRTHDAY].toString()
                                                val name =
                                                    userInfo[ConstantKey.CLOUD_KEY_USERNAME].toString()
                                                val isRole =
                                                    userInfo[ConstantKey.CLOUD_KEY_IS_ROLE].toString()
                                                val isEnable: Boolean =
                                                    userInfo[ConstantKey.CLOUD_KEY_IS_ENABLE] as Boolean
                                                val newUser =
                                                    User(
                                                        uid, email, strPassword, name, phone, date, isEnable, isRole
                                                    )
                                                DataStoreManager.setUser(newUser)
                                                FunctionGlobal.gotoMainActivity(this@SignInActivity)
                                                finishAffinity()
                                            }
                                        }
                                    } else {
                                        showMessage("Tên đăng nhập hoặc mật khẩu không chính xác.")
                                        resetDataLogin()
                                    }
                                }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showDialog(false)
                            showMessage("Tên đăng nhập hoặc mật khẩu không chính xác.")
                            resetDataLogin()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showDialog(false)
                        showMessage("Tên đăng nhập hoặc mật khẩu không chính xác.")
                        resetDataLogin()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun resetDataLogin()
    {
        signInActivityBinding?.edtEmail?.setText("")
        signInActivityBinding?.edtPassword?.setText("")
        signInActivityBinding?.edtEmail?.requestFocus()
    }

    private suspend fun getInformationUser(uid: String): MutableMap<String, Any> {
        return withContext(Dispatchers.IO) {
            val mapValue: MutableMap<String, Any> = mutableMapOf()
            val documentRf = firebaseStore.collection("Users").document(uid)
            val documentSnapshot = documentRf.get().await()
            mapValue
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

    override fun onPause() {
        super.onPause()
        loadingDialog?.dismiss()
    }
}