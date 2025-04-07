package com.example.marketgreenapp.admin

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.R
import com.example.marketgreenapp.adapter.VoucherAdminAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityVoucherAdminBinding
import com.example.marketgreenapp.listener.IOnClickItemVoucherAdmin
import com.example.marketgreenapp.model.Voucher
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class VoucherAdminActivity : AppCompatActivity() {
    private var voucherAdminBinding: ActivityVoucherAdminBinding? = null
    private lateinit var voucherAdminAdapter: VoucherAdminAdapter
    private var mListVouchers: MutableList<Voucher>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (voucherAdminBinding == null)
            voucherAdminBinding = ActivityVoucherAdminBinding.inflate(layoutInflater)
        setContentView(voucherAdminBinding!!.root)

        getDataVoucherFirebase()
        voucherAdminBinding?.btnAddVoucher?.setOnClickListener {
            TransitionHelper.navigateWithTransition(
                this@VoucherAdminActivity,
                AddOrUpdateVoucherMainActivity::class.java,
                voucherAdminBinding!!.layoutVoucherAdminRoot,
                "transition_drawer",
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
        }
    }

    private fun getDataVoucherFirebase() {
        try {
            MarketGreenFirebaseApp[this@VoucherAdminActivity].getDataVoucherFromFirebaseDatabaseReference()
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (mListVouchers == null) mListVouchers = mutableListOf()
                        else mListVouchers?.clear()
                        for (snapshotChildren in snapshot.children) {
                            val voucherChildren: Voucher? =
                                snapshotChildren.getValue(Voucher::class.java)
                            voucherChildren?.let {
                                mListVouchers!!.add(0, it)
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
        voucherAdminAdapter = VoucherAdminAdapter(mListVouchers, object : IOnClickItemVoucherAdmin {
            override fun onClickEdit(voucher: Voucher, position: Int) {
                solutionEditVoucher(voucher,position)
            }

            override fun onClickRemove(voucher: Voucher, position: Int) {
                solutionDeleteVoucher(voucher,position)
            }

        })
        voucherAdminBinding?.rcvVoucher?.setHasFixedSize(false)
        voucherAdminBinding?.rcvVoucher?.layoutManager = LinearLayoutManager(this)
        voucherAdminBinding?.rcvVoucher?.adapter = voucherAdminAdapter
    }

    private fun solutionEditVoucher(voucher: Voucher, position: Int) {
        val bundle = Bundle()
        bundle.putSerializable(ConstantKey.KEY_VOUCHER_ADMIN,voucher)
        TransitionHelper.navigateWithTransition(
            this@VoucherAdminActivity,
            AddOrUpdateVoucherMainActivity::class.java,
            voucherAdminBinding!!.layoutVoucherAdminRoot,
            "transition_drawer",
            R.anim.slide_in_right,
            R.anim.slide_no_anim,
            bundle
        )
    }
    private fun solutionDeleteVoucher(voucher: Voucher, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn chắc chắn xóa mục này không?")
            .setPositiveButton("Đồng ý") { _: DialogInterface?, _: Int ->
                MarketGreenFirebaseApp[this@VoucherAdminActivity].getDataVoucherFromFirebaseDatabaseReference()
                    .child(voucher.id.toString()).removeValue { _, _ ->
                        Toast.makeText(this,
                            "Xóa thành công", Toast.LENGTH_SHORT).show()

                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private suspend fun getDataVoucherFirebase2() {
        return withContext(Dispatchers.IO)
        {
            try {
                if (mListVouchers == null) mListVouchers = mutableListOf()
                else mListVouchers?.clear()
                val snapshot =
                    MarketGreenFirebaseApp[this@VoucherAdminActivity].getDataVoucherFromFirebaseDatabaseReference()
                        .get().await()
                for (snapshotChildren in snapshot.children) {
                    val voucherChildren: Voucher? = snapshotChildren.getValue(Voucher::class.java)
                    voucherChildren?.let {
                        mListVouchers!!.add(0, it)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}