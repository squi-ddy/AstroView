package com.example.astroview.stars

import com.example.astroview.math.Vec3

abstract class Star {
    abstract val x0: Int
    abstract val x1: Int
    abstract val dx0: Int
    abstract val dx1: Int
    abstract val mag: Int
    abstract val bV: Int

    abstract val j2KPos: Vec3
}