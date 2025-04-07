package com.example.marketgreenapp.shop

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.CategoryShopAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.databinding.ActivityShopCategoryBinding
import com.example.marketgreenapp.listener.IOnClickItemCategory
import com.example.marketgreenapp.model.Category

class ShopCategoryActivity : AppCompatActivity() {
    private var shopCategoryBinding:ActivityShopCategoryBinding? = null
    private var listCategory: MutableList<Category>? = null
    private lateinit var categoryShopAdapter: CategoryShopAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(shopCategoryBinding == null)
            shopCategoryBinding = ActivityShopCategoryBinding.inflate(layoutInflater)
        setContentView(shopCategoryBinding!!.root)
        setCategoryToRecyclerView()
        shopCategoryBinding?.imgBackMain?.setOnClickListener {
            this.finish()
        }
    }
    private fun setCategoryToRecyclerView()
    {
        listCategory = mutableListOf()
        listCategory = FunctionGlobal.getDataCategories(this@ShopCategoryActivity)
        categoryShopAdapter = CategoryShopAdapter(listCategory,object : IOnClickItemCategory {
            override fun onClickItem(category: Category) {
                solution(category)
            }
        })
        shopCategoryBinding?.rcvCategory?.setHasFixedSize(false)
        val gridLayoutManager = LinearLayoutManager(this)
        shopCategoryBinding?.rcvCategory?.layoutManager = gridLayoutManager
        shopCategoryBinding?.rcvCategory?.adapter = categoryShopAdapter

    }
    private fun solution(category: Category)
    {
        val resultIntent = Intent()
        resultIntent.putExtra(ConstantKey.KEY_CATEGORY_SHOP, category)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}