package com.example.astroview.api.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("name") val username: String
)