package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.R
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutItemArticleBinding
import com.example.marketgreenapp.listener.IOnClickItemPost
import com.example.marketgreenapp.model.Post
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

class PostAdapter(
    private val mListPosts: MutableList<Post>?,
    private val mContext: Context,
    private val iOnClickItemPost: IOnClickItemPost
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val postBinding: LayoutItemArticleBinding =
            LayoutItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(postBinding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = mListPosts?.get(position)
        var isProcessingLike = false
        if (currentPost != null) {
            holder.bindData(currentPost)
            if (holder.postBinding != null) {
                //post-> đảm bảo rằng mã trong block sẽ chạy sau khi txt hoàn tất việc đo đạc
                holder.postBinding.txtContentPost.post {
                    val lineCount = holder.postBinding.txtContentPost.lineCount
                    val maxLines = holder.postBinding.txtContentPost.maxLines

                    if (lineCount >= maxLines) {
                        holder.postBinding.txtRedMore.visibility = View.VISIBLE
                    } else {
                        holder.postBinding.txtRedMore.visibility = View.GONE
                    }
                }
                if (currentPost.isRole.equals("shop")) {
                    holder.postBinding.relativeLayoutShopOrUser.visibility = View.VISIBLE
                    val userCurrentDataStoreManager = DataStoreManager.getUser()
                    if(userCurrentDataStoreManager != null)
                    {
                        if(userCurrentDataStoreManager.role.equals("shop"))
                        {
                            if(userCurrentDataStoreManager.uid.equals(currentPost.userId))
                            {
                                holder.postBinding.relativeLayoutShopOrUser.visibility = View.VISIBLE
                            }
                            else
                            {
                                holder.postBinding.relativeLayoutShopOrUser.visibility = View.GONE
                            }
                        }
                    }
                    holder.postBinding.relativeLayoutShopOrUser.setOnClickListener {
                        iOnClickItemPost.onClickItemAccessShop(currentPost)
                    }
                }
                else
                    holder.postBinding.relativeLayoutShopOrUser.visibility = View.GONE
                holder.postBinding.txtRedMore.setOnClickListener {
                    if (holder.postBinding.txtRedMore.text.toString() == "Xem thêm") {
                        holder.postBinding.txtContentPost.maxLines = Int.MAX_VALUE
                        holder.postBinding.txtContentPost.ellipsize = null
                        holder.postBinding.txtRedMore.text = "Ẩn bớt"
                    } else {
                        holder.postBinding.txtContentPost.maxLines = 2
                        holder.postBinding.txtContentPost.ellipsize = null
                        holder.postBinding.txtRedMore.text = "Xem thêm"
                    }
                }
                holder.postBinding.imgEditPost.setOnClickListener {
                    iOnClickItemPost.onClickItemPost(currentPost, position)
                }
                val userId = DataStoreManager.getUser()?.uid ?: return
                val postId = currentPost.id
                val postUser = currentPost.userId
                if (postUser!!.trim() == userId) {
                    holder.postBinding.imgEditPost.visibility = View.VISIBLE
                    holder.postBinding.imgDeletePost.visibility = View.VISIBLE
                } else {
                    holder.postBinding.imgEditPost.visibility = View.GONE
                    holder.postBinding.imgDeletePost.visibility = View.GONE
                }
                if (currentPost.likes != null) {
                    //val listUserLike = currentPost.likes?.filter { it.value }?.keys
                    val checkIsLike = currentPost.likes?.get(userId) ?: false
                    holder.postBinding.imgLikePost.setImageResource(
                        if (checkIsLike) R.drawable.icons8_like_done else R.drawable.icons8_love
                    )
                }

                holder.postBinding.imgLikePost.setOnClickListener {
                    if (isProcessingLike) return@setOnClickListener
                    isProcessingLike = true
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            withContext(Dispatchers.IO) {
                                toggleLike(postId.toString(), userId, holder)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isProcessingLike = false
                        }
                    }
                }
                holder.postBinding.imgDeletePost.setOnClickListener {
                    iOnClickItemPost.onClickDeletePost(currentPost, position)
                }
                holder.postBinding.imgComment.setOnClickListener {
                    iOnClickItemPost.onClickItemCurrent(currentPost, position)
                }
                holder.postBinding.txtNumberComment.setOnClickListener {
                    iOnClickItemPost.onClickItemCurrent(currentPost, position)
                }
            }
        }
    }


    private suspend fun toggleLike(postId: String, userId: String, holder: PostViewHolder) {
        val likeRef =
            MarketGreenFirebaseApp[mContext].getDataPostArticleFromFirebaseDatabaseReference()
                .child(postId)
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
                            println("newLikeCount = $newLikeCount")
                            updateLikeUI(newLikeStatus, newLikeCount, holder)
                        }
                    }
                }
            })
    }

    fun updateLikeUI(isLiked: Boolean, likeCount: Int, holder: PostViewHolder) {
        holder.postBinding?.imgLikePost?.setImageResource(
            if (isLiked) R.drawable.icons8_like_done else R.drawable.icons8_love
        )
        holder.postBinding?.txtNumberLike?.text = likeCount.toString()
    }


    override fun getItemCount(): Int {
        return mListPosts?.size ?: 0
    }

    inner class PostViewHolder(val postBinding: LayoutItemArticleBinding?) :
        RecyclerView.ViewHolder(postBinding!!.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(post: Post) {
            postBinding?.txtTitlePost?.text = post.title.toString().trim()
            postBinding?.txtContentPost?.text = post.content.toString().trim()
            postBinding?.txtTimePostArticle?.text = post.datePost.toString().trim()
            //GlideImageURL.loadImageURL(post.imageUrl,postBinding?.roundedImgAvatar)
            postBinding?.txtNameUser?.text = post.usernamePost.toString().trim()
            postBinding?.txtNumberLike?.text = post.quantityLike.toString()
            postBinding?.txtNumberComment?.text = "${post.quantityComment} Bình luận"
            val gradientDrawable = postBinding?.backgroundPostArticle
            if (post.colorBackground != null) {
                gradientDrawable?.setBackgroundColor(Color.parseColor(post.colorBackground))
            } else {
                val colorDefault = mContext.resources.getColor(R.color.colorDefaultPostColor, null)
                gradientDrawable?.setBackgroundColor(colorDefault)
            }
            if (post.imageUrl != null) {
                postBinding?.imgPost?.visibility = View.VISIBLE
                GlideImageURL.loadImageURL(post.imageUrl, postBinding?.imgPost)
            } else
                postBinding?.imgPost?.visibility = View.GONE
        }
    }
}