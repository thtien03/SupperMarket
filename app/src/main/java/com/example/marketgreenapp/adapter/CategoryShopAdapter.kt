package com.example.marketgreenapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.databinding.LayoutItemCategoryShopBinding
import com.example.marketgreenapp.listener.IOnClickItemCategory
import com.example.marketgreenapp.model.Category

class CategoryShopAdapter (private var mListCategories:MutableList<Category>?,
                           private val iOnClickItemCategory: IOnClickItemCategory)
    : RecyclerView.Adapter<CategoryShopAdapter.CategoryShopViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryShopAdapter.CategoryShopViewHolder {
        val categoryShopBinding:LayoutItemCategoryShopBinding?
                = LayoutItemCategoryShopBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CategoryShopViewHolder(categoryShopBinding)
    }

    override fun getItemCount(): Int {
        return mListCategories?.size ?: 0
    }

    override fun onBindViewHolder(holder: CategoryShopAdapter.CategoryShopViewHolder, position: Int) {
        val currentCategory = mListCategories?.get(position)
        if(currentCategory != null)
        {
            holder.bindData(currentCategory)
            holder.categoryShopBinding?.cardViewCategory?.setOnClickListener {
                iOnClickItemCategory.onClickItem(currentCategory)
            }
            holder.categoryShopBinding?.imgNext?.setOnClickListener {
                iOnClickItemCategory.onClickItem(currentCategory)
            }
        }
    }
    inner class CategoryShopViewHolder(val categoryShopBinding:LayoutItemCategoryShopBinding?)
        : RecyclerView.ViewHolder(categoryShopBinding!!.root)
    {
        fun bindData(category:Category)
        {
            categoryShopBinding?.txtCategory?.text = category.name
            categoryShopBinding?.imgCategory?.setImageResource(category.image!!)
        }
    }
}