package com.example.astroview.math

import com.example.astroview.util.Intersection

/**
 * A spherical cap on the unit sphere.
 * @param centerVector [Vec3] for the centre of the cap.
 * @param cosLimit Dot product limit that defines the cap.
 */
class SphericalCap(centerVector: Vec3, private val cosLimit: Double) {
    private val centerVector = centerVector.norm()

    fun dot(other: Vec3): Double {
        return centerVector.dot(other.norm())
    }

    fun contains(other: Vec3): Boolean {
        return dot(other) >= cosLimit
    }

    fun containsTriangle(other: Triangle): Intersection {
        val testPoints = arrayListOf(
            other.c0, other.c1, other.c2
        )
        testPoints.sortedByDescending { dot(it) }


        testPoints

        var allInCap = true
        var oneInCap = false

        for (point in testPoints) {
            val pointInCap = contains(point)
            allInCap = allInCap && pointInCap
            oneInCap = oneInCap || pointInCap
        }

        return if (allInCap) {
            // Totally in cap
            Intersection.COMPLETELY
        } else if (!oneInCap && !other.contains(centerVector)) {
            // Not in cap
            Intersection.NONE
        } else {
            // Partially in cap
            Intersection.PARTIALLY
        }
    }
}