package com.onlypass.plugin

import com.google.gson.annotations.SerializedName

data class APIResponse(
    val status: Boolean,
    var message: String,
    val data:String
)