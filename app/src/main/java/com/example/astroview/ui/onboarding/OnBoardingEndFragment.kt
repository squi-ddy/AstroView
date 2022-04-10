package com.example.astroview.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.astroview.R
import com.example.astroview.ui.main.MainActivity

class OnBoardingEndFragment : Fragment(R.layout.onboard_four) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val endButton = view.findViewById<Button>(R.id.button)
        endButton.setOnClickListener { v ->
            val prefs = v.context.getSharedPreferences("com.example.astroview", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putBoolean("firstTime", false)
            editor.apply()
            val intent = Intent(v.context, MainActivity::class.java)
            startActivity(intent)
        }
    }
}