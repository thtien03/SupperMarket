package com.example.marketgreenapp.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.marketgreenapp.R
import com.example.marketgreenapp.adapter.ColorProductAdapter
import com.example.marketgreenapp.adapter.SizeProductAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.GlideImageURL
import com.example.marketgreenapp.databinding.LayoutProductBottomSheetFragmentBinding
import com.example.marketgreenapp.model.Product
import com.example.marketgreenapp.model.ProductOrder
import com.example.marketgreenapp.model.Variation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.util.*

class ProductPaymentBottomSheetDialog : BottomSheetDialogFragment() {

    interface OnProductOrderListener {
        fun onProductOrder(productOrder: ProductOrder)
        fun onProductOrderAddToCart(size: String, color: String, productOrder: ProductOrder)
    }

    private var productBottomSheetBinding: LayoutProductBottomSheetFragmentBinding? = null

    //private var productDetailActivity: ProductDetailActivity? = null
    private var listener: OnProductOrderListener? = null
    private lateinit var productOrder: ProductOrder
    private lateinit var product: Product

    //private var variations: MutableList<Variation> = mutableListOf()
    private var selectedSize: String? = null
    private var selectedColor: String? = null
    private var quantitySizeColor = 0

    private var type: String = ""
    private val binding get() = productBottomSheetBinding!!

