package com.example.astroview.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.astroview.astro.OrientationData

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val core by lazy {
        AstroViewCore.getCore(application.applicationContext)
    }

    val orientation by lazy {
        MutableLiveData(OrientationData(0f, 0f, 0f))
    }
}