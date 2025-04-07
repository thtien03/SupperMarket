package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.R
import com.example.marketgreenapp.databinding.LayoutItemColorProductBinding
import com.example.marketgreenapp.model.Variation

class ColorProductAdapter(
    private val variations: List<Variation>,
    private var selectedColor: String?,
    private val onColorSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorProductAdapter.ColorProductViewHolder>() {


    inner class ColorProductViewHolder(val binding: LayoutItemColorProductBinding?) :
        RecyclerView.ViewHolder(binding!!.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorProductViewHolder {
        val colorBinding: LayoutItemColorProductBinding = LayoutItemColorProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColorProductViewHolder(colorBinding)
    }

    override fun getItemCount(): Int {
        return variations.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ColorProductViewHolder, position: Int) {
        val color = variations[position].color
        val quantity = variations[position].quantity
        holder.binding!!.txtColor.text = color

        if (quantity > 0) {
            holder.binding.layoutRootColor.setBackgroundResource(
                if (color == selectedColor) R.drawable.bg_background_variation_selected else R.drawable.bg_background_variation_unselected
            )
            holder.binding.txtColor.setTextColor(Color.BLACK)
            holder.binding.txtColor.setTextColor(
                if (color == selectedColor) Color.WHITE else Color.BLACK
            )
            holder.binding.layoutRootColor.isEnabled = true
        } else {
            holder.binding.layoutRootColor.setBackgroundResource(R.drawable.bg_disabled)
            holder.binding.txtColor.setTextColor(Color.GRAY)
            holder.binding.layoutRootColor.isEnabled = false
        }
        /*holder.binding.root.setOnClickListener {
        onColorClick(colorCurrent)
        }
        if (selectedColor == colorCurrent) {
            holder.binding.layoutRootColor.setBackgroundResource(R.drawable.bg_background_variation_selected)
            //holder.binding.txtColor.setBackgroundColor(R.drawable.bg_background_variation_selected)
            holder.binding.txtColor.setTextColor(Color.WHITE)
        } else {
            holder.binding.layoutRootColor.setBackgroundResource(R.drawable.bg_background_variation_unselected)
            //holder.binding.txtColor.setBackgroundColor(R.drawable.bg_background_variation_unselected)
            holder.binding.txtColor.setTextColor(Color.BLACK)
        }*/

        holder.itemView.setOnClickListener {
            if (quantity > 0) {
                selectedColor = color
                onColorSelected(color!!)
                notifyDataSetChanged()
            }
        }
    }
}