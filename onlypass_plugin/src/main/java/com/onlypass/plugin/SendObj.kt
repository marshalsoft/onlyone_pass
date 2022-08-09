package com.onlypass.plugin

data class SendObj(
    val apikey: String,
    val merchantID: String,
    val amount: String,
    val memo: String,
    val currency: String,
    val email: String,
    val mobile_number:String,
    val gatewayId: Int,
    val gatewayName:String,
    val publicKey:String,
    val refNo:String
)
