package com.example.marketgreenapp.listener

import com.example.marketgreenapp.model.Product

interface IOnClickProductShop {
    fun onClickProduct(product: Product)
    fun onClickBuyProduct(product: Product)
    fun onClickAddToCart(product: Product)
    fun onClickUpdateProduct(product: Product,pos:Int)
    fun onClickDeleteProduct(product: Product,position:Int)
}