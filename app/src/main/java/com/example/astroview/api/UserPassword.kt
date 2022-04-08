package com.example.astroview.api

import com.google.gson.annotations.SerializedName

data class UserPassword (
    @SerializedName("password") val password: String
)