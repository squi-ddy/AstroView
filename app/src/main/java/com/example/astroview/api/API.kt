package com.example.astroview.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

object API {
    private val caller = Retrofit.Builder()
        .baseUrl("https://api.squiddy.me/astroview/")
        .addConverterFactory(GsonConverterFactory.create())
        .callbackExecutor(Executors.newSingleThreadExecutor())
        .build()
    val client: AstroViewAPI = caller.create(AstroViewAPI::class.java)

    fun getDownloadMarkdownLink(page: Int) = "https://api.squiddy.me/astroview/pages/number/${page}/link"
}