package com.example.marketgreenapp.references

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.marketgreenapp.constant.ConstantKey

class FunctionMyShared(private var context:Context?) {


    fun putStringValueCustom(key:String?,value:String?)
    {
        val pref = context!!.getSharedPreferences(ConstantKey.SHARED_PREFERENCES_GLOBAL,MODE_PRIVATE)
        val sharedEditor = pref.edit()
        sharedEditor.apply {
            this.putString(key,value)
            this.apply()
        }
    }

    fun getStringValueCustom(key: String?):String?
    {
        val pref = context!!.getSharedPreferences(
            ConstantKey.SHARED_PREFERENCES_GLOBAL,
            MODE_PRIVATE)
        return pref.getString(key,"")
    }
}