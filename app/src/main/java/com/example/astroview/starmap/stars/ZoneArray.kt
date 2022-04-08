package com.example.astroview.starmap.stars

import com.example.astroview.starmap.astro.Time
import com.example.astroview.starmap.math.Triangle
import com.example.astroview.starmap.math.Vec3
import com.example.astroview.starmap.stars.data.DetailedStar
import com.example.astroview.starmap.stars.data.Star
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * An array representing a collection of zones from a cat file.
 * @param zones The zones contained.
 * @param level This array's level on the grid.
 * @param magMin Minimum star magnitude, in milli-mag
 * @param magStep The step size for the magnitude, in milli-mag
 */
class ZoneArray(val zones: Array<ZoneData>, val level: Int, val magMin: Int, val magStep: Int) {
    var starPositionScale = 0.0

    companion object {
        val NORTH = Vec3.fromXYZ(0, 0, 1)
        const val D2K = 2451545.0
    }

    fun initTriangle(index: Int, t: Triangle) {
        val z = zones[index]
        z.center = (t.c0 + t.c1 + t.c2).norm()
        z.axis0 = NORTH.cross(z.center).norm()
        z.axis1 = z.center.cross(z.axis0)

        for (i in 0 until 3) {
            val mu0 = (t[i] - z.center).dot(z.axis0)
            val mu1 = (t[i] - z.center).dot(z.axis1)
            val f = 1 / sqrt(1 - mu0 * mu0 - mu1 * mu1)
            var h = abs(mu0) * f
            starPositionScale = max(starPositionScale, h)
            h = abs(mu1) * f
            starPositionScale = max(starPositionScale, h)
        }
    }

    /**
     * Search around an J2k vector for stars.
     * @param index Zone to search
     * @param v J2k vector to search around
     * @param cosLimFov Fov limit: if the dot product is greater than this, the star is returned.
     */
    fun searchAround(index: Int, v: Vec3, cosLimFov: Double, resultSet: MutableList<DetailedStar>) {
        val movementFactor = (PI / 180) * (0.0001 / 3600) * ((Time.getJDE() - D2K) / 365.25) / starPositionScale
        val z = zones[index]
        val vn = v.norm()
        //Log.e("sus", z.stars.size.toString())
        for (star in z.stars) {
            val starVec = star.getJ2kPos(z, movementFactor)
            if (starVec.norm().dot(vn) >= cosLimFov) {
                resultSet.add(DetailedStar(star, starVec, level))
            }
        }
    }

    fun scaleAxis() {
        starPositionScale /= StarUtils.getMaxPosVal(zones[0].stars[0])
        for (z in zones.reversed()) {
            z.axis0 *= starPositionScale
            z.axis1 *= starPositionScale
        }
    }

    /**
     * Returns the V magnitude of a star in this ZoneArray.
     * @param s Star to get magnitude for
     * @return The star's magnitude
     */
    fun getVMagnitude(s: Star): Double {
        return (s.getMag() * magStep + magMin) / 1000.0
    }
}