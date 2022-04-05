package com.example.astroview.math

import com.example.astroview.util.Intersection

/**
 * A spherical cap on the unit sphere.
 * @param centerVector [Vec3] for the centre of the cap.
 * @param cosLimit Dot product limit that defines the cap.
 */
class SphericalCap(centerVector: Vec3, private val cosLimit: Double) {
    private val centerVector = centerVector.norm()

    fun contains(other: Vec3): Boolean {
        return centerVector.dot(other.norm()) >= cosLimit
    }

    fun containsTriangle(other: Triangle): Intersection {
        // Project the triangle onto a plane
        val l0 = (other.c1 - other.c0).norm()
        val l1 = (other.c2 - other.c0).norm()
        val xAxis = l0
        val yAxis = (l1 - (xAxis * l1.dot(xAxis))).norm()
        val c0 = Vec2.fromXY(0, 0)
        val c1 = Vec2.fromXY((other.c1 - other.c0).dot(xAxis), 0)
        val c2 = Vec2.fromXY((other.c2 - other.c0).do)

        // Project the centerVector onto this plane.
        // c * cV = x0 * xAxis + y0 * yAxis

        return if (c0InCap && c1InCap && c2InCap) {
            // Totally in cap
            Intersection.COMPLETELY
        } else if (!c0InCap && !c1InCap && !c2InCap && !other.contains(centerVector)) {
            // Not in cap
            Intersection.NONE
        } else {
            // Partially in cap
            Intersection.PARTIALLY
        }
    }
}