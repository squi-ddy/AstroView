package com.example.astroview.api.data

import com.google.gson.annotations.SerializedName

data class HelpPage(
    @SerializedName("category") val category: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("rating_ct") val commentCount: Int
)