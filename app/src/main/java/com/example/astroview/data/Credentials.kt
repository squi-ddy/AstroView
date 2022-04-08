package com.example.astroview.data

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.astroview.api.API
import com.example.astroview.api.User
import com.example.astroview.api.UserPassword
import com.example.astroview.api.handleReturn
import com.example.pairworkpa.api.CallbackAction
import java.io.IOException

class Credentials {
    private val userInternal = MutableLiveData<FullUser?>(null)
    val user get() = userInternal as LiveData<FullUser?>

    // Convenience methods
    val username get() = userInternal.value?.username
    val auth get() = userInternal.value?.auth
    val password get() = userInternal.value?.password

    fun login(name: String, pass: String, whenDone: CallbackAction<User>) {
        val toEncode = "${name}:${pass}"
        val authHeader =
            "Basic ${Base64.encodeToString(toEncode.encodeToByteArray(), Base64.NO_WRAP)}"
        API.client.getUser(name, authHeader).handleReturn(object : CallbackAction<User> {
            override fun onSuccess(result: User, code: Int) {
                if (loginInternal(name, pass, authHeader))
                    whenDone.onSuccess(result, code)
                else
                    whenDone.onFailure(IOException("Bad response"))
            }

            override fun onBadCode(result: String, code: Int): Boolean {
                logout()
                return whenDone.onBadCode(result, code)
            }

            override fun onFailure(error: Throwable) {
                logout()
                whenDone.onFailure(error)
            }
        })
    }

    fun signUp(name: String, pass: String, whenDone: CallbackAction<User>) {
        val toEncode = "${name}:${pass}"
        val authHeader =
            "Basic ${Base64.encodeToString(toEncode.encodeToByteArray(), Base64.NO_WRAP)}"
        API.client.addUser(name, UserPassword(pass)).handleReturn(object : CallbackAction<User> {
            override fun onSuccess(result: User, code: Int) {
                if (loginInternal(name, pass, authHeader))
                    whenDone.onSuccess(result, code)
                else
                    whenDone.onFailure(IOException("Bad response"))
            }

            override fun onBadCode(result: String, code: Int): Boolean {
                logout()
                return whenDone.onBadCode(result, code)
            }

            override fun onFailure(error: Throwable) {
                logout()
                whenDone.onFailure(error)
            }
        })
    }

    fun delete(whenDone: CallbackAction<Unit>) {
        API.client.deleteUser(username ?: "", auth ?: "")
            .handleReturn(object : CallbackAction<Unit> {
                override fun onSuccess(result: Unit, code: Int) {
                    logout()
                    whenDone.onSuccess(result, code)
                }

                override fun onBadCode(result: String, code: Int): Boolean {
                    return when (code) {
                        401 -> {
                            logout()
                            whenDone.onBadCode(result, code)
                        }
                        else -> whenDone.onBadCode(result, code)
                    }
                }

                override fun onEmptyBody() {
                    onSuccess(Unit, 204)
                }

                override fun onFailure(error: Throwable) {
                    whenDone.onFailure(error)
                }

            })
    }

    fun modify(password: String, add: Int, whenDone: CallbackAction<User>) {
        API.client.modifyUser(username ?: "", UserPassword(password), auth ?: "")
            .handleReturn(object : CallbackAction<User> {
                override fun onSuccess(result: User, code: Int) {
                    if (password.isNotEmpty()) {
                        val toEncode = "${username}:${password}"
                        val authHeader =
                            "Basic ${
                                Base64.encodeToString(
                                    toEncode.encodeToByteArray(),
                                    Base64.NO_WRAP
                                )
                            }"
                        loginInternal(username, password, authHeader)
                    }
                    whenDone.onSuccess(result, code)
                }

                override fun onBadCode(result: String, code: Int): Boolean {
                    return when (code) {
                        401 -> {
                            logout()
                            whenDone.onBadCode(result, code)
                        }
                        else -> whenDone.onBadCode(result, code)
                    }
                }

                override fun onFailure(error: Throwable) {
                    whenDone.onFailure(error)
                }

            })
    }

    private fun loginInternal(
        username: String?,
        password: String?,
        auth: String?
    ): Boolean {
        userInternal.postValue(
            FullUser(
                username ?: return false,
                password ?: return false,
                auth ?: return false
            )
        )
        return true
    }

    fun logout() {
        userInternal.postValue(null)
    }
}