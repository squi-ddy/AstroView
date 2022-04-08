package com.example.astroview.starmap.math

import com.example.astroview.starmap.core.CoreConstants
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

        if (contains(testPoints[2])) {
            // Totally in cap
            return Intersection.COMPLETELY
        } else if (contains(testPoints[0]) || contains(other.c0 + other.c1 + other.c2)) {
            // Already found an intersection
            return Intersection.PARTIALLY
        }

        MathUtils.genLine(testPoints, CoreConstants.MAX_SPLIT)

        for (point in testPoints) {
            if (contains(point)) {
                return Intersection.PARTIALLY
            }
        }

        return Intersection.NONE
    }
}