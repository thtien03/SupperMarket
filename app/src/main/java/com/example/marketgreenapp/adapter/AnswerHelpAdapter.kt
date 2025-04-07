package com.example.marketgreenapp.adapter

import android.annotation.SuppressLint

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.databinding.LayoutItemHelpBinding
import com.example.marketgreenapp.model.AnswerHelp

class AnswerHelpAdapter(private var mListAnswerHelp:MutableList<AnswerHelp>?,
                        private val iOnClickItemAnswer: IOnClickItemAnswer,)
        : RecyclerView.Adapter<AnswerHelpAdapter.AnswerHelpViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setDataFilter(answerHelp:MutableList<AnswerHelp>?)
    {
        this.mListAnswerHelp = answerHelp
        notifyDataSetChanged()
    }
    interface IOnClickItemAnswer {
        fun selectAnswer(answerHelp: AnswerHelp)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerHelpViewHolder {
        val itemBinding: LayoutItemHelpBinding
            = LayoutItemHelpBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AnswerHelpViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return mListAnswerHelp?.size ?: 0
    }

    override fun onBindViewHolder(holder: AnswerHelpViewHolder, position: Int) {
        val answerHelpCurrent = mListAnswerHelp?.get(position)
        if(answerHelpCurrent != null)
        {
            holder.bindData(answerHelpCurrent)
            holder.itemBinding!!.imgExpanded.setOnClickListener {
                iOnClickItemAnswer.selectAnswer(answerHelpCurrent)
            }
        }
    }
    inner class AnswerHelpViewHolder( val itemBinding:LayoutItemHelpBinding?)
        : RecyclerView.ViewHolder(itemBinding!!.root)
    {
        fun bindData(answerHelp: AnswerHelp)
        {
            itemBinding?.apply {
                this.txtTitle.text = answerHelp.question?.trim()
            }
        }
    }

}