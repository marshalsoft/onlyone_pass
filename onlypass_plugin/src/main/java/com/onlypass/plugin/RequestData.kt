package com.onlypass.plugin

data class RequestData(
    val amount: String,
    val externalReference: String,
    val gatewayId: Int,
    val isDemo: Boolean
)