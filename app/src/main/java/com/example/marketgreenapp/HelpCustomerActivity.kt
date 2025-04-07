package com.example.marketgreenapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.SearchView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketgreenapp.adapter.AnswerHelpAdapter
import com.example.marketgreenapp.constant.FunctionGlobal
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityHelpCustomerBinding
import com.example.marketgreenapp.model.AnswerHelp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HelpCustomerActivity : AppCompatActivity() {
    private var helpBinding:ActivityHelpCustomerBinding? = null
    private val delayMillis: Long = 20
    private var targetApp:String = ""
    private var listAnswerHelp:MutableList<AnswerHelp>? = null
    private var mListAnswerHelp:MutableList<AnswerHelp>? = null
    private lateinit var answerAdapter: AnswerHelpAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(helpBinding == null)
            helpBinding = ActivityHelpCustomerBinding.inflate(layoutInflater)
        setContentView(helpBinding!!.root)
        targetApp = resources.getString(R.string.target_app)
        lifecycleScope.launch {
            showTextAnimation(helpBinding?.txtMarketGreenApp,targetApp)
        }

        helpBinding!!.imgBackMain.setOnClickListener {
            this.finish()
        }

        setRecyclerview()
        solutionSearch()

    }
    private suspend fun showTextAnimation(textView: TextView?, text: String) {
        textView?.text = ""
        for (i in text.indices) {
            textView?.text = text.substring(0, i + 1)
            delay(delayMillis)
        }
    }
    private fun setRecyclerview()
    {
        mListAnswerHelp = FunctionGlobal.getDataAnswerHelp(this@HelpCustomerActivity)

        helpBinding!!.rcvExpandedInformation.setHasFixedSize(false)
        helpBinding!!.rcvExpandedInformation.layoutManager = LinearLayoutManager(this)
        val itemDecoration = DividerItemDecoration(this@HelpCustomerActivity, RecyclerView.VERTICAL)
        helpBinding!!.rcvExpandedInformation.addItemDecoration(itemDecoration)
        answerAdapter = AnswerHelpAdapter(mListAnswerHelp,object : AnswerHelpAdapter.IOnClickItemAnswer{
            override fun selectAnswer(answerHelp: AnswerHelp) {
                val bundle = Bundle()
                bundle.putSerializable("KEY123",answerHelp)
                TransitionHelper.navigateWithTransition(
                    this@HelpCustomerActivity,
                    AnswerHelpDetailActivity::class.java,
                    helpBinding!!.layoutRoot,
                    "transition_drawer",
                    R.anim.slide_in_right,
                    R.anim.slide_no_anim,
                    bundle
                )
            }
        })
        helpBinding!!.rcvExpandedInformation.adapter = answerAdapter
    }
    private fun solutionSearch()
    {
        helpBinding!!.searchInformation.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText)
                return true
            }

        })
    }
    private fun filterData(query:String?) {
        listAnswerHelp = mutableListOf()
        if (TextUtils.isEmpty(query)) {
            listAnswerHelp = mListAnswerHelp
            answerAdapter.setDataFilter(mListAnswerHelp)
        } else {
            listAnswerHelp = mutableListOf()
            val listFilter = mutableListOf<AnswerHelp>()
            for (item in mListAnswerHelp!!) {
                if (item.question.toString().lowercase().trim()
                        .contains(query.toString().lowercase().trim())
                )
                    listFilter.add(item)
            }
            listAnswerHelp = listFilter
            if (listAnswerHelp?.isEmpty() == false) {
                answerAdapter.setDataFilter(listAnswerHelp)
            }
        }
    }
}