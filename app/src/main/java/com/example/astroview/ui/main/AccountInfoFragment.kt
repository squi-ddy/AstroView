package com.example.astroview.ui.main

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
import com.example.astroview.api.CallbackAction
import com.example.astroview.api.data.User
import com.example.astroview.data.AppViewModel
import com.example.astroview.databinding.FragmentAccountInfoBinding
import com.example.astroview.ui.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar

class AccountInfoFragment : Fragment() {
    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<AppViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).fab?.hide()
        setHasOptionsMenu(true)

        val username = viewModel.credentials.username!!
        binding.accountDetails.text = getString(R.string.account_details, username)

        binding.logoutButton.setOnClickListener { v ->
            v.context.hideKeyboard(v)
            viewModel.credentials.logout()
            Snackbar.make(
                v,
                "Goodbye, ${username}!",
                Snackbar.LENGTH_SHORT
            ).show()
            findNavController().navigateUp()
        }

        binding.deleteButton.setOnClickListener { v ->
            v.context.hideKeyboard(v)
            viewModel.credentials.delete(object : CallbackAction<Unit> {
                override fun onSuccess(result: Unit, code: Int) {
                    activity?.runOnUiThread {
                        Snackbar.make(
                            v,
                            "Goodbye, ${username}!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }
                }

                override fun onBadCode(result: String, code: Int): Boolean {
                    activity?.runOnUiThread {
                        Snackbar.make(
                            v,
                            "Unknown error. Try again later.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }
                    return true
                }

                override fun onFailure(error: Throwable) {
                    activity?.runOnUiThread {
                        Snackbar.make(
                            v,
                            "Network error. Try again later.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

            })
        }

        binding.showPasswordAccount.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.showPasswordAccount.transformationMethod = PasswordTransformationMethod()
            } else {
                binding.showPasswordAccount.transformationMethod = null
            }
        }

        binding.passwordEntryAccount.setOnFocusChangeListener { v, focus ->
            if (!focus) v.context.hideKeyboard(v)
        }

        binding.changePasswordButton.setOnClickListener { v ->
            v.context.hideKeyboard(v)
            val enteredPassword = binding.passwordEntryAccount.text.toString()

            if (!enteredPassword.matches("[A-Za-z0-9_$#@%!&]*".toRegex())) {
                Snackbar.make(v, "Password can only be alphanumeric with '_\$#@%!&'.", Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.credentials.modify(enteredPassword, object : CallbackAction<User> {
                    override fun onSuccess(result: User, code: Int) {
                        activity?.runOnUiThread {
                            Snackbar.make(
                                v,
                                "Modification successful!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        }
                    }

                    override fun onBadCode(result: String, code: Int): Boolean {
                        activity?.runOnUiThread {
                            Snackbar.make(
                                v,
                                "Unknown error. Try again later.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        }
                        return true
                    }

                    override fun onFailure(error: Throwable) {
                        activity?.runOnUiThread {
                            Snackbar.make(
                                v,
                                "Network error. Try again later.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }

                })
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