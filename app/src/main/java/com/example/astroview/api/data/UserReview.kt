package com.example.astroview.api.data

import com.google.gson.annotations.SerializedName

data class UserReview(
    @SerializedName("description") val content: String
)
