package com.example.astroview.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.astroview.astro.OrientationData
import com.example.astroview.stars.ProjectedStar

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val core by lazy {
        CoreInterface.getCore(application.applicationContext)
    }

    val orientation by lazy {
        MutableLiveData(OrientationData(0f, 0f, 0f))
    }

    val projectedStars by lazy {
        MutableLiveData(listOf<ProjectedStar>())
    }
}