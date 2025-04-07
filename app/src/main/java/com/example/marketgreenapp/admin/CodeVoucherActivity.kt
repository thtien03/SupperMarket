package com.example.marketgreenapp.admin

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.R
import com.example.marketgreenapp.adapter.CodeVoucherAdapter
import com.example.marketgreenapp.adapter.VoucherAdminAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityCodeVoucherBinding
import com.example.marketgreenapp.listener.IOnClickItemCodeVoucher
import com.example.marketgreenapp.listener.IOnClickItemVoucherAdmin
import com.example.marketgreenapp.model.CodeVoucher
import com.example.marketgreenapp.model.Voucher
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CodeVoucherActivity : AppCompatActivity() {
    private var codeVoucherActivity:ActivityCodeVoucherBinding? = null
    private var mListCodeVouchers:MutableList<CodeVoucher>? = null
    private lateinit var codeVoucherAdapter:CodeVoucherAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(codeVoucherActivity == null)
            codeVoucherActivity = ActivityCodeVoucherBinding.inflate(layoutInflater)
        setContentView(codeVoucherActivity!!.root)

        getDataCodeVoucherFirebase()
        codeVoucherActivity?.btnAddCodeVoucher?.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                this@CodeVoucherActivity,
                AddOrUpdateCodeVoucherActivity::class.java,
                codeVoucherActivity!!.layoutVoucherAdminRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
        }
    }

    private fun getDataCodeVoucherFirebase() {
        try {
            MarketGreenFirebaseApp[this@CodeVoucherActivity].getDataCodeVoucherFromFirebaseDatabaseReference()
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (mListCodeVouchers == null) mListCodeVouchers = mutableListOf()
                        else mListCodeVouchers?.clear()
                        for (snapshotChildren in snapshot.children) {
                            val voucherChildren: CodeVoucher? =
                                snapshotChildren.getValue(CodeVoucher::class.java)
                            voucherChildren?.let {
                                mListCodeVouchers!!.add(0, it)
                            }
                        }
                        setDataVoucherToRecyclerView()
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setDataVoucherToRecyclerView() {
        codeVoucherAdapter = CodeVoucherAdapter(mListCodeVouchers, object : IOnClickItemCodeVoucher {
            override fun onClickEdit(voucher: CodeVoucher, position: Int) {
                solutionEditVoucher(voucher,position)
            }

            override fun onClickRemove(voucher: CodeVoucher, position: Int) {
                solutionDeleteVoucher(voucher,position)
            }

        })
        codeVoucherActivity?.rcvCodeVoucher?.setHasFixedSize(false)
        codeVoucherActivity?.rcvCodeVoucher?.layoutManager = LinearLayoutManager(this)
        codeVoucherActivity?.rcvCodeVoucher?.adapter = codeVoucherAdapter
    }

    private fun solutionDeleteVoucher(voucher: CodeVoucher, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn chắc chắn xóa mục này không?")
            .setPositiveButton("Đồng ý") { _: DialogInterface?, _: Int ->
                MarketGreenFirebaseApp[this@CodeVoucherActivity].getDataCodeVoucherFromFirebaseDatabaseReference()
                    .child(voucher.id.toString()).removeValue { _, _ ->
                        Toast.makeText(this,
                            "Xóa thành công", Toast.LENGTH_SHORT).show()

                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun solutionEditVoucher(voucher: CodeVoucher, position: Int) {
        val bundle = Bundle()
        bundle.putSerializable(ConstantKey.KEY_CODE_VOUCHER_ADMIN,voucher)
        TransitionHelper.navigateWithTransition(
            this@CodeVoucherActivity,
            AddOrUpdateCodeVoucherActivity::class.java,
            codeVoucherActivity!!.layoutVoucherAdminRoot,
            "transition_drawer",
            R.anim.slide_in_right,
            R.anim.slide_no_anim,
            bundle
        )
    }
}