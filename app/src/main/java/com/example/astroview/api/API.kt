package com.example.astroview.api

import com.example.pairworkpa.api.AstroViewAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

object API {
    private val caller = Retrofit.Builder()
        .baseUrl("https://astroview.squiddy.me/")
        .addConverterFactory(GsonConverterFactory.create())
        .callbackExecutor(Executors.newSingleThreadExecutor())
        .build()
    val client = caller.create(AstroViewAPI::class.java)
}