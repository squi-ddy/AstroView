package com.example.astroview.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * Simplifies Retrofit's callback using [CallbackAction].
 *
 * @param action The [CallbackAction] that will handle the response.
 */
fun <T> Call<T>.handleReturn(action: CallbackAction<T>) {
    this.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            val result = response.body()
            if (response.isSuccessful) {
                if (response.code() == 204) {
                    return action.onEmptyBody()
                } else if (result == null) {
                    return action.onFailure(IOException("Bad response"))
                }
                return action.onSuccess(result, response.code())
            }
            if (!action.onBadCode(response.message(), response.code())) return action.onFailure(
                IOException("Bad code")
            )
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            action.onFailure(t)
        }
    })
}