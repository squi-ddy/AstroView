package com.example.astroview.math

import kotlin.math.sqrt

object MathUtil {
    /**
     * Returns if the given line segment intersects the circle.
     * @param sV Line segment start
     * @param eV Line segment end
     * @param cV Centre of circle
     * @param r Radius of circle
     * @return Boolean of whether the line intersects the circle.
     */
    fun isLineIntersectingCircle(sV: Vec2, eV: Vec2, cV: Vec2, r: Double): Boolean {
        val dV = eV - sV
        val fV = sV - cV
        val a = dV.magSquared
        val b = 2 * fV.dot(dV)
        val c = fV.magSquared - r * r

        var discriminant = b * b - 4 * a * c
        return if (discriminant < 0) {
            false
        } else {
            discriminant = sqrt(discriminant)
            val t1 = (-b - discriminant) / (2 * a)
            val t2 = (-b + discriminant) / (2 * a)
            (t1 in 0.0..1.0 || t2 in 0.0..1.0)
        }
    }
}