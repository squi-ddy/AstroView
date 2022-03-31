package com.example.astroview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.astroview.stars.StarManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StarManager(this).init()
    }
}