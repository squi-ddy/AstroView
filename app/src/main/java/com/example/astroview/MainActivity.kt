package com.example.astroview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.astroview.stars.CatalogueReader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CatalogueReader(resources.openRawResource(R.raw.stars_0_0v0_8)).read()
    }
}