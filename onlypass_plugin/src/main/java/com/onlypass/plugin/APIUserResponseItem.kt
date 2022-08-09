package com.onlypass.plugin

data class APIUserResponseItem(
    val createdAt: String,
    val gateway: Gateway,
    val gatewayId: Int,
    val merchantId: Int,
    val merchantPaymentGatewayId: String,
    val publicKey: String,
    val status: String,
    val updatedAt: String,
    var ref: String = "",

)