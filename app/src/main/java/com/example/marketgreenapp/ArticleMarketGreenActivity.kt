package com.example.marketgreenapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.PostAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityArticleMarketGreenBinding
import com.example.marketgreenapp.fragment.CommentBottomSheetFragment
import com.example.marketgreenapp.listener.IOnClickItemPost
import com.example.marketgreenapp.listener.IOnCommentUpdatedListener
import com.example.marketgreenapp.model.Post
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ArticleMarketGreenActivity : AppCompatActivity() {
    private var communicateBinding: ActivityArticleMarketGreenBinding ? = null
    private lateinit var postAdapter: PostAdapter
    private var mListPosts: MutableList<Post>? = null

    private var activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val action = result.data?.getStringExtra(ConstantKey.KEY_ACTION_POST)
                val post = result.data!!.getSerializableExtra(ConstantKey.KEY_POST) as Post?
                post?.let {
                    lifecycleScope.launch {
                        try {
                            when (action) {
                                ConstantKey.VALUE_ADD_POST -> {
                                    savePostToFirebase(it)
                                }
                                ConstantKey.VALUE_EDIT_POST -> {
                                    updatePostToFirebase(it)
                                }
                            }
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(
                                this@ArticleMarketGreenActivity,
                                "Thêm bài viết thất bại!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (communicateBinding == null)
            communicateBinding = ActivityArticleMarketGreenBinding.inflate(layoutInflater)
        setContentView(communicateBinding!!.root)


        communicateBinding!!.lottieLoadingData.visibility = View.VISIBLE
        lifecycleScope.launch {
            getDataFromFirebase()
            communicateBinding!!.lottieLoadingData.cancelAnimation()
            communicateBinding!!.lottieLoadingData.visibility = View.GONE
            updateListPostToRecyclerView()
        }
        communicateBinding!!.imgBackMain.setOnClickListener {
            this.finish()
        }
        communicateBinding!!.constraintLayoutCreatePost.setOnClickListener {
            val intent = Intent(this@ArticleMarketGreenActivity,PostArticleActivity::class.java)
            val pairs:Array<Pair<View, String>> = arrayOf(
                Pair(communicateBinding!!.layoutRoot,"transition_drawer")
            )
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@ArticleMarketGreenActivity,
                *pairs
            )
            option.update(
                ActivityOptionsCompat.makeCustomAnimation(
                    this@ArticleMarketGreenActivity,
                    R.anim.slide_in_bottom,
                    R.anim.slide_no_anim
                )
            )
            activityResultLauncher.launch(intent,option)
        }
        communicateBinding!!.linearLayoutCreateArticle.setOnClickListener {


            val intent = Intent(this@ArticleMarketGreenActivity,PostArticleActivity::class.java)
            val pairs:Array<Pair<View, String>> = arrayOf(
                Pair(communicateBinding!!.layoutRoot,"transition_drawer")
            )
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@ArticleMarketGreenActivity,
                *pairs
            )
            option.update(
                ActivityOptionsCompat.makeCustomAnimation(
                    this@ArticleMarketGreenActivity,
                    R.anim.slide_in_bottom,
                    R.anim.slide_no_anim
                )
            )
            intent.putExtra(ConstantKey.KEY_POST, Post())
            activityResultLauncher.launch(intent,option)
        }

    }

    private suspend fun savePostToFirebase(post: Post) {
        val databaseReference =
            MarketGreenFirebaseApp[this].getDataPostArticleFromFirebaseDatabaseReference()
        withContext(Dispatchers.IO) {
            databaseReference.child(post.id.toString()).setValue(post).await()
            withContext(Dispatchers.Main) { // Update UI on Main thread
                val message = "Bài viết đã được thêm thành công!"
                Toast.makeText(this@ArticleMarketGreenActivity, message, Toast.LENGTH_SHORT).show()
                mListPosts?.add(0, post)
                updateListPostToRecyclerView()
            }
        }
    }

    private fun isPostExist(postId: Long): Boolean {
        return mListPosts?.any { it.id == postId } ?: false
    }

    private suspend fun updatePostToFirebase(post: Post) {
        val databaseReference =
            MarketGreenFirebaseApp[this].getDataPostArticleFromFirebaseDatabaseReference()
        withContext(Dispatchers.IO) {
            try {
                databaseReference.child(post.id.toString()).setValue(post).await()
                withContext(Dispatchers.Main) {
                    mListPosts?.let { it ->
                        val index = it.indexOfFirst { it.id == post.id }
                        if (index != -1) {
                            it[index] = post
                            postAdapter.notifyItemChanged(index)
                        }
                    }
                    Toast.makeText(
                        this@ArticleMarketGreenActivity,
                        "Cập nhật bài viết thành công!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private suspend fun fetchDataAndUpdateUI() {
        getDataFromFirebase()
        updateListPostToRecyclerView()
    }

    //addOnSuccessListener sẽ chạy trên thread mà Firebase sử dụng
    // databaseReference.child(post.id.toString()).setValue(post).await() -> IO, is suspend fun
    private suspend fun getDataFromFirebase() {
        return withContext(Dispatchers.IO)
        {
            try {
                val dataSnapshot =
                    MarketGreenFirebaseApp[applicationContext].getDataPostArticleFromFirebaseDatabaseReference()
                        .get().await()
                if (mListPosts != null) mListPosts!!.clear()
                else mListPosts = mutableListOf()
                for (dataSnapshotChildren in dataSnapshot.children) {
                    val postChildren: Post? = dataSnapshotChildren.getValue(Post::class.java)
                    postChildren?.let {
                        mListPosts!!.add(0, postChildren)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateListPostToRecyclerView() {
        postAdapter = PostAdapter(mListPosts, this, object : IOnClickItemPost {
            override fun onClickItemPost(post: Post?, position: Int) {
                val checkIsLike = post?.likes?.get(DataStoreManager.getUser()?.uid) ?: false
                val intent = Intent(this@ArticleMarketGreenActivity, PostArticleActivity::class.java)
                intent.putExtra(ConstantKey.KEY_BOOLEAN_EDIT_POST, true)
                intent.putExtra(ConstantKey.KEY_POST, post)
                activityResultLauncher.launch(intent)
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_no_anim)
            }

            override fun onClickItemAccessShop(post: Post?) {
                val bundle = Bundle()
                bundle.putString(ConstantKey.KEY_INTENT_POST_USERID, post!!.userId)
                bundle.putString(ConstantKey.KEY_INTENT_HOME_PRODUCT_KEY, "article")
                TransitionHelper.navigateWithTransition(
                    this@ArticleMarketGreenActivity,
                    ShopActivity::class.java,
                    communicateBinding!!.layoutRoot,
                    "transition_drawer",
                    R.anim.slide_in_right,
                    R.anim.slide_no_anim,
                    bundle
                )
            }

            override fun onClickDeletePost(post: Post?, position: Int) {
                deletePostFromFirebase(post, position)
            }

            override fun onClickSavePost(post: Post?, state: Boolean) {
                if (state) {
                    Toast.makeText(
                        this@ArticleMarketGreenActivity,
                        "da save bai viet",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ArticleMarketGreenActivity,
                        "da delete bai viet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onClickItemCurrent(post: Post?, position: Int) {
                openCommentBottomSheet(post)
            }
        })
        if (mListPosts!!.size > 0) {
            communicateBinding?.linearLayoutCreateArticle?.visibility = View.GONE
            communicateBinding?.rcvArticle?.visibility = View.VISIBLE

            communicateBinding?.rcvArticle?.layoutManager = LinearLayoutManager(this)
            communicateBinding?.rcvArticle?.adapter = postAdapter
        } else {
            communicateBinding?.linearLayoutCreateArticle?.visibility = View.VISIBLE
            communicateBinding?.rcvArticle?.visibility = View.GONE
        }

    }
    private fun openCommentBottomSheet(post: Post?) {
        val bottomSheetFragment = CommentBottomSheetFragment()
        val bundle = Bundle()
        bundle.putSerializable(ConstantKey.KEY_POST_COMMENT,post)
        bottomSheetFragment.arguments = bundle

        bottomSheetFragment.setOnCommentUpdatedListener(object : IOnCommentUpdatedListener {
            override fun onCommentUpdated(postId: String, newCommentCount: Int) {
                updateCommentNumberUI(postId,newCommentCount)
            }
        })
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun updateCommentNumberUI(postId: String, newCommentCount: Int)
    {
        val position = mListPosts?.indexOfFirst { it.id.toString() == postId }
        if (position != null && position >= 0) {
            mListPosts!![position].quantityComment = newCommentCount
            postAdapter.notifyItemChanged(position)
        }
    }
    private fun deletePostFromFirebase(post: Post?, position: Int) {
        val databaseReference =
            MarketGreenFirebaseApp[this].getDataPostArticleFromFirebaseDatabaseReference()
        val databaseEvaluateRef =
            MarketGreenFirebaseApp[this].getDataPostArticleFromFirebaseDatabaseReference()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                databaseReference.child(post?.id.toString()).removeValue().await()
                databaseEvaluateRef.child(post?.id.toString()).removeValue().await()
                withContext(Dispatchers.Main)
                {
                    mListPosts?.remove(post)
                    postAdapter.notifyItemRemoved(position)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

}

