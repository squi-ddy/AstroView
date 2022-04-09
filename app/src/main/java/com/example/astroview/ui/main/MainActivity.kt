package com.example.astroview.ui.main

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.astroview.R
import com.example.astroview.api.CallbackAction
import com.example.astroview.api.data.User
import com.example.astroview.data.AppViewModel
import com.example.astroview.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<AppViewModel>()

    val fab get() = if (this::binding.isInitialized) binding.fab else null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        setDefaultNightMode(MODE_NIGHT_YES)

        binding.fab.setOnClickListener {
            if (!viewModel.credentials.loggedIn)
                navController.navigate(R.id.action_starCanvasFragment_to_loginFragment)
            else
                navController.navigate(R.id.action_starCanvasFragment_to_accountInfoFragment)
        }

        setUpAccountObservers()
    }

    private fun setUpAccountObservers() {
        if (viewModel.credentials.user.hasObservers()) return

        val masterKey =
            MasterKey.Builder(this, MasterKey.DEFAULT_MASTER_KEY_ALIAS).apply {
                setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            }.build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            this,
            "account_info",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        if (sharedPreferences.getBoolean("loggedIn", false)) {
            val username = sharedPreferences.getString("username", "")!!
            val password = sharedPreferences.getString("password", "")!!
            viewModel.credentials.login(username, password, object : CallbackAction<User> {
                override fun onSuccess(result: User, code: Int) {
                    runOnUiThread {
                        Snackbar.make(
                            binding.root,
                            "Welcome, ${viewModel.credentials.username}!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onBadCode(result: String, code: Int): Boolean {
                    sharedPreferences.edit().apply {
                        putBoolean("loggedIn", false)
                        apply()
                    }
                    return true
                }

                override fun onFailure(error: Throwable) {
                    return
                }
            })
        }

        viewModel.credentials.user.observe(this) {
            if (it != null) {
                sharedPreferences.edit().apply {
                    putBoolean("loggedIn", true)
                    putString("username", it.username)
                    putString("password", it.password)
                    apply()
                }
            } else {
                sharedPreferences.edit().apply() {
                    putBoolean("loggedIn", false)
                    apply()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}