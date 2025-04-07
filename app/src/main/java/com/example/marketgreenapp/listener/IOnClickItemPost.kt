package com.example.marketgreenapp.listener

import com.example.marketgreenapp.model.Post

interface IOnClickItemPost {
    fun onClickItemPost(post: Post?, position:Int)
    fun onClickItemAccessShop(post: Post?)
    fun onClickDeletePost(post: Post?,position:Int)
    fun onClickSavePost(post: Post?,state:Boolean)
    fun onClickItemCurrent(post: Post?,position:Int)
}