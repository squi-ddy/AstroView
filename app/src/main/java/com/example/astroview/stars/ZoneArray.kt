package com.example.astroview.stars

import com.example.astroview.astro.Time
import com.example.astroview.math.Triangle
import com.example.astroview.math.Vec3
import com.example.astroview.util.StarUtils
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

class ZoneArray(val zones: Array<ZoneData>, val level: Int) {
    var starPositionScale = 0.0

    companion object {
        val north = Vec3.fromXYZ(0, 0, 1)
        val d2k = 2451545
    }

    fun initTriangle(index: Int, t: Triangle) {
        val z = zones[index]
        z.center = (t.c0 + t.c1 + t.c2).norm()
        z.axis0 = north.cross(z.center).norm()
        z.axis1 = z.center.cross(z.axis0).norm()

        for (i in 0 until 3) {
            val mu0 = (t[i] - z.center).dot(z.axis0)
            val mu1 = (t[i] - z.center).dot(z.axis1)
            val f = 1 / sqrt(1 - mu0 * mu0 - mu1 * mu1)
            var h = abs(mu0) * f
            if (starPositionScale < h) starPositionScale = h
            h = abs(mu1) * f
            if (starPositionScale < h) starPositionScale = h
        }
    }

    fun searchAround(index: Int, v: Vec3, cosLimFov: Double): ArrayList<Star> {
        val movementFactor = (PI / 180) * (0.0001 / 3600) * ((Time.getJDE() - d2k) / 365.25) / starPositionScale
        val z = zones[index]
        val result = arrayListOf<Star>()
        for (star in z.stars) {
            val starVec = star.getJ2kPos(z, movementFactor).norm()
            if (starVec.dot(v) >= cosLimFov) {
                result.add(star)
            }
        }
        return result
    }

    fun scaleAxis() {
        starPositionScale /= StarUtils.getMaxPosVal(zones[0].stars[0])
        for (z in zones.reversed()) {
            z.axis0 *= starPositionScale
            z.axis1 *= starPositionScale
        }
    }
}