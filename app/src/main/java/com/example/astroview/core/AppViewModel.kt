package com.example.astroview.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.astroview.astro.OrientationData
import com.example.astroview.stars.data.ProjectedStar
import kotlin.collections.ArrayDeque

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val core by lazy {
        CoreInterface.getCore(application.applicationContext)
    }

    val orientation by lazy {
        MutableLiveData(OrientationData(0f, 0f, 0f))
    }

    private val lastOrientations = ArrayDeque<OrientationData>()
    fun addOrientation(data: OrientationData) {
        var currentData = orientation.value!! * lastOrientations.size
        if (lastOrientations.size > CoreConstants.INTERPOLATE_SIZE) {
            currentData -= lastOrientations.removeFirst()
        }
        currentData += data
        lastOrientations.addLast(data)
        orientation.value = currentData / lastOrientations.size
    }

    val projectedStars by lazy {
        MutableLiveData(listOf<ProjectedStar>())
    }
}