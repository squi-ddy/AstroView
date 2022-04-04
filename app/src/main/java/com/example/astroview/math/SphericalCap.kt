package com.example.astroview.math

import com.example.astroview.util.Intersection

/**
 * A spherical cap on the unit sphere.
 * @param centerVector [Vec3] for the centre of the cap.
 * @param cosLimit Dot product limit that defines the cap.
 * @property centerVector *Unit* [Vec3] for the centre of the cap.
 */
class SphericalCap(centerVector: Vec3, val cosLimit: Double) {
    val centerVector = centerVector.norm()

    fun contains(other: Vec3): Boolean {
        return centerVector.dot(other.norm()) >= cosLimit
    }

    fun containsTriangle(other: Triangle): Intersection {
        val c0InCap = contains(other.c0)
        val c1InCap = contains(other.c1)
        val c2InCap = contains(other.c2)
        return if (!c0InCap && !c1InCap && !c2InCap) {
            // Not in cap
            Intersection.NONE
        } else if (c0InCap && c1InCap && c2InCap) {
            // Totally in cap
            Intersection.COMPLETELY
        } else {
            // Partially in cap
            Intersection.PARTIALLY
        }
    }
}