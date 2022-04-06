package com.example.astroview.astro

import com.example.astroview.math.Mat4
import kotlin.math.PI
import kotlin.math.sqrt

object Coordinates {
    const val EARTH_A = 6378137.0 // Earth's semi-major axis
    const val EARTH_B = 6356752.3142 // Earth's semi-minor axis
    val EARTH_E = sqrt(1 - EARTH_B * EARTH_B / EARTH_A * EARTH_A) // Earth's eccentricity

    val matJ2kToVsop87 =
        Mat4.rotationX(-23.4392803055555555556 * PI / 180) * Mat4.rotationZ(0.0000275 * PI / 180)
    val matVsop87ToJ2k = matJ2kToVsop87.transpose()

    var matAltAzToJ2k = Mat4.identity()
    var matJ2kToAltAz = Mat4.identity()
    var lat = 0.0
    var long = 0.0

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

    /**
     * Returns a [Mat4] object that converts from Equatorial coordinates to J2k coordinates.
     * @param jde Julian day in TT
     */
    fun getRotEquatorialToJ2k(jde: Double): Mat4 {
        val precession = Precession.getPrecessionAnglesVondrak(jde)
        val rotPrecession = Mat4.rotationZ(-precession.psiA) *
                Mat4.rotationX(-precession.omegaA) *
                Mat4.rotationZ(precession.chiA)
        val nutation = Precession.getNutationAngles(jde)
        val rotNutation = Mat4.rotationX(precession.epsilonA) *
                Mat4.rotationZ(-nutation.deltaPsi) *
                Mat4.rotationX(-precession.epsilonA - nutation.deltaEpsilon)
        return matVsop87ToJ2k * rotPrecession * rotNutation
    }

    /**
     * Call this every frame to update matrices.
     */
    fun updateMatrices() {
        Time.setJDFromSystem()

        val jd = Time.getJD()
        val jde = Time.getJDE()

        val matAltAzToEquatorial = getRotAltAzToEquatorial(jd, jde, 0.0, 0.0)
        val matEquatorialToJ2k = getRotEquatorialToJ2k(jde)
        matAltAzToJ2k = matAltAzToEquatorial * matEquatorialToJ2k
        matJ2kToAltAz = matAltAzToJ2k.transpose()
    }
}