package com.example.marketgreenapp.shop

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.marketgreenapp.PostArticleActivity
import com.example.marketgreenapp.R
import com.example.marketgreenapp.adapter.SecondaryAdapter
import com.example.marketgreenapp.adapter.VariationAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.ActivityAddOrUpdateProductBinding
import com.example.marketgreenapp.listener.IOnClickItemImageSecondary
import com.example.marketgreenapp.model.Category
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.model.Variation
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class AddOrUpdateProductActivity : AppCompatActivity() {
    private lateinit var addOrUpdateProductBinding: ActivityAddOrUpdateProductBinding
    private var imageChooserLauncher: ActivityResultLauncher<Intent>? = null
    private var imageDetailChooserLauncher: ActivityResultLauncher<Intent>? = null
    private var mListImageDetail: MutableList<String>? = null
    private lateinit var secondaryAdapter: SecondaryAdapter

    private var mListVariation: MutableList<Variation>? = null
    private lateinit var variationAdapter: VariationAdapter
    private var currentImageType: String? = null

    private var mCategory: Category? = null
    private var isCheck1 = false

    private var selectedImagePrimary: String? = null
    private var imageUrlPrimary: String? = null

    private var selectedImageDescription: String? = null
    private var imageUrlDescription: String? = null

    private var selectedImageDetail: String? = null
    private var imageUrlDetail: String? = null


    private var selectedSize = ""
    private var selectedColor = ""

    private var isSpinnerSize = false

    private var loadingDialog: Dialog? = null
    private var productEditAvailable: Product? = null

    private var activityResultLauncherCategory = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val category =
                    result.data!!.getSerializableExtra(ConstantKey.KEY_CATEGORY_SHOP) as Category?
                category?.let {
                    mCategory = it
                    bindDataCategory()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOrUpdateProductBinding = ActivityAddOrUpdateProductBinding.inflate(layoutInflater)
        setContentView(addOrUpdateProductBinding.root)

        getDataIntent()
        bindDataCategory()
        initDataSpinner()
        solutionGetImage()
        solutionGetImageDetail()
        solutionRemoveImage()
        setDataImageDetailToRecyclerView()
        setDataSizeColorToRecyclerView()
        addOrUpdateProductBinding.imgBack.setOnClickListener { this.finish() }
        addOrUpdateProductBinding.imgNext.setOnClickListener { openCategoryFb() }
        addOrUpdateProductBinding.imgAddCategory.setOnClickListener { openCategoryFb() }
        addOrUpdateProductBinding.btnAddProduct.setOnClickListener {
            addOrUpdateProduct()
        }
        addOrUpdateProductBinding.imgOther.setOnClickListener { openCategoryFb() }

        addOrUpdateProductBinding.layoutAddImagePrimary.setOnClickListener {
            currentImageType = "primary"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this@AddOrUpdateProductActivity, Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@AddOrUpdateProductActivity,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        PostArticleActivity.REQUEST_CODE_STORAGE_PERMISSION
                    )
                } else {
                    selectImage()
                }
            }
        }
        addOrUpdateProductBinding.imgPrimaryDescription.setOnClickListener {
            currentImageType = "description"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this@AddOrUpdateProductActivity, Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@AddOrUpdateProductActivity,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        PostArticleActivity.REQUEST_CODE_STORAGE_PERMISSION
                    )
                } else {
                    selectImage()
                }
            }
        }
        addOrUpdateProductBinding.layoutAddImageSecondary.setOnClickListener {
            currentImageType = "detail"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this@AddOrUpdateProductActivity, Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@AddOrUpdateProductActivity,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        PostArticleActivity.REQUEST_CODE_STORAGE_PERMISSION
                    )
                } else {
                    selectImage()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDataIntent() {
        val getProductIntent = intent.getSerializableExtra(ConstantKey.KEY_PRODUCT) as Product?
        if (getProductIntent != null) {
            getDataCategory(getProductIntent.category)
            productEditAvailable = getProductIntent
            imageUrlPrimary = productEditAvailable!!.imagePrimary
            imageUrlDescription = productEditAvailable!!.imageDescription
            setProductEditUpdateToView(getProductIntent)

            addOrUpdateProductBinding.btnAddProduct.text = "Chỉnh sửa"
        }
    }

    private fun getDataCategory(category: Int) {
        val mListCategory = FunctionGlobal.getDataCategories(this)
        for (item in mListCategory) {
            if (item.id == category) {
                mCategory = item
                break
            }
        }
    }

    private fun setProductEditUpdateToView(getProductIntent: Product) {
        addOrUpdateProductBinding.edtInputNameProduct.setText(getProductIntent.titleProduct.toString())
        addOrUpdateProductBinding.edtInputPrice.setText(getProductIntent.price.toString())

        if (getProductIntent.imagePrimary != null) {
            GlideImageURL.loadImageURL(
                getProductIntent.imagePrimary,
                addOrUpdateProductBinding.imgPrimary
            )
            addOrUpdateProductBinding.imgRemoveImgPrimary.visibility = View.VISIBLE
        }
        if (getProductIntent.imageDescription != null) {
            GlideImageURL.loadImageURL(
                getProductIntent.imageDescription,
                addOrUpdateProductBinding.imgPrimaryDescription
            )
            addOrUpdateProductBinding.imgRemoveImgDescription.visibility = View.VISIBLE
        }
        addOrUpdateProductBinding.edtInputDescription.setText(getProductIntent.description.toString())
        addOrUpdateProductBinding.edtInputQuantity.setText(getProductIntent.quantity.toString())

        if (getProductIntent.imagesDetails.isNotEmpty()) {
            if (mListImageDetail == null) mListImageDetail = mutableListOf()
            else mListImageDetail!!.clear()
            mListImageDetail!!.addAll(getProductIntent.imagesDetails)
            setDataImageDetailToRecyclerView()
        }
        if (getProductIntent.variations.isNotEmpty()) {
            if (mListVariation == null) mListVariation = mutableListOf()
            else mListVariation!!.clear()

            mListVariation!!.addAll(getProductIntent.variations)
            setDataSizeColorToRecyclerView()
        }
    }

    fun parsePrice(priceString: String): Long {
        val sanitizedPrice = priceString.replace(".", "").replace("VNĐ", "").trim()
        return sanitizedPrice.toLong() / 1000
    }

    private fun initDataSpinner() {
        val sizeSpinner =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, FunctionGlobal.sizeClothes)
        sizeSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val sizeSpinnerShoes =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, FunctionGlobal.sizeShoes)
        sizeSpinnerShoes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        addOrUpdateProductBinding.spinnerSize.adapter = sizeSpinner
        addOrUpdateProductBinding.spinnerSizeShoes.adapter = sizeSpinnerShoes

        val color =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, FunctionGlobal.colors)
        color.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        addOrUpdateProductBinding.spinnerColor.adapter = color


        val sizes = FunctionGlobal.sizeClothes
        val sizesShoes = FunctionGlobal.sizeShoes
        val colors = FunctionGlobal.colors

        selectedSize = if (isSpinnerSize)
            sizesShoes[0]
        else
            sizes[0]

        selectedColor = colors[0]

        addOrUpdateProductBinding.spinnerSize.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSize = sizes[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
        addOrUpdateProductBinding.spinnerSizeShoes.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSize = sizesShoes[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        addOrUpdateProductBinding.spinnerColor.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedColor = colors[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    private fun openCategoryFb() {
        val intent = Intent(this@AddOrUpdateProductActivity, ShopCategoryActivity::class.java)
        val pairs: Array<Pair<View, String>> = arrayOf(
            Pair(addOrUpdateProductBinding.layoutRoot, "transition_drawer")
        )
        val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@AddOrUpdateProductActivity,
            *pairs
        )
        option.update(
            ActivityOptionsCompat.makeCustomAnimation(
                this@AddOrUpdateProductActivity,
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        )
        activityResultLauncherCategory.launch(intent, option)
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
                        val tempFile = createFileFromBitmap(bitmap)
                        tempFile?.let {
                            if (currentImageType == "primary") {

                                selectedImagePrimary = tempFile.path
                                addOrUpdateProductBinding.imgPrimary.setImageBitmap(bitmap)
                                addOrUpdateProductBinding.imgRemoveImgPrimary.visibility =
                                    View.VISIBLE
                                uploadImage(selectedImagePrimary!!) { imageUrl ->
                                    imageUrlPrimary = imageUrl
                                }
                            } else if (currentImageType == "description") {
                                selectedImageDescription = tempFile.path
                                addOrUpdateProductBinding.imgPrimaryDescription.setImageBitmap(
                                    bitmap
                                )
                                addOrUpdateProductBinding.imgRemoveImgDescription.visibility =
                                    View.VISIBLE
                                uploadImage(selectedImageDescription!!) { imageUrl ->
                                    imageUrlDescription = imageUrl
                                }
                            }
                        }
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                } else {
                    Toast.makeText(this@AddOrUpdateProductActivity, "null", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun solutionGetImageDetail() {
        imageDetailChooserLauncher = registerForActivityResult(
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
                        val tempFile = createFileFromBitmap(bitmap)
                        tempFile?.let {
                            lifecycleScope.launch {
                                try {
                                    val imageUrl = uploadImageCoroutines(it.path)
                                    imageUrlDetail = imageUrl
                                    if (mListImageDetail == null) mListImageDetail = mutableListOf()
                                    mListImageDetail!!.add(0, imageUrl)
                                    secondaryAdapter.notifyItemInserted(0)
                                    setDataImageDetailToRecyclerView()

                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                } else {
                    Toast.makeText(this@AddOrUpdateProductActivity, "null", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun solutionRemoveImage() {
        addOrUpdateProductBinding.imgRemoveImgPrimary.setOnClickListener {
            addOrUpdateProductBinding.imgPrimary.setImageResource(R.drawable.invalid_image)
            addOrUpdateProductBinding.imgRemoveImgPrimary.visibility = View.GONE
            selectedImagePrimary = ""
            imageUrlPrimary = ""
        }
        addOrUpdateProductBinding.imgRemoveImgDescription.setOnClickListener {
            addOrUpdateProductBinding.imgPrimaryDescription.setImageResource(R.drawable.invalid_image)
            addOrUpdateProductBinding.imgRemoveImgDescription.visibility = View.GONE
            selectedImageDescription = ""
            imageUrlDescription = ""
        }
    }

    private fun setDataImageDetailToRecyclerView() {
        if (mListImageDetail == null) mListImageDetail = mutableListOf()
        if (mListImageDetail!!.isEmpty()) {
            addOrUpdateProductBinding.imgInvalidSecondary.visibility = View.VISIBLE
            addOrUpdateProductBinding.rcvImageSecondary.visibility = View.GONE
        } else {
            addOrUpdateProductBinding.imgInvalidSecondary.visibility = View.GONE
            addOrUpdateProductBinding.rcvImageSecondary.visibility = View.VISIBLE
        }
        secondaryAdapter = SecondaryAdapter(mListImageDetail, object : IOnClickItemImageSecondary {
            override fun onClickItemImage(string: String, position: Int) {
                if (position >= 0 && position < mListImageDetail!!.size) {
                    mListImageDetail!!.removeAt(position)
                    secondaryAdapter.notifyItemRemoved(position)
                    secondaryAdapter.notifyItemRangeChanged(position, mListImageDetail!!.size)
                }
            }
        })
        addOrUpdateProductBinding.rcvImageSecondary.setHasFixedSize(false)
        addOrUpdateProductBinding.rcvImageSecondary.layoutManager =
            GridLayoutManager(this@AddOrUpdateProductActivity, 2)
        addOrUpdateProductBinding.rcvImageSecondary.adapter = secondaryAdapter
    }

    private fun setDataSizeColorToRecyclerView() {
        if (mListVariation == null) mListVariation = mutableListOf()
        variationAdapter =
            VariationAdapter(mListVariation, object : VariationAdapter.IOnClickVariation {
                override fun onClickVariation(variation: Variation, position: Int) {
                    if (position >= 0 && position < mListVariation!!.size) {
                        mListVariation!!.remove(variation)
                        variationAdapter.notifyItemRemoved(position)
                        variationAdapter.notifyItemRangeChanged(position, mListVariation!!.size)
                    }
                }
            })
        addOrUpdateProductBinding.rcvSizeColor.setHasFixedSize(false)
        addOrUpdateProductBinding.rcvSizeColor.layoutManager = LinearLayoutManager(this)
        addOrUpdateProductBinding.rcvSizeColor.adapter = variationAdapter
    }

    private fun bindDataCategory() {
        if (mCategory != null) {
            isCheck1 = true
            setConstraintLayoutColorSize(mCategory!!)
            addOrUpdateProductBinding.constraintLayoutNoCategory.visibility = View.GONE
            addOrUpdateProductBinding.cardViewCategory.visibility = View.VISIBLE

            addOrUpdateProductBinding.imgCategory.setImageResource(mCategory!!.image!!)
            addOrUpdateProductBinding.txtCategory.text = mCategory!!.name
        } else {
            isCheck1 = false
            addOrUpdateProductBinding.constraintLayoutNoCategory.visibility = View.VISIBLE
            addOrUpdateProductBinding.cardViewCategory.visibility = View.GONE
            addOrUpdateProductBinding.constraintLayoutColorSize.visibility = View.GONE

        }
    }

    private fun setConstraintLayoutColorSize(mCategory: Category) {
        if (mCategory.id == 7 || mCategory.id == 8) {
            addOrUpdateProductBinding.constraintLayoutColorSize.visibility = View.VISIBLE
            if (productEditAvailable == null)
                addOrUpdateProductBinding.edtInputQuantity.setText("0")
            addOrUpdateProductBinding.edtInputQuantity.isEnabled = false
            if (mCategory.id == 7) {
                addOrUpdateProductBinding.spinnerSize.visibility = View.VISIBLE
                addOrUpdateProductBinding.spinnerSizeShoes.visibility = View.GONE
                isSpinnerSize = false
            } else if (mCategory.id == 8) {
                addOrUpdateProductBinding.spinnerSize.visibility = View.GONE
                addOrUpdateProductBinding.spinnerSizeShoes.visibility = View.VISIBLE
                isSpinnerSize = true
            }
        } else {
            addOrUpdateProductBinding.constraintLayoutColorSize.visibility = View.GONE
            if (productEditAvailable == null)
                addOrUpdateProductBinding.edtInputQuantity.setText("")
            addOrUpdateProductBinding.edtInputQuantity.isEnabled = true
        }

        addOrUpdateProductBinding.btnAddSizeColor.setOnClickListener {
            val quantitySizeColor =
                addOrUpdateProductBinding.edtInputQuanlityColorSize.text.toString()
            solutionAddSizeColor(selectedSize, selectedColor, quantitySizeColor)
        }
    }


    private fun solutionAddSizeColor(
        selectedSize: String,
        selectedColor: String,
        quantitySizeColor: String
    ) {

        if (checkDataSizeColor(selectedSize, selectedColor, quantitySizeColor)) {

            val existingVariation = mListVariation!!.find {
                it.size == selectedSize && it.color == selectedColor
            }
            if (existingVariation != null) {
                existingVariation.quantity += quantitySizeColor.toInt()
                val position = mListVariation!!.indexOf(existingVariation)
                variationAdapter.notifyItemChanged(position)
            } else {
                val newVariation = Variation(selectedSize, selectedColor, quantitySizeColor.toInt())
                mListVariation!!.add(0, newVariation)
                variationAdapter.notifyItemInserted(0)
            }
            addOrUpdateProductBinding.edtInputQuanlityColorSize.setText("")
            addOrUpdateProductBinding.spinnerColor.setSelection(0)
            addOrUpdateProductBinding.spinnerSizeShoes.setSelection(0)
            addOrUpdateProductBinding.spinnerSize.setSelection(0)
        }
    }

    private fun checkDataSizeColor(
        selectedSize: String,
        selectedColor: String,
        quantitySizeColor: String
    ): Boolean {
        if (TextUtils.isEmpty(selectedSize)) {
            showMessage("Vui lòng chọn size")
            return false
        } else if (TextUtils.isEmpty(selectedColor)) {
            showMessage("Vui lòng chọn màu")
            return false
        } else if (TextUtils.isEmpty(quantitySizeColor)) {
            showNotificationDialog(Gravity.CENTER)
            return false
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun showNotificationDialog(gravity: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_item_dialog_warning)

        val btnAccept: Button = dialog.findViewById(R.id.btn_accept)
        val txtContent: TextView = dialog.findViewById(R.id.txt_content)
        txtContent.text =
            "Vui lòng nhập số lượng"
        btnAccept.setOnClickListener {
            dialog.dismiss()
        }
        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(0))
        val windowAttribute = window.attributes
        windowAttribute.gravity = gravity

        window.attributes = windowAttribute
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showMessage(strMessage: String) {
        Toast.makeText(this@AddOrUpdateProductActivity, strMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PostArticleActivity.REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
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
        if (currentImageType == "detail")
            imageDetailChooserLauncher!!.launch(intent)
        else
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
            Toast.makeText(this, "Không thể tạo file từ Bitmap: ${e.message}", Toast.LENGTH_SHORT)
                .show()
            null
        }
    }

    private fun uploadImage(filePath: String, callback: (String) -> Unit) {
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
                    callback(imageUrl)
                    //imageUrlPrimary = imageUrl
                    //println("Image URL: $imageUrl")
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                }
            })
            .dispatch()
    }

    private suspend fun uploadImageCoroutines(filePath: String): String =
        withContext(Dispatchers.IO) {
            val completableDeferred = CompletableDeferred<String>()

            MediaManager.get().upload(filePath)
                .callback(object : UploadCallback {
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url").toString()
                        completableDeferred.complete(imageUrl) // Trả về kết quả khi thành công
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        completableDeferred.completeExceptionally(Exception("Upload thất bại: ${error?.description}"))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        completableDeferred.completeExceptionally(Exception("Upload bị dừng: ${error?.description}"))
                    }

                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                })
                .dispatch()

            completableDeferred.await() // Chờ kết quả từ upload
        }


    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION: Int = 123
    }

    private fun checkData(
        nameProduct: String,
        priceProduct: String,
        descriptionProduct: String,
        quantityProduct: String
    ): Boolean {

        if (TextUtils.isEmpty(nameProduct)) {
            addOrUpdateProductBinding.edtInputNameProduct.error = "*Nhập tên sản phẩm"
            return false
        } else if (TextUtils.isEmpty(priceProduct)) {
            addOrUpdateProductBinding.edtInputPrice.error = "*Nhập giá sản phẩm"
            return false
        } else if (TextUtils.isEmpty(descriptionProduct)) {
            addOrUpdateProductBinding.edtInputDescription.error = "*Nhập mô tả sản phẩm"
            return false
        } else if (TextUtils.isEmpty(quantityProduct)) {
            addOrUpdateProductBinding.edtInputQuantity.error = "*Nhập số lượng"
            return false
        }
        return true
    }

    private fun addOrUpdateProduct() {

        if(mListImageDetail == null) mListImageDetail = mutableListOf()
        if(mListVariation == null) mListVariation = mutableListOf()
        val nameProduct = addOrUpdateProductBinding.edtInputNameProduct.text.toString()
        val priceProduct = addOrUpdateProductBinding.edtInputPrice.text.toString()
        val descriptionProduct = addOrUpdateProductBinding.edtInputDescription.text.toString()
        val quantityProduct = addOrUpdateProductBinding.edtInputQuantity.text.toString()

        if (checkData(nameProduct, priceProduct, descriptionProduct, quantityProduct)) {
            if (isCheck1) {
                if (productEditAvailable != null) {
                    //tôi cần update post sau đó trả lại dữ liệu
                    productEditAvailable!!.apply {
                        this.titleProduct = nameProduct
                        this.price = priceProduct
                        this.description = descriptionProduct
                        this.quantity = quantityProduct.toInt()
                        this.imagePrimary = imageUrlPrimary
                        this.imageDescription = imageUrlDescription
                        this.imagesDetails = mListImageDetail!!
                        this.variations = mListVariation!!
                        this.category = mCategory!!.id
                        this.categoryName = mCategory!!.name
                    }
                    // Cập nhật sản phẩm trong Firebase
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            MarketGreenFirebaseApp[this@AddOrUpdateProductActivity]
                                .getDataProductFromFirebaseDatabaseReference()
                                .child(productEditAvailable!!.productId.toString())
                                .setValue(productEditAvailable).await()

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@AddOrUpdateProductActivity,
                                    "Cập nhật sản phẩm thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val resultIntent = Intent()
                                resultIntent.putExtra(ConstantKey.KEY_PRODUCT, productEditAvailable)
                                resultIntent.putExtra(
                                    ConstantKey.KEY_ACTION_PRODUCT,
                                    ConstantKey.KEY_UPDATE_PRODUCT
                                )
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@AddOrUpdateProductActivity,
                                    "Cập nhật sản phẩm thất bại",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                else
                {
                    val productId = System.currentTimeMillis()
                    val userDataManager = DataStoreManager.getUser()
                    showDialog(true)
                    if (userDataManager?.uid != null) {
                        val newProduct = Product(
                            productId,
                            imageUrlPrimary,
                            nameProduct,
                            descriptionProduct,
                            imageUrlDescription,
                            priceProduct,
                            quantityProduct.toInt(),
                            0,
                            mCategory!!.id,
                            mCategory!!.name,
                            userDataManager.uid,
                            userDataManager.fullname,
                            mListImageDetail!!,
                            0f,
                            0,
                            mListVariation!!
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                MarketGreenFirebaseApp[this@AddOrUpdateProductActivity]
                                    .getDataProductFromFirebaseDatabaseReference()
                                    .child(productId.toString())
                                    .setValue(newProduct).await()
                                withContext(Dispatchers.Main)
                                {
                                    showDialog(false)
                                    resetData()
                                    Toast.makeText(
                                        this@AddOrUpdateProductActivity,
                                        "Thêm sản phẩm thành công",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val resultIntent = Intent()
                                    resultIntent.putExtra(ConstantKey.KEY_PRODUCT, newProduct)
                                    resultIntent.putExtra(
                                        ConstantKey.KEY_ACTION_PRODUCT,
                                        ConstantKey.KEY_ADD_PRODUCT
                                    )
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this@AddOrUpdateProductActivity,
                    "Category no choose",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun resetData() {
        addOrUpdateProductBinding.edtInputQuantity.setText("")
        addOrUpdateProductBinding.edtInputNameProduct.setText("")
        addOrUpdateProductBinding.edtInputPrice.setText("")
        addOrUpdateProductBinding.edtInputDescription.setText("")
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