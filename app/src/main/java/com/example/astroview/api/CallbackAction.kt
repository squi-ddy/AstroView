package com.example.pairworkpa.api

import java.io.IOException

/**
 * A simplified version of Retrofit's Callback.
 *
 * Handles things like network/parsing errors, success calls, and bad code calls.
 *
 * @param T The type to be returned on success.
 * @see [onSuccess]
 * @see [onFailure]
 * @see [onBadCode]
 */
interface CallbackAction<T> {
    /**
     * Called when the request is successful.
     *
     * The result is the fetched object, along with the return code.
     *
     * @param result The fetched object
     * @param code The return code, in [200,300).
     */
    fun onSuccess(result: T, code: Int)

    /**
     * Called when the request is successful, but the code is not in [200, 300).
     *
     * Return true if the situation has been handled. Else, [onFailure] is called.
     * By default, this function always returns false.
     *
     * @param result The result body, as plaintext.
     * @param code The return code.
     */
    fun onBadCode(result: String, code: Int): Boolean {
        return false
    }

    /**
     * Called when there was a network error, or if [onBadCode] returned false.
     *
     * @param error A throwable exception
     */
    fun onFailure(error: Throwable)

    /**
     * Called when the return code is 204, i.e. No Content.
     *
     * By default, this is treated as an error.
     */
    fun onEmptyBody() {
        onFailure(IOException("Bad response"))
    }
}
