package com.example.astroview.api.data

import com.google.gson.annotations.SerializedName

data class PageReview(
    @SerializedName("rating_id") val id: Int,
    @SerializedName("page") val page: String,
    @SerializedName("user") val user: String,
    @SerializedName("description") val content: String,
    @SerializedName("time") val time: DateTime
)