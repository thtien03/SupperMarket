package com.example.marketgreenapp.listener

interface IOnCommentUpdatedListener {
    fun onCommentUpdated(postId: String, newCommentCount: Int)
}