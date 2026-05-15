package com.example.kutira_kushala.data

import com.example.kutira_kushala.data.model.ProductCategory

data class DirectoryFilter(
    val category: ProductCategory? = null,
    val onlyAcceptingOrders: Boolean = false,
)
