package com.example.astroview.api

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("name") val username: String
)