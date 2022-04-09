package com.example.astroview.api.data

import com.google.gson.annotations.SerializedName

data class UserPassword (
    @SerializedName("password") val password: String
)