package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.R
import com.example.marketgreenapp.databinding.LayoutItemCommentBinding
import com.example.marketgreenapp.model.Comment
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommentAdapter(private val mListComments:MutableList<Comment>?,
                     private val mContext:Context)
    : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val commentBinding: LayoutItemCommentBinding
            = LayoutItemCommentBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CommentViewHolder(commentBinding)
    }

    override fun getItemCount(): Int {
        return mListComments?.size ?: 0
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        var isProcessingLike = false
        val currentComment = mListComments?.get(position)
        val user = DataStoreManager.getUser()
        if(user != null)
        {
            if(currentComment != null)
            {
                holder.bindData(currentComment)
                holder.commentBinding?.txtNameUser?.text = currentComment.usernameComment

                    if (currentComment.userId != null) {
                    val checkIsLike = currentComment.likes?.get(user.uid) ?: false
                    holder.commentBinding?.imgLikeComment?.setImageResource(
                        if (checkIsLike) R.drawable.icons8_like_done else R.drawable.icons8_love
                    )
                }
                holder.commentBinding?.imgLikeComment?.setOnClickListener {
                    if (isProcessingLike) return@setOnClickListener
                    isProcessingLike = true
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val user = DataStoreManager.getUser()
                            withContext(Dispatchers.IO) {
                                toggleLike(currentComment.postId!!, currentComment.id.toString(),user!!.uid.toString(), holder)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isProcessingLike = false
                        }
                    }
                }
            }
        }
    }
    private suspend fun toggleLike(postId: String, commentId: String, userId:String,holder: CommentAdapter.CommentViewHolder) {
        val likeRef = MarketGreenFirebaseApp[mContext].getDataEvaluateFromFirebaseDatabaseReference()
            .child(postId).child("comments").child(commentId)

        val snapshot = likeRef.child("likes").child(userId).get().await()

        val isCurrentlyLiked = snapshot.getValue(Boolean::class.java) ?: false
        val newLikeStatus = !isCurrentlyLiked
        likeRef.child("likes").child(userId).setValue(newLikeStatus).await()
        likeRef.child("quantityLike")
            .runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: MutableData): com.google.firebase.database.Transaction.Result {
                    val currentLikes = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = if (newLikeStatus) currentLikes + 1 else currentLikes - 1
                    return com.google.firebase.database.Transaction.success(currentData)
                }
                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (committed) {
                        val newLikeCount = currentData?.getValue(Int::class.java) ?: 0
                        CoroutineScope(Dispatchers.Main).launch {
                            updateLikeUI(newLikeStatus, newLikeCount, holder)
                        }
                    }
                }
            })
    }
    @SuppressLint("SetTextI18n")
    fun updateLikeUI(isLiked: Boolean, likeCount: Int, holder: CommentAdapter.CommentViewHolder) {
        holder.commentBinding?.imgLikeComment?.setImageResource(
            if (isLiked) R.drawable.icons8_like_done else R.drawable.icons8_love
        )
        holder.commentBinding?.txtNumberLike?.text = "$likeCount lượt thích"
    }

    inner class CommentViewHolder(val commentBinding:LayoutItemCommentBinding?) :  RecyclerView.ViewHolder(commentBinding!!.root)
    {
        @SuppressLint("SetTextI18n")
        fun bindData(comment: Comment)
        {
            commentBinding?.txtContentComment?.text = comment.contentComment
            commentBinding?.txtNumberLike?.text = "${comment.quantityLike} lượt thích"
            commentBinding?.txtTimePostArticle?.text = comment.dateSend.toString()
        }
    }

}