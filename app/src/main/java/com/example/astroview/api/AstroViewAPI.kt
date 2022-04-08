package com.example.pairworkpa.api

import com.example.astroview.api.User
import com.example.astroview.api.UserPassword
import retrofit2.Call
import retrofit2.http.*

interface AstroViewAPI {
    @GET("users/{name}/")
    fun getUser(@Path("name") username: String, @Header("Authorization") auth: String): Call<User>

    @POST("users/{name}/")
    fun addUser(@Path("name") username: String, @Body body: UserPassword): Call<User>

    @PATCH("users/{name}/")
    fun modifyUser(@Path("name") username: String, @Body body: UserPassword, @Header("Authorization") auth: String): Call<User>

    @DELETE("users/{name}/")
    fun deleteUser(@Path("name") username: String, @Header("Authorization") auth: String): Call<Unit>

    /*@GET("products/{id}/ratings/")
    fun getReviews(@Path("id") productId: String): Call<ProductReviews>

    @POST("products/{id}/ratings/")
    fun postReview(@Path("id") productId: String, @Header("Authorization") auth: String, @Body body: CreatedReview): Call<Review>*/
}
