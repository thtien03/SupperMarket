package com.example.marketgreenapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.ActivityPostArticleBinding
import com.example.marketgreenapp.databinding.LayoutOptionArticleBinding
import com.example.marketgreenapp.model.Post
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PostArticleActivity : AppCompatActivity() {
    private var postBinding: ActivityPostArticleBinding? = null
    private lateinit var bottomSheetBinding: LayoutOptionArticleBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var imageChooserLauncher: ActivityResultLauncher<Intent>? = null

    private lateinit var handler: Handler
    private lateinit var updateTimeRunnable: Runnable

    private var selectedPostColor: String? = null
    private var selectedImagePost: String? = null
    private var imageUrlPost: String? = null

    private lateinit var colorItems: List<Pair<View, ImageView>>
    private lateinit var colorResIds: List<Int>

    private var postEditAvailable: Post? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (postBinding == null)
            postBinding = ActivityPostArticleBinding.inflate(layoutInflater)
        setContentView(postBinding!!.root)


        val dataUser = DataStoreManager.getUser()
        if (dataUser != null) {
            postBinding!!.txtNameUser.text = dataUser.fullname
        }
        getDataIntent()
        postBinding!!.imgBack.setOnClickListener {
            this.finish()
            overridePendingTransition(0, R.anim.slide_out_bottom);
        }
        initDatePost()
        // Truy xuất và thao tác với layout được include
        bottomSheetBinding = LayoutOptionArticleBinding.bind(postBinding!!.layoutOptionArticle.root)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetBinding.root)

        postBinding!!.imgRemoveImgPost.setOnClickListener {
            postBinding!!.imgPost.setImageBitmap(null)
            postBinding!!.imgPost.visibility = View.GONE
            postBinding!!.imgRemoveImgPost.visibility = View.GONE
            selectedImagePost = ""
            imageUrlPost = ""
        }
        solutionGetImage()
        eventHideSoftKeyboard()
        initColorAndImagePost()
        initColorOption()
        handlerOptionBottomSheetBehavior()
        setSubtitleIndicatorColor()

        postBinding!!.btnPost.setOnClickListener {
            createNewOrUpdatePost()
        }
    }

    private fun getDataIntent() {
        if (intent.getBooleanExtra(ConstantKey.KEY_BOOLEAN_EDIT_POST, false)) {
            val getPostIntent = intent.getSerializableExtra(ConstantKey.KEY_POST) as Post?
            if (getPostIntent != null) {
                postEditAvailable = getPostIntent
                setPostUpdateToView(getPostIntent)
            }
        }
    }

    private fun Activity.showMessage(str: String) {
        Toast.makeText(this@PostArticleActivity, str, Toast.LENGTH_SHORT).show()
    }

    private fun checkData(title: String, content: String): Boolean {
        if (TextUtils.isEmpty(title)) {
            showMessage("Vui lòng nhập tiêu đề...")
            return false
        } else if (TextUtils.isEmpty(content)) {
            showMessage("Vui lòng nhập nội dung...")
            return false
        }
        return true
    }

    private fun setPostUpdateToView(getPostIntent: Post) {
        postBinding?.btnPost?.text = "Chỉnh sửa"
        postBinding?.edtPostTitle?.setText(getPostIntent.title.toString())
        postBinding?.edtContentPost?.setText(getPostIntent.content.toString())
        if (getPostIntent.imageUrl != null && getPostIntent.imageUrl?.isNotEmpty() == true) {
            imageUrlPost = getPostIntent.imageUrl
            postBinding?.imgPost?.visibility = View.VISIBLE
            GlideImageURL.loadImageURL(getPostIntent.imageUrl, postBinding?.imgPost)
            postBinding?.imgRemoveImgPost?.visibility = View.VISIBLE
        }
    }

    private fun createNewOrUpdatePost() {
        println("KAAAAAAAAAAAAAAAAAAAAAAAA=====${imageUrlPost}")
        val edtTitlePost = postBinding!!.edtPostTitle.text.toString().trim { it <= ' ' }
        val edtContentPost = postBinding!!.edtContentPost.text.toString().trim { it <= ' ' }
        val tvDatePost = postBinding!!.txtTimePost.text.toString().trim { it <= ' ' }

        val user = DataStoreManager.getUser()
        if (user != null) {
            if (checkData(edtTitlePost, edtContentPost)) {
                if (postEditAvailable != null) {
                    //tôi cần update post sau đó trả lại dữ liệu
                    postEditAvailable!!.apply {
                        this.title = edtTitlePost
                        this.content = edtContentPost
                        this.colorBackground = selectedPostColor
                        this.imageUrl = imageUrlPost
                    }
                    val resultIntent = Intent()
                    resultIntent.putExtra(ConstantKey.KEY_POST, postEditAvailable)
                    resultIntent.putExtra(ConstantKey.KEY_ACTION_POST, ConstantKey.VALUE_EDIT_POST)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                    overridePendingTransition(0, R.anim.slide_out_bottom)
                } else {
                    val uid = System.currentTimeMillis()
                    val post = Post(
                        uid,
                        edtTitlePost,
                        edtContentPost,
                        imageUrlPost,
                        tvDatePost,
                        selectedPostColor,
                        0,
                        0,
                        user.role,
                        user.uid,
                        user.fullname,
                    )
                    val resultIntent = Intent()
                    resultIntent.putExtra(ConstantKey.KEY_POST, post)
                    resultIntent.putExtra(ConstantKey.KEY_ACTION_POST, ConstantKey.VALUE_ADD_POST)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                    overridePendingTransition(0, R.anim.slide_out_bottom)
                }
            }
        }
    }

    private fun solutionGetImage() {
        imageChooserLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    try {
                        val inputStream =
                            contentResolver.openInputStream(selectedImageUri) // mở luồng dữ liệu từ Uri đã chọn
                        val bitmap =
                            BitmapFactory.decodeStream(inputStream) // chuyển đổi luồng dữ liệu thành Bitmap
                        postBinding?.imgPost?.setImageBitmap(bitmap)
                        postBinding?.imgPost?.visibility = View.VISIBLE
                        postBinding?.imgRemoveImgPost?.visibility =
                            View.VISIBLE
                        val tempFile = createFileFromBitmap(bitmap)
                        tempFile?.let {
                            selectedImagePost = tempFile.path
                            uploadImage(selectedImagePost!!)
                        }
                        /*selectedImagePost = getRealPathFromUri2(selectedImageUri)
                        if (selectedImagePost != null) {
                            uploadImage(selectedImagePost!!)
                        } else {
                            Toast.makeText(this@PostArticleActivity, "Không thể lấy đường dẫn hình ảnh", Toast.LENGTH_SHORT).show()
                        }
                        uploadImage(selectedImagePost!!)*/
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }
                else
                {
                    Toast.makeText(this@PostArticleActivity,"null",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun initDatePost() {
        postBinding!!.txtTimePost.visibility = View.VISIBLE
        postBinding!!.txtTimePost.text =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date())
    }

    private fun startUpdatingTime() {
        postBinding!!.txtTimePost.visibility = View.VISIBLE
        handler = Handler(Looper.getMainLooper())
        updateTimeRunnable = object : Runnable {
            override fun run() {
                val currentTime =
                    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                postBinding!!.txtTimePost.text = currentTime
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateTimeRunnable)
    }


    private fun eventHideSoftKeyboard() {

        postBinding!!.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            postBinding!!.root.getWindowVisibleDisplayFrame(rect)

            val screenHeight = postBinding!!.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            //phần bị che lớn hơn 15% chiều cao màn => giả định là bàn phím
            if (keypadHeight > screenHeight * 0.15) {
                bottomSheetBinding.layoutMiscellaneous.visibility = View.GONE
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBinding.layoutMiscellaneous.visibility = View.VISIBLE
            }
        }
    }

    private fun initColorAndImagePost() {
        selectedPostColor = "#1DE9B6"
        selectedImagePost = ""
    }

    private fun initColorOption() {
        colorItems = listOf(
            Pair(bottomSheetBinding.viewColor1, bottomSheetBinding.imageColor1),
            Pair(bottomSheetBinding.viewColor2, bottomSheetBinding.imageColor2),
            Pair(bottomSheetBinding.viewColor3, bottomSheetBinding.imageColor3),
            Pair(bottomSheetBinding.viewColor4, bottomSheetBinding.imageColor4),
            Pair(bottomSheetBinding.viewColor5, bottomSheetBinding.imageColor5),
            Pair(bottomSheetBinding.viewColor6, bottomSheetBinding.imageColor6),
            Pair(bottomSheetBinding.viewColor7, bottomSheetBinding.imageColor7)
        )
        colorResIds = listOf(
            R.color.colorDefaultPostColor,
            R.color.colorDefaultPostColor2,
            R.color.colorDefaultPostColor3,
            R.color.colorDefaultPostColor4,
            R.color.colorDefaultPostColor5,
            R.color.colorDefaultPostColor6,
            R.color.colorDefaultPostColor7
        )
    }

    private fun handlerOptionBottomSheetBehavior() {
        bottomSheetBinding.constraintLayout1.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        colorItems.forEachIndexed { index, pair ->
            val (viewColor, imageColor) = pair
            viewColor.setOnClickListener {
                selectPostColor(
                    colorResIds[index],
                    imageColor,
                    *(colorItems.map { it.second }.filter { it != imageColor }
                        .toTypedArray()) // *..toTypedArray() == thành tham số biến vararg
                )
            }
        }

        if (postEditAvailable != null && postEditAvailable!!.colorBackground != null && postEditAvailable!!.colorBackground?.trim()
                ?.isNotEmpty() == true
        ) {
            when (postEditAvailable!!.colorBackground) {
                "#FDBE3B" -> {
                    bottomSheetBinding.viewColor2.performClick()
                }
                "#FF4842" -> {
                    bottomSheetBinding.viewColor3.performClick()
                }
                "#00E5FF" -> {
                    bottomSheetBinding.viewColor4.performClick()
                }
                "#FFF9C4" -> {
                    bottomSheetBinding.viewColor5.performClick()
                }
                "#00E676" -> {
                    bottomSheetBinding.viewColor6.performClick()
                }
                "#FFFFFF" -> {
                    bottomSheetBinding.viewColor7.performClick()
                }
            }
        }
        bottomSheetBinding.layoutAddImage.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this@PostArticleActivity, Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@PostArticleActivity,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        REQUEST_CODE_STORAGE_PERMISSION
                    )
                } else {
                    selectImage()
                }
            }
        }
    }

    private fun selectPostColor(
        colorResId: Int,
        selectedImageView: ImageView,
        vararg otherImageViews: ImageView
    ) {
        val color = ContextCompat.getColor(this, colorResId)
        selectedPostColor = String.format("#%06X", 0xFFFFFF and color)
        selectedImageView.setImageResource(R.drawable.done_48px)
        otherImageViews.forEach { it.setImageResource(0) }
        setSubtitleIndicatorColor()
    }

    private fun setSubtitleIndicatorColor() {
        val gradientDrawable: GradientDrawable =
            postBinding!!.viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedPostColor))
    }

    private fun getRealPathFromUri1(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        var filePath: String? = null
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = it.getString(columnIndex)
            }
            it.close()
        }
        return filePath
    }
    private fun getRealPathFromUri2(uri: Uri): String? {
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return it.getString(columnIndex)
                }
            }
        } else if (uri.scheme == "file") {
            return uri.path
        }
        return null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) selectImage() else Toast.makeText(
                this,
                "Permission Denied!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("IntentReset")
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        //val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imageChooserLauncher!!.launch(intent)
    }

    private fun createFileFromBitmap(bitmap: Bitmap): File? {
        val cacheDir = cacheDir
        val tempFile = File(cacheDir, "temp_image_${System.currentTimeMillis()}.png")
        return try {
            FileOutputStream(tempFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            tempFile
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể tạo file từ Bitmap: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun uploadImage(filePath: String) {
        MediaManager.get().upload(filePath)
            //.unsigned("preset-name") // Tạo unsigned preset trên Cloudinary ta không cần đưa API KEY và API Secret vào giúp bảo vệ thông tin Settings > Upload.  Upload Presets, nhấp vào Add Upload Preset.
            //Signing Mode: để không yêu cầu API KEY Và API Secret
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val progress = bytes * 100 / totalBytes
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("secure_url").toString()
                    imageUrlPost = imageUrl
                    println("Image URL: $imageUrl")
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                }
            })
            .dispatch()
    }



    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION: Int = 123
    }

    override fun onDestroy() {
        super.onDestroy()
        postBinding = null
    }
}
