package com.example.astroview.ui

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.astroview.R
import com.example.astroview.api.User
import com.example.astroview.data.AppViewModel
import com.example.astroview.databinding.FragmentLoginBinding
import com.example.astroview.ui.util.hideKeyboard
import com.example.pairworkpa.api.CallbackAction
import com.google.android.material.snackbar.Snackbar


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AppViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun onReturn(v: View) =
        object : CallbackAction<User> {
            override fun onSuccess(result: User, code: Int) {
                activity?.runOnUiThread {
                    Snackbar.make(
                        v,
                        "Welcome, ${viewModel.credentials.username}!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
            }

            override fun onBadCode(result: String, code: Int): Boolean {
                return when (code) {
                    401 -> {
                        activity?.runOnUiThread {
                            Snackbar.make(
                                v,
                                "Wrong username or password",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        true
                    }
                    else -> super.onBadCode(result, code)
                }
            }

            override fun onFailure(error: Throwable) {
                activity?.runOnUiThread {
                    Snackbar.make(
                        v,
                        "Unable to connect with server",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        binding.showPassword.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.passwordEntry.transformationMethod = PasswordTransformationMethod()
            } else {
                binding.passwordEntry.transformationMethod = null
            }
        }

        binding.passwordEntry.setOnFocusChangeListener { v, focus ->
            if (!focus) v.context.hideKeyboard(v)
        }

        binding.usernameEntry.setOnFocusChangeListener { v, focus ->
            if (!focus) v.context.hideKeyboard(v)
        }

        binding.loginButton.setOnClickListener { v ->
            v.context.hideKeyboard(v)
            val enteredUsername = binding.usernameEntry.text.toString()
            val enteredPassword = binding.passwordEntry.text.toString()
            if (!enteredUsername.matches("[A-Za-z0-9_]{1,30}".toRegex())) {
                Snackbar.make(
                    v,
                    "Username must only be alphanumeric with '_', and be between 1 and 30 characters.",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else if (!enteredPassword.matches("[A-Za-z0-9_$#@%!&]+".toRegex())) {
                Snackbar.make(
                    v,
                    "Password can only be alphanumeric with '_\$#@%!&', and cannot be empty.",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                viewModel.credentials.login(enteredUsername, enteredPassword, onReturn(v))
            }
        }

        binding.signUpButton.setOnClickListener { v ->
            v.context.hideKeyboard(v)
            val enteredUsername = binding.usernameEntry.text.toString()
            val enteredPassword = binding.passwordEntry.text.toString()
            if (!enteredUsername.matches("[A-Za-z0-9_]{1,30}".toRegex())) {
                Snackbar.make(
                    v,
                    "Username must only be alphanumeric with '_', and be between 1 and 30 characters.",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else if (!enteredPassword.matches("[A-Za-z0-9_$#@%!&]+".toRegex())) {
                Snackbar.make(
                    v,
                    "Password can only be alphanumeric with '_\$#@%!&', and cannot be empty.",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                viewModel.credentials.signUp(enteredUsername, enteredPassword, onReturn(v))
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val lockGyro = menu.findItem(R.id.menu_gyro_lock)
        lockGyro.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}