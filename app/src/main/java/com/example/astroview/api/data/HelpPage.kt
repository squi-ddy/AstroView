package com.example.astroview.api.data

import com.google.gson.annotations.SerializedName

data class HelpPage(
    @SerializedName("category") val category: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("page_number") val id: Int,
    @SerializedName("name") val name: String
)