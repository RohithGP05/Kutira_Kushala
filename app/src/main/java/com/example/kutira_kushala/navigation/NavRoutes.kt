package com.example.kutira_kushala.navigation

object NavRoutes {
    const val BOOTSTRAP = "bootstrap"
    const val SIGN_IN = "sign_in"
    const val ROLE = "role"
    const val PRODUCER = "producer"
    const val BUYER = "buyer"
    const val PROFILE_EDIT = "profile_edit"
    const val PRODUCT_EDIT = "product_edit/{productId}"

    fun productEdit(productId: String): String =
        "product_edit/${android.net.Uri.encode(productId)}"

    const val BUSINESS_DETAIL = "business_detail/{businessId}"

    fun businessDetail(businessId: String): String =
        "business_detail/${android.net.Uri.encode(businessId)}"
}