    companion object {
        fun newInstance(
            productOrder: ProductOrder,
            product: Product,
            type: String,
            // productDetailActivity: ProductDetailActivity
            listener: OnProductOrderListener
        ): ProductPaymentBottomSheetDialog {
            val productBottomSheetPayment = ProductPaymentBottomSheetDialog()
            val bundle = Bundle()
            bundle.putSerializable(ConstantKey.KEY_PRODUCT_ORDER_BOTTOM_SHEET, productOrder)

            bundle.putSerializable(ConstantKey.KEY_PRODUCT_BOTTOM_SHEET, product)

            productBottomSheetPayment.arguments = bundle
            productBottomSheetPayment.type = type
            //productBottomSheetPayment.productDetailActivity = productDetailActivity
            productBottomSheetPayment.listener = listener
            return productBottomSheetPayment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundleReceive = arguments
        if (bundleReceive != null) {
            productOrder =
                (bundleReceive.get(ConstantKey.KEY_PRODUCT_ORDER_BOTTOM_SHEET) as? ProductOrder)!!

            product =
                (bundleReceive.get(ConstantKey.KEY_PRODUCT_BOTTOM_SHEET) as? Product)!!
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.bg_background_bottom_product_sheet)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog: BottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        productBottomSheetBinding = LayoutProductBottomSheetFragmentBinding.inflate(layoutInflater)
        setDataToBottomSheetDialog(bottomSheetDialog)

        if (type == "AddToCart") {
            productBottomSheetBinding!!.btnAddToCart.visibility = View.VISIBLE
        } else if (type == "Payment") {
            productBottomSheetBinding!!.btnPayment.visibility = View.VISIBLE
        }
        productOrder.quantityBuy = 1
        bottomSheetDialog.setContentView(productBottomSheetBinding!!.root)

        bottomSheetDialog.show()
        return bottomSheetDialog
    }

    private fun updateColorList(variationsProduct: MutableList<Variation>) {
        val filteredVariations =
            variationsProduct.filter { it.size == selectedSize }
        //val colors = filteredVariations.map { it.color }.distinct()
        val colorProductAdapter = ColorProductAdapter(filteredVariations, selectedColor) { color ->
            selectedColor = color
            productBottomSheetBinding!!.relativeQuantity.visibility = View.VISIBLE
            updateQuantity(variationsProduct, color)
        }
        binding.rcvColors.setHasFixedSize(false)
        binding.rcvColors.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rcvColors.adapter = colorProductAdapter
    }

    private fun updateQuantity(variations: MutableList<Variation>, color: String) {
        val selectedVariation =
            variations.find { it.size == selectedSize && it.color == color }
        val availableQuantity = selectedVariation?.quantity ?: 0
        quantitySizeColor = availableQuantity
        Toast.makeText(requireContext(), "Còn lại: $availableQuantity", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    private fun setDataToBottomSheetDialog(bottomSheetDialog: BottomSheetDialog) {

        if (product.variations.isNotEmpty()) {
            binding.constraintLayoutSizeColor.visibility = View.VISIBLE
            binding.constraintLayoutIncreaseNumber.visibility = View.GONE
            val variationsProduct = product.variations
            val uniqueSizes = variationsProduct.map { it.size }.distinct()
            val sizeProductAdapter = SizeProductAdapter(uniqueSizes, selectedSize) { size ->
                selectedSize = size
                binding.viewDividerSizeColor.visibility = View.VISIBLE
                binding.relativeQuantity.visibility = View.GONE
                updateColorList(variationsProduct)
            }
            binding.rcvSizes.setHasFixedSize(false)
            binding.rcvSizes.layoutManager = GridLayoutManager(requireContext(), 3)
            binding.rcvSizes.adapter = sizeProductAdapter
        } else {
            binding.constraintLayoutIncreaseNumber.visibility = View.VISIBLE
            binding.constraintLayoutSizeColor.visibility = View.GONE
        }
        productBottomSheetBinding!!.imgClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        GlideImageURL.loadImageURL(
            productOrder.imagePrimary,
            productBottomSheetBinding!!.imgProduct
        )
        productBottomSheetBinding!!.txtTitleProduct.text = productOrder.titleProduct

        val formattedPrice =
            NumberFormat.getNumberInstance(Locale.GERMANY).format(productOrder.price!!.toInt())
        productBottomSheetBinding!!.txtPrice.text = "${formattedPrice}.000VNĐ"
        productBottomSheetBinding!!.txtNumber.text = productOrder.quantityBuy.toString()

        productBottomSheetBinding!!.btnDecreased.setOnClickListener {
            if (productOrder.quantityBuy > 1) {
                productOrder.quantityBuy -= 1
                productBottomSheetBinding!!.txtNumber.text = productOrder.quantityBuy.toString()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Số lượng tối thiểu là 1",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        productBottomSheetBinding!!.btnIncrease.setOnClickListener {
            if (productOrder.quantityBuy < 10) {
                productOrder.quantityBuy += 1
                productBottomSheetBinding!!.txtNumber.text = productOrder.quantityBuy.toString()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Bạn mua sản phẩm vượt quá số lượng kho hàng rồi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        productBottomSheetBinding!!.btnDecreasedSizeColor.setOnClickListener {
            if (productOrder.quantityBuy > 1) {
                productOrder.quantityBuy -= 1
                productBottomSheetBinding!!.txtNumberSizeColor.text =
                    productOrder.quantityBuy.toString()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Số lượng tối thiểu là 1",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        productBottomSheetBinding!!.btnIncreaseSizeColor.setOnClickListener {
            if (productOrder.quantityBuy < 10 && quantitySizeColor > productOrder.quantityBuy) {
                productOrder.quantityBuy += 1
                productBottomSheetBinding!!.txtNumberSizeColor.text =
                    productOrder.quantityBuy.toString()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Bạn mua sản phẩm vượt quá số lượng kho hàng rồi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        productBottomSheetBinding!!.btnPayment.setOnClickListener {
            if (product.category == 7 || product.category == 8) {
                if (checkData(selectedSize, selectedColor)) {
                    val sizeColor = "Sản phẩm có size : $selectedSize , color : $selectedColor"
                    productOrder.sizeColor = sizeColor
                    bottomSheetDialog.dismiss()
                    listener?.onProductOrder(productOrder)
                }
            } else {
                bottomSheetDialog.dismiss()
                listener?.onProductOrder(productOrder)
            }
            //productDetailActivity!!.gotoProductPayActivity(requireActivity(),productOrder)
        }
        productBottomSheetBinding!!.btnAddToCart.setOnClickListener {
            if (checkData(selectedSize, selectedColor)) {
                val sizeColor = "Sản phẩm có size : $selectedSize , color : $selectedColor"
                productOrder.sizeColor = sizeColor
                bottomSheetDialog.dismiss()
                listener?.onProductOrderAddToCart(selectedSize!!,selectedColor!!,productOrder)
            }
        }
    }

    private fun checkData(selectedSize: String?, selectedColor: String?): Boolean {
        if (TextUtils.isEmpty(selectedSize)) {
            showMessage("Vui lòng chọn size")
            return false
        } else if (TextUtils.isEmpty(selectedColor)) {
            showMessage("Vui lòng chọn màu")
            return false
        }
        return true
    }

    private fun showMessage(str: String) {
        Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        productBottomSheetBinding = null
    }
}