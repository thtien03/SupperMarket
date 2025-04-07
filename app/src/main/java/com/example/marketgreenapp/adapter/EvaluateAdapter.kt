package com.example.marketgreenapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.databinding.LayoutItemEvaluateBinding
import com.example.marketgreenapp.model.Evaluate


class EvaluateAdapter(private val listEvaluate:List<Evaluate>?) : RecyclerView.Adapter<EvaluateAdapter.EvaluateViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvaluateViewHolder {
        val binding: LayoutItemEvaluateBinding =
            LayoutItemEvaluateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EvaluateViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listEvaluate?.size ?: 0
    }

    override fun onBindViewHolder(holder: EvaluateViewHolder, position: Int) {
        holder.bindData(listEvaluate!![position])
    }

    inner class EvaluateViewHolder(private val binding: LayoutItemEvaluateBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bindData(evaluate: Evaluate)
            {
                binding.apply {
                    this.txtName.text = evaluate.name
                    this.txtDatePost.text = evaluate.date
                    this.txtContent.text = evaluate.message
                    this.ratingbar.rating = evaluate.numberStart
                }
            }
    }
}