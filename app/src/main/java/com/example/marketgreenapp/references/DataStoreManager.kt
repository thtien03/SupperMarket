package com.example.marketgreenapp.references
import android.content.Context
import android.text.TextUtils
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.model.User
import com.google.gson.Gson

class DataStoreManager {
    private var sharedPreferences: FunctionMyShared? = null

    companion object {
        private var instance: DataStoreManager? = null

        fun init(context: Context?) {
            instance = DataStoreManager()
            instance!!.sharedPreferences = FunctionMyShared(context)
        }

        private fun getInstance(): DataStoreManager? {
            return if (instance != null) {
                instance
            } else {
                throw IllegalStateException("Not initialized")
            }
        }

        public fun setUser(user: User?) {
            var convertUserToJson = ""
            if (user != null) {
                convertUserToJson = user.toJson()
            }
            getInstance()!!.sharedPreferences!!
                .putStringValueCustom(ConstantKey.SHARED_PREFERENCES_USER, convertUserToJson)
        }

        public fun getUser(): User? {
            val jsonUser: String? =
                getInstance()!!.sharedPreferences!!.getStringValueCustom(ConstantKey.SHARED_PREFERENCES_USER)
            return if (!TextUtils.isEmpty(jsonUser)) {
                Gson().fromJson(jsonUser, User::class.java)
            } else User()
        }
    }
}