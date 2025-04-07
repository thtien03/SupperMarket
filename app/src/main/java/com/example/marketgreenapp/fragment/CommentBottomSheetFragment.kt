package com.example.marketgreenapp.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.R
import com.example.marketgreenapp.adapter.CommentAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.databinding.LayoutCommentBottomSheetFragmentBinding
import com.example.marketgreenapp.listener.IOnCommentUpdatedListener
import com.example.marketgreenapp.model.Comment
import com.example.marketgreenapp.model.Post
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CommentBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: LayoutCommentBottomSheetFragmentBinding? = null
    private val binding get() = _binding!!

    private var postAvailable: Post? = null
    private var mListComment: MutableList<Comment>? = null
    private lateinit var commentAdapter: CommentAdapter

    private var commentUpdatedListener: IOnCommentUpdatedListener? = null

    fun setOnCommentUpdatedListener(listener: IOnCommentUpdatedListener) {
        commentUpdatedListener = listener
    }
    //onCreate:cần dữ liệu để hiển thị lên view,thành phần hoặc logic trước khi hiển thị giao diện(logic và khởi tạo dữ liệu)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postAvailable = it.getSerializable(ConstantKey.KEY_POST_COMMENT) as Post
        }
    }

    //onCreateView khi dữ liệu cần thiết để update ui trực tiếp hoặc ràng buộc với các view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) {
            _binding = LayoutCommentBottomSheetFragmentBinding.inflate(inflater, container, false)
        }
        mListComment = mutableListOf()
        initRecyclerview()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                getDataCommentFromFirebaseByPostId(postAvailable)
                updateCommentToRecyclerView()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _binding!!.imgSendComment.setOnClickListener {
            sendCommentToRecyclerView()
            _binding!!.edtContentComment.setText("")
            hideKeyboard()
        }
        _binding!!.edtContentComment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val current = s.toString()
                if (current.isEmpty())
                    _binding!!.imgSendComment.visibility = View.GONE
                else
                    _binding!!.imgSendComment.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        return binding.root
    }
    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(_binding?.edtContentComment?.windowToken, 0)
    }

    private suspend fun getDataCommentFromFirebaseByPostId(postAvailable: Post?) {
        if (postAvailable == null) return
        return withContext(Dispatchers.IO)
        {
            try {
                val commentDataSnapshot =
                    MarketGreenFirebaseApp[requireContext()].getDataEvaluateFromFirebaseDatabaseReference()
                        .child(postAvailable.id.toString()).child("comments").get().await()
                if (mListComment != null) mListComment!!.clear()
                else mListComment = mutableListOf()
                for (dataSnapshotChildren in commentDataSnapshot.children) {
                    val commentChildren: Comment? =
                        dataSnapshotChildren.getValue(Comment::class.java)
                    commentChildren?.let {
                        mListComment!!.add(0, commentChildren)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendCommentToRecyclerView() {
        val inputComment = _binding!!.edtContentComment.text.toString().trim()
        val dateComment = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val user = DataStoreManager.getUser()
        if (user != null) {
            val currentTimePostId = System.currentTimeMillis()
            val comment = Comment(
                currentTimePostId,
                inputComment,
                dateComment,
                0,
                user.uid,
                user.fullname,
                postAvailable!!.id.toString()
            )
            viewLifecycleOwner.lifecycleScope.launch {
                pushEvaluatesToFirebase(comment)
            }
        }
    }

    private suspend fun pushEvaluatesToFirebase(comment: Comment) {
        val postEvaluateRef =
            MarketGreenFirebaseApp[requireContext()].getDataEvaluateFromFirebaseDatabaseReference()
                .child(postAvailable!!.id.toString()).child("comments")
        postEvaluateRef.child(comment.id.toString()).setValue(comment).await()
        withContext(Dispatchers.Main)
        {
            mListComment!!.add(0, comment)
            commentAdapter.notifyItemInserted(0)
            updateCommentToRecyclerView()
            incrementCommentCount(postAvailable!!.id.toString())
            commentUpdatedListener?.onCommentUpdated(postAvailable!!.id.toString(), mListComment!!.size)
        }
    }

    private fun updateCommentToRecyclerView() {

        if (mListComment != null && mListComment!!.size > 0) {
            _binding?.rcvComments?.visibility = View.VISIBLE
            _binding?.constraintLayoutNotifyNoComment?.visibility = View.GONE
        } else {
            _binding?.rcvComments?.visibility = View.GONE
            _binding?.constraintLayoutNotifyNoComment?.visibility = View.VISIBLE
        }
    }
    private fun initRecyclerview()
    {
        commentAdapter = CommentAdapter(mListComment,requireContext())
        _binding?.rcvComments?.layoutManager = LinearLayoutManager(requireContext())
        _binding?.rcvComments?.setHasFixedSize(false)
        _binding?.rcvComments?.adapter = commentAdapter
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                sheet.setBackgroundResource(R.drawable.bg_background_bottom_sheet)
                // Đặt chiều cao mặc định của BottomSheet là 3/4 màn hình
                val displayMetrics = resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels
                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                sheet.requestLayout()
                // Thiết lập BottomSheetBehavior
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.peekHeight =
                    (screenHeight * 0.75).toInt()  // Đặt chiều cao mặc định 3/4 màn hình
                behavior.isFitToContents = false  // Cho phép vuốt lên full màn hình
                behavior.expandedOffset = 0       // Khi vuốt lên, full màn hình
                behavior.isHideable = true        // Vuốt xuống để loại bỏ

            }
        }
    }

    private fun incrementCommentCount(postId: String) {
        val postRef = MarketGreenFirebaseApp[requireContext()].getDataPostArticleFromFirebaseDatabaseReference()
            .child(postId)

        postRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: MutableData): com.google.firebase.database.Transaction.Result {
                val currentComments = currentData.child("quantityComment").getValue(Int::class.java) ?: 0
                currentData.child("quantityComment").value = currentComments + 1
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed) {
                    println("Updated quantityComment successfully.")
                } else {
                    println("Failed to update quantityComment: ${error?.message}")
                }
            }
        })
    }
    private fun decrementCommentCount(postId: String) {
        val postRef = MarketGreenFirebaseApp[requireContext()].getDataPostArticleFromFirebaseDatabaseReference()
            .child(postId)

        postRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: MutableData): com.google.firebase.database.Transaction.Result {
                val currentComments = currentData.child("quantityComment").getValue(Int::class.java) ?: 0
                if (currentComments > 0) {
                    currentData.child("quantityComment").value = currentComments - 1
                }
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed) {
                    println("Updated quantityComment successfully.")
                } else {
                    println("Failed to update quantityComment: ${error?.message}")
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
/*
runTransaction
    Đảm bảo việc đọc-ghi dữ liệu là an toàn và nhất quán trong các tình huống có nhiều người dùng đồng thời.
    (Nếu một người dùng khác thay đổi dữ liệu cùng lúc, Firebase sẽ tự động thử lại quá trình transaction cho đến khi hoàn tất thành công hoặc thất bại.)
    Xử lý đúng logic cập nhật dựa trên dữ liệu hiện tại.
    (Transaction cho phép bạn đọc giá trị hiện tại của dữ liệu (currentData) và sử dụng nó để tính toán giá trị mới--->
    Điều này tránh được lỗi ghi đè (overwrite) dữ liệu do hai hoặc nhiều người dùng cùng thao tác.)
    Tránh lỗi ghi đè dữ liệu không mong muốn.
 */