package com.example.marketgreenapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketgreenapp.adapter.DeliveryAddressAdapter
import com.example.marketgreenapp.constant.ConstantKey
import com.example.marketgreenapp.constant.TransitionHelper
import com.example.marketgreenapp.databinding.ActivityDeliveryAddressBinding
import com.example.marketgreenapp.model.DeliveryAddress
import com.example.marketgreenapp.references.DataStoreManager
import com.example.marketgreenapp.util.MarketGreenFirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DeliveryAddressActivity : AppCompatActivity() {
    private var deliveryBinding:ActivityDeliveryAddressBinding? = null
    private var mListAddress: MutableList<DeliveryAddress>? = null
    private lateinit var deliveryAddressAdapter: DeliveryAddressAdapter
    private var positionUpdateDelivery = -1
    private var activityResultLauncherDelivery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val action = result.data?.getStringExtra(ConstantKey.KEY_ACTION_DELIVERY)
                val delivery = result.data!!.getSerializableExtra(ConstantKey.KEY_DELIVERY) as DeliveryAddress?
                delivery?.let {
                    when(action)
                    {
                        ConstantKey.VALUE_ADD_DELIVERY -> {
                            solutionAddDelivery(it)
                        }
                        ConstantKey.VALUE_EDIT_DELIVERY -> {
                            solutionUpdateDelivery(it)
                        }
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deliveryBinding = ActivityDeliveryAddressBinding.inflate(layoutInflater)
        setContentView(deliveryBinding!!.root)

        lifecycleScope.launch {
            getListDeliveryAddressFB()
            setListAddressToRecyclerView()
        }
        deliveryBinding?.linearAddDeliveryAddress?.setOnClickListener {
            solutionAddNewAddress()
        }
        deliveryBinding?.imgBackMain?.setOnClickListener {
            this.finish()
        }
    }
    private suspend fun getListDeliveryAddressFB()
    {
        return withContext(Dispatchers.IO)
        {
            try {
                val userCurrent = DataStoreManager.getUser()
                if(userCurrent?.uid != null)
                {
                    val dataSnapshot =
                        MarketGreenFirebaseApp[this@DeliveryAddressActivity].getDataDeliveryUserFromFirebaseDatabaseReference().child(
                            userCurrent.uid!!
                        ).get().await()
                    if (mListAddress != null) mListAddress!!.clear()
                    else mListAddress = mutableListOf()

                    for (dataSnapshotChildren in dataSnapshot.children) {
                        val addressChildren: DeliveryAddress? = dataSnapshotChildren.getValue(DeliveryAddress::class.java)
                        addressChildren?.let {
                            mListAddress!!.add(0, addressChildren)
                        }
                    }
                }
            }
            catch (e:java.lang.Exception)
            {
                e.printStackTrace()
            }
        }

    }
    private fun setListAddressToRecyclerView()
    {
        deliveryAddressAdapter = DeliveryAddressAdapter(mListAddress,object : DeliveryAddressAdapter.IOnClickDelivery{
            override fun onClickDelivery(delivery: DeliveryAddress) {
                val resultIntent = Intent()
                resultIntent.putExtra(ConstantKey.KEY_DELIVERY, delivery)
                setResult(RESULT_OK, resultIntent)
                finish()
            }

            override fun onClickUpdateDelivery(delivery: DeliveryAddress, position: Int) {
                solutionEditDeliveryAddress(delivery,position)
            }
        })
        deliveryBinding?.rcvDeliveryAddress?.setHasFixedSize(false)
        deliveryBinding?.rcvDeliveryAddress?.layoutManager = LinearLayoutManager(this)
        deliveryBinding?.rcvDeliveryAddress?.adapter = deliveryAddressAdapter
    }
    private fun solutionAddNewAddress2()
    {
        TransitionHelper.navigateWithTransition(
            this@DeliveryAddressActivity,
            CreateDeliveryAddressActivity::class.java,
            deliveryBinding!!.createDeliveryRoot,
            "transition_drawer",
            R.anim.slide_in_right,
            R.anim.slide_no_anim
        )
    }
    private fun solutionEditDeliveryAddress(delivery: DeliveryAddress, position: Int)
    {
        positionUpdateDelivery = position
        val intent = Intent(this@DeliveryAddressActivity,CreateDeliveryAddressActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(ConstantKey.KEY_DELIVERY_UPDATE,delivery)
        val pairs:Array<Pair<View, String>> = arrayOf(
            Pair(deliveryBinding?.createDeliveryRoot,"transition_drawer")
        )
        val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@DeliveryAddressActivity,
            *pairs
        )
        option.update(
            ActivityOptionsCompat.makeCustomAnimation(
                this@DeliveryAddressActivity,
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        )
        intent.putExtras(bundle)
        activityResultLauncherDelivery.launch(intent,option)
    }
    private fun solutionAddNewAddress()
    {
        val intent = Intent(this@DeliveryAddressActivity,CreateDeliveryAddressActivity::class.java)
        val pairs:Array<Pair<View, String>> = arrayOf(
            Pair(deliveryBinding?.createDeliveryRoot,"transition_drawer")
        )
        val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@DeliveryAddressActivity,
            *pairs
        )
        option.update(
            ActivityOptionsCompat.makeCustomAnimation(
                this@DeliveryAddressActivity,
                R.anim.slide_in_right,
                R.anim.slide_no_anim
            )
        )
        activityResultLauncherDelivery.launch(intent,option)

    }
    @SuppressLint("NotifyDataSetChanged")
    private fun solutionAddDelivery(deliveryAddress: DeliveryAddress)
    {
        if(mListAddress == null) mListAddress = mutableListOf()
        if (deliveryAddress.isDefault) {
            for (address in mListAddress!!) {
                address.isDefault = false
            }
        }
        mListAddress!!.add(0,deliveryAddress)
        deliveryAddressAdapter.notifyDataSetChanged()
        //deliveryAddressAdapter.notifyItemInserted(0)
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun solutionUpdateDelivery(deliveryAddress: DeliveryAddress)
    {
        if(positionUpdateDelivery != -1)
        {
            if(mListAddress != null && mListAddress!!.size > 0)
            {
                if (deliveryAddress.isDefault) {
                    for (address in mListAddress!!) {
                        address.isDefault = false
                    }
                }
                mListAddress!![positionUpdateDelivery] = deliveryAddress
                //deliveryAddressAdapter.notifyItemChanged(positionUpdateDelivery)
                deliveryAddressAdapter.notifyDataSetChanged()
            }
        }
    }
}