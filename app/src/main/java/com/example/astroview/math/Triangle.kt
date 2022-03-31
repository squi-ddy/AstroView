package com.example.astroview.math

import java.lang.IndexOutOfBoundsException

class Triangle(
    val c0: Vec3,
    val c1: Vec3,
    val c2: Vec3
) {
    companion object {
        val NULL = Triangle(
            Vec3(0, 0, 0),
            Vec3(0, 0, 0),
            Vec3(0, 0, 0)
        )
    }

    operator fun get(index: Int): Vec3 {
        return when (index) {
            0 -> c0
            1 -> c1
            2 -> c2
            else -> throw IndexOutOfBoundsException("Triangle has no index $index")
        }
    }
}