package com.example.astroview.api.data

import com.google.gson.annotations.SerializedName

data class StarReview(
    @SerializedName("rating_id") val id: Int,
    @SerializedName("star") val star: Int,
    @SerializedName("user") val user: String,
    @SerializedName("description") val content: String,
    @SerializedName("time") val time: DateTime
)