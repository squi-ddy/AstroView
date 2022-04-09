package com.example.astroview.api

import com.example.astroview.api.data.*
import retrofit2.Call
import retrofit2.http.*

interface AstroViewAPI {
    @GET("users/{name}/")
    fun getUser(@Path("name") username: String, @Header("Authorization") auth: String): Call<User>

    @POST("users/{name}/")
    fun addUser(@Path("name") username: String, @Body body: UserPassword): Call<User>

    @PATCH("users/{name}/")
    fun modifyUser(
        @Path("name") username: String,
        @Body body: UserPassword,
        @Header("Authorization") auth: String
    ): Call<User>

    @DELETE("users/{name}/")
    fun deleteUser(
        @Path("name") username: String,
        @Header("Authorization") auth: String
    ): Call<Unit>

    @GET("stars/{id}/ratings/")
    fun getStarReviews(@Path("id") star: Int): Call<List<StarReview>>

    @POST("stars/{id}/ratings/")
    fun postStarReview(
        @Path("id") star: Int,
        @Header("Authorization") auth: String,
        @Body body: UserReview
    ): Call<StarReview>

    @GET("pages/{id}/ratings/")
    fun getPageReviews(@Path("id") page: String): Call<List<PageReview>>

    @POST("pages/{id}/ratings/")
    fun postPageReview(
        @Path("id") page: String,
        @Header("Authorization") auth: String,
        @Body body: UserReview
    ): Call<PageReview>
}
