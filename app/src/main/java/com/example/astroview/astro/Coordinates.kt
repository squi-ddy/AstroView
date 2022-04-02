package com.example.astroview.astro

import com.example.astroview.math.Mat4
import kotlin.math.PI
import kotlin.math.sqrt

object Coordinates {
    const val EARTH_A = 6378137.0 // Earth's semi-major axis
    const val EARTH_B = 6356752.3142 // Earth's semi-minor axis
    val EARTH_E = sqrt(1 - EARTH_B * EARTH_B / EARTH_A * EARTH_A) // Earth's eccentricity

    /**
     * Returns a [Mat4] object that converts from Alt-Az at this position to Equatorial coordinates.
     * @param jd Julian day in UTC
     * @param jde Julian day in TT
     * @param lat current latitude
     * @param long current longitude
     */
    fun getRotAltAzToEquatorial(jd: Double, jde: Double, lat: Double, long: Double): Mat4 {
        return Mat4.rotationZ(Time.getApparentSiderealTime(jd, jde) + long * PI / 180) *
                Mat4.rotationY((90 - lat) * PI / 180)
    }
}