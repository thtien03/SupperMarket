package com.example.marketgreenapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.databinding.LayoutItemCategorieBinding
import com.example.marketgreenapp.listener.IOnClickItemCategory
import com.example.marketgreenapp.model.AnswerHelp
import com.example.marketgreenapp.model.Category

class CategoryAdapter(private var mListCategories:MutableList<Category>?,
                      private val iOnClickItemCategory: IOnClickItemCategory)
    : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemCategoryBinding = LayoutItemCategorieBinding
                .inflate(LayoutInflater.from(parent.context),parent,false)
        return CategoryViewHolder(itemCategoryBinding)
    }

    override fun getItemCount(): Int {
        return mListCategories?.size ?: 0
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryCurrent = mListCategories?.get(position)
        if(categoryCurrent != null)
        {
            holder.bindData(categoryCurrent)
            holder.itemBinding?.constraintLayoutCategories?.setOnClickListener {
                iOnClickItemCategory.onClickItem(categoryCurrent)
            }
        }
    }
    inner class CategoryViewHolder( val itemBinding:LayoutItemCategorieBinding?) : RecyclerView.ViewHolder(itemBinding!!.root)
    {
        fun bindData(category: Category)
        {
            itemBinding?.txtCategories?.text = category.name
            itemBinding?.imgCategories?.setImageResource(category.image!!)
        }
    }
}