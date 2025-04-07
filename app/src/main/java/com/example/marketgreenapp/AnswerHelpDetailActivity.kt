package com.example.marketgreenapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.example.marketgreenapp.databinding.ActivityAnswerHelpDetailBinding
import com.example.marketgreenapp.model.AnswerHelp

class AnswerHelpDetailActivity : AppCompatActivity() {
    private var answerHelpDetailActivity: ActivityAnswerHelpDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(answerHelpDetailActivity == null)
            answerHelpDetailActivity = ActivityAnswerHelpDetailBinding.inflate(layoutInflater)
        setContentView(answerHelpDetailActivity!!.root)

        getDataIntent()
        answerHelpDetailActivity!!.imgBackCarbonTracker.setOnClickListener {
            this.finish()
        }
    }
    private fun getDataIntent() {
        val bundle = intent.extras ?: return
        val answerHelp: AnswerHelp? =
            bundle.getSerializable("KEY123") as AnswerHelp?
        if (answerHelp != null) {
            answerHelpDetailActivity!!.txtTitle.text = answerHelp.question
            answerHelpDetailActivity!!.txtContent.text = answerHelp.content
            answerHelpDetailActivity!!.imgLink.setImageResource(answerHelp.link!!)
        }
    }
}