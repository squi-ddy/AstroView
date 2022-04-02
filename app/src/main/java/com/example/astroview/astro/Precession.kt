package com.example.astroview.astro

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object Precession {
    //---- Nutation ----
    private const val NUTATION_BEGIN = 260057.5 // 1 1 -4000
    private const val NUTATION_END = 4642999.5 // 1 1 8000
    private const val NUTATION_TRANSITION = 100.0

    private const val arcSec2Rad = PI * 2.0 / (360.0 * 3600.0)

    // Result cache
    private const val NUTATION_EPOCH_THRESHOLD = 1.0 / 24 // Re-computation threshold (1 hour)
    private var cacheLastJdeNutation = -1e100
    private var cacheDeltaPsi = 0.0
    private var cacheDeltaEpsilon = 0.0

    private data class Nut2000B(
        val lFactor: Double,     // multiplier for lunar mean anomaly l
        val lsFactor: Double,    // multiplier for solar mean anomaly
        val fFactor: Double,     // multiplier for F=L-Omega where L s mean longitude of the moon
        val dFactor: Double,     // mean elongation of the moon from the sun
        val omegaFactor: Double, // mean longitude of lunar ascending node
        val period: Double,      // days
        val a: Double,           // A  [0.1 mas]
        val aPrime: Double,          // A' [0.1 mas]
        val b: Double,           // B  [0.1 mas]
        val bPrime: Double,          // B' [0.1 mas]
        val aPrimePrime: Double,         // A''[0.1 mas]
        val bPrimePrime: Double          // B''[0.1 mas]
    )

    // Nutation table
    private val nut2000BTable = arrayOf(
        Nut2000B(
            0.0,
            0.0,
            0.0,
            0.0,
            1.0,
            -6798.35,
            -172064161.0,
            -174666.0,
            92052331.0,
            9086.0,
            33386.0,
            15377.0
        ),
        Nut2000B(
            0.0,
            0.0,
            2.0,
            -2.0,
            2.0,
            182.62,
            -13170906.0,
            -1675.0,
            5730336.0,
            -3015.0,
            -13696.0,
            -4587.0
        ),
        Nut2000B(
            0.0,
            0.0,
            2.0,
            0.0,
            2.0,
            13.66,
            -2276413.0,
            -234.0,
            978459.0,
            -485.0,
            2796.0,
            1374.0
        ),
        Nut2000B(
            0.0,
            0.0,
            0.0,
            0.0,
            2.0,
            -3399.18,
            2074554.0,
            207.0,
            -897492.0,
            470.0,
            -698.0,
            -291.0
        ),
        Nut2000B(
            0.0,
            1.0,
            0.0,
            0.0,
            0.0,
            365.26,
            1475877.0,
            -3633.0,
            73871.0,
            -184.0,
            11817.0,
            -1924.0
        ),
        Nut2000B(
            0.0,
            1.0,
            2.0,
            -2.0,
            2.0,
            121.75,
            -516821.0,
            1226.0,
            224386.0,
            -677.0,
            -524.0,
            -174.0
        ),
        Nut2000B(
            1.0, 0.0, 0.0, 0.0, 0.0, 27.55, 711159.0, 73.0, -6750.0, 0.0, -872.0, 358.0
        ),
        Nut2000B(0.0, 0.0, 2.0, 0.0, 1.0, 13.63, -387298.0, -367.0, 200728.0, 18.0, 380.0, 318.0),
        Nut2000B(1.0, 0.0, 2.0, 0.0, 2.0, 9.13, -301461.0, -36.0, 129025.0, -63.0, 816.0, 367.0),
        Nut2000B(
            0.0,
            -1.0,
            2.0,
            -2.0,
            2.0,
            365.22,
            215829.0,
            -494.0,
            -95929.0,
            299.0,
            111.0,
            132.0
        ),
        Nut2000B(0.0, 0.0, 2.0, -2.0, 1.0, 177.84, 128227.0, 137.0, -68982.0, -9.0, 181.0, 39.0),
        Nut2000B(-1.0, 0.0, 2.0, 0.0, 2.0, 27.09, 123457.0, 11.0, -53311.0, 32.0, 19.0, -4.0),
        Nut2000B(-1.0, 0.0, 0.0, 2.0, 0.0, 31.81, 156994.0, 10.0, -1235.0, 0.0, -168.0, 82.0),
        Nut2000B(1.0, 0.0, 0.0, 0.0, 1.0, 27.67, 63110.0, 63.0, -33228.0, 0.0, 27.0, -9.0),
        Nut2000B(-1.0, 0.0, 0.0, 0.0, 1.0, -27.44, -57976.0, -63.0, 31429.0, 0.0, -189.0, -75.0),
        Nut2000B(-1.0, 0.0, 2.0, 2.0, 2.0, 9.56, -59641.0, -11.0, 25543.0, -11.0, 149.0, 66.0),
        Nut2000B(1.0, 0.0, 2.0, 0.0, 1.0, 9.12, -51613.0, -42.0, 26366.0, 0.0, 129.0, 78.0),
        Nut2000B(-2.0, 0.0, 2.0, 0.0, 1.0, 1305.48, 45893.0, 50.0, -24236.0, -10.0, 31.0, 20.0),
        Nut2000B(0.0, 0.0, 0.0, 2.0, 0.0, 14.77, 63384.0, 11.0, -1220.0, 0.0, -150.0, 29.0),
        Nut2000B(0.0, 0.0, 2.0, 2.0, 2.0, 7.10, -38571.0, -1.0, 16452.0, -11.0, 158.0, 68.0),
        Nut2000B(-2.0, 0.0, 0.0, 2.0, 0.0, -205.89, -47722.0, 0.0, 477.0, 0.0, -18.0, -25.0),
        Nut2000B(2.0, 0.0, 2.0, 0.0, 2.0, 6.86, -31046.0, -1.0, 13238.0, -11.0, 131.0, 59.0),
        Nut2000B(1.0, 0.0, 2.0, -2.0, 2.0, 23.94, 28593.0, 0.0, -12338.0, 10.0, -1.0, -3.0),
        Nut2000B(-1.0, 0.0, 2.0, 0.0, 1.0, 26.98, 20441.0, 21.0, -10758.0, 0.0, 10.0, -3.0),
        Nut2000B(2.0, 0.0, 0.0, 0.0, 0.0, 13.78, 29243.0, 0.0, -609.0, 0.0, -74.0, 13.0),
        Nut2000B(0.0, 0.0, 2.0, 0.0, 0.0, 13.61, 25887.0, 0.0, -550.0, 0.0, -66.0, 11.0),
        Nut2000B(0.0, 1.0, 0.0, 0.0, 1.0, 386.00, -14053.0, -25.0, 8551.0, -2.0, 79.0, -45.0),
        Nut2000B(-1.0, 0.0, 0.0, 2.0, 1.0, 31.96, 15164.0, 10.0, -8001.0, 0.0, 11.0, -1.0),
        Nut2000B(0.0, 2.0, 2.0, -2.0, 2.0, 91.31, -15794.0, 72.0, 6850.0, -42.0, -16.0, -5.0),
        Nut2000B(0.0, 0.0, -2.0, 2.0, 0.0, -173.31, 21783.0, 0.0, -167.0, 0.0, 13.0, 13.0),
        Nut2000B(1.0, 0.0, 0.0, -2.0, 1.0, -31.66, -12873.0, -10.0, 6953.0, 0.0, -37.0, -14.0),
        Nut2000B(0.0, -1.0, 0.0, 0.0, 1.0, -346.64, -12654.0, 11.0, 6415.0, 0.0, 63.0, 26.0),
        Nut2000B(-1.0, 0.0, 2.0, 2.0, 1.0, 9.54, -10204.0, 0.0, 5222.0, 0.0, 25.0, 15.0),
        Nut2000B(0.0, 2.0, 0.0, 0.0, 0.0, 182.63, 16707.0, -85.0, 168.0, -1.0, -10.0, 10.0),
        Nut2000B(1.0, 0.0, 2.0, 2.0, 2.0, 5.64, -7691.0, 0.0, 3268.0, 0.0, 44.0, 19.0),
        Nut2000B(-2.0, 0.0, 2.0, 0.0, 0.0, 1095.18, -11024.0, 0.0, 104.0, 0.0, -14.0, 2.0),
        Nut2000B(0.0, 1.0, 2.0, 0.0, 2.0, 13.17, 7566.0, -21.0, -3250.0, 0.0, -11.0, -5.0),
        Nut2000B(0.0, 0.0, 2.0, 2.0, 1.0, 7.09, -6637.0, -11.0, 3353.0, 0.0, 25.0, 14.0),
        Nut2000B(0.0, -1.0, 2.0, 0.0, 2.0, 14.19, -7141.0, 21.0, 3070.0, 0.0, 8.0, 4.0),
        Nut2000B(0.0, 0.0, 0.0, 2.0, 1.0, 14.80, -6302.0, -11.0, 3272.0, 0.0, 2.0, 4.0),
        Nut2000B(1.0, 0.0, 2.0, -2.0, 1.0, 23.86, 5800.0, 10.0, -3045.0, 0.0, 2.0, -1.0),
        Nut2000B(2.0, 0.0, 2.0, -2.0, 2.0, 12.81, 6443.0, 0.0, -2768.0, 0.0, -7.0, -4.0),
        Nut2000B(-2.0, 0.0, 0.0, 2.0, 1.0, -199.84, -5774.0, -11.0, 3041.0, 0.0, -15.0, -5.0),
        Nut2000B(2.0, 0.0, 2.0, 0.0, 1.0, 6.85, -5350.0, 0.0, 2695.0, 0.0, 21.0, 12.0),
        Nut2000B(0.0, -1.0, 2.0, -2.0, 1.0, 346.60, -4752.0, -11.0, 2719.0, 0.0, -3.0, -3.0),
        Nut2000B(0.0, 0.0, 0.0, -2.0, 1.0, -14.73, -4940.0, -11.0, 2720.0, 0.0, -21.0, -9.0),
        Nut2000B(-1.0, -1.0, 0.0, 2.0, 0.0, 34.85, 7350.0, 0.0, -51.0, 0.0, -8.0, 4.0),
        Nut2000B(2.0, 0.0, 0.0, -2.0, 1.0, 212.32, 4065.0, 0.0, -2206.0, 0.0, 6.0, 1.0),
        Nut2000B(1.0, 0.0, 0.0, 2.0, 0.0, 9.61, 6579.0, 0.0, -199.0, 0.0, -24.0, 2.0),
        Nut2000B(0.0, 1.0, 2.0, -2.0, 1.0, 119.61, 3579.0, 0.0, -1900.0, 0.0, 5.0, 1.0),
        Nut2000B(1.0, -1.0, 0.0, 0.0, 0.0, 29.80, 4725.0, 0.0, -41.0, 0.0, -6.0, 3.0),
        Nut2000B(-2.0, 0.0, 2.0, 0.0, 2.0, 1615.76, -3075.0, 0.0, 1313.0, 0.0, -2.0, -1.0),
        Nut2000B(3.0, 0.0, 2.0, 0.0, 2.0, 5.49, -2904.0, 0.0, 1233.0, 0.0, 15.0, 7.0),
        Nut2000B(0.0, -1.0, 0.0, 2.0, 0.0, 15.39, 4348.0, 0.0, -81.0, 0.0, -10.0, 2.0),
        Nut2000B(1.0, -1.0, 2.0, 0.0, 2.0, 9.37, -2878.0, 0.0, 1232.0, 0.0, 8.0, 4.0),
        Nut2000B(0.0, 0.0, 0.0, 1.0, 0.0, 29.53, -4230.0, 0.0, -20.0, 0.0, 5.0, -2.0),
        Nut2000B(-1.0, -1.0, 2.0, 2.0, 2.0, 9.81, -2819.0, 0.0, 1207.0, 0.0, 7.0, 3.0),
        Nut2000B(-1.0, 0.0, 2.0, 0.0, 0.0, 26.88, -4056.0, 0.0, 40.0, 0.0, 5.0, -2.0),
        Nut2000B(0.0, -1.0, 2.0, 2.0, 2.0, 7.24, -2647.0, 0.0, 1129.0, 0.0, 11.0, 5.0),
        Nut2000B(-2.0, 0.0, 0.0, 0.0, 1.0, -13.75, -2294.0, 0.0, 1266.0, 0.0, -10.0, -4.0),
        Nut2000B(1.0, 1.0, 2.0, 0.0, 2.0, 8.91, 2481.0, 0.0, -1062.0, 0.0, -7.0, -3.0),
        Nut2000B(2.0, 0.0, 0.0, 0.0, 1.0, 13.81, 2179.0, 0.0, -1129.0, 0.0, -2.0, -2.0),
        Nut2000B(-1.0, 1.0, 0.0, 1.0, 0.0, 3232.87, 3276.0, 0.0, -9.0, 0.0, 1.0, 0.0),
        Nut2000B(1.0, 1.0, 0.0, 0.0, 0.0, 25.62, -3389.0, 0.0, 35.0, 0.0, 5.0, -2.0),
        Nut2000B(1.0, 0.0, 2.0, 0.0, 0.0, 9.11, 3339.0, 0.0, -107.0, 0.0, -13.0, 1.0),
        Nut2000B(-1.0, 0.0, 2.0, -2.0, 1.0, -32.61, -1987.0, 0.0, 1073.0, 0.0, -6.0, -2.0),
        Nut2000B(1.0, 0.0, 0.0, 0.0, 2.0, 27.78, -1981.0, 0.0, 854.0, 0.0, 0.0, 0.0),
        Nut2000B(-1.0, 0.0, 0.0, 1.0, 0.0, -411.78, 4026.0, 0.0, -553.0, 0.0, -353.0, -139.0),
        Nut2000B(0.0, 0.0, 2.0, 1.0, 2.0, 9.34, 1660.0, 0.0, -710.0, 0.0, -5.0, -2.0),
        Nut2000B(-1.0, 0.0, 2.0, 4.0, 2.0, 5.80, -1521.0, 0.0, 647.0, 0.0, 9.0, 4.0),
        Nut2000B(-1.0, 1.0, 0.0, 1.0, 1.0, 6146.17, 1314.0, 0.0, -700.0, 0.0, 0.0, 0.0),
        Nut2000B(0.0, -2.0, 2.0, -2.0, 1.0, 6786.31, -1283.0, 0.0, 672.0, 0.0, 0.0, 0.0),
        Nut2000B(1.0, 0.0, 2.0, 2.0, 1.0, 5.64, -1331.0, 0.0, 663.0, 0.0, 8.0, 4.0),
        Nut2000B(-2.0, 0.0, 2.0, 2.0, 2.0, 14.63, 1383.0, 0.0, -594.0, 0.0, -2.0, -2.0),
        Nut2000B(-1.0, 0.0, 0.0, 0.0, 2.0, -27.33, 1405.0, 0.0, -610.0, 0.0, 4.0, 2.0),
        Nut2000B(1.0, 1.0, 2.0, -2.0, 2.0, 22.47, 1290.0, 0.0, -556.0, 0.0, 0.0, 0.0),
        Nut2000B(-2.0, 0.0, 2.0, 4.0, 2.0, 7.35, -1214.0, 0.0, 518.0, 0.0, 5.0, 2.0),
        Nut2000B(-1.0, 0.0, 4.0, 0.0, 2.0, 9.06, 1146.0, 0.0, -490.0, 0.0, -3.0, -1.0)
    )

    /**
     * Stores nutation values.
     * @param deltaPsi Value for delta-psi (radians)
     * @param deltaEpsilon Value for delta-epsilon (radians)
     */
    data class NutationValues(
        val deltaPsi: Double,
        val deltaEpsilon: Double
    )

    /**
     * Gets parameters for Earth's nutation.
     *
     * @param jde Julian Day in TT
     * @return A [NutationValues] object
     */
    fun getNutationAngles(jde: Double): NutationValues {
        if (jde <= NUTATION_BEGIN - NUTATION_TRANSITION || jde >= NUTATION_END + NUTATION_TRANSITION) {
            return NutationValues(0.0, 0.0) // Unknown; not in table range
        }
        if (abs(jde - cacheLastJdeNutation) > NUTATION_EPOCH_THRESHOLD) {
            // Re-compute values
            cacheLastJdeNutation = jde
            val t = (jde - 2451545.0) / 36525.0
            // F1 : l = mean anomaly of the Moon ['']
            val l = (485868.249036 + 1717915923.2178 * t)
            // F2 : l' = mean anomaly of the Sun ['']
            val ls = (1287104.79305 + 129596581.0481 * t)
            // F3 : F = L - Omega (L is the mean longitude of the Moon)
            val f = (335779.526232 + 1739527262.8478 * t)
            // F4 : D = mean elongation of the Moon from the Sun
            val d = (1072260.70369 + 1602961601.2090 * t)
            // F5 : Omega = mean longitude of the ascending node of the lunar orbit
            val omega = (450160.398036 - 6962890.5431 * t)

            var deltaEpsilon = 0.0
            var deltaPsi = 0.0

            for (nut in nut2000BTable) {
                var theta =
                    nut.lFactor * l + nut.lsFactor * ls + nut.fFactor * f + nut.dFactor * d + nut.omegaFactor * omega
                theta *= arcSec2Rad
                val sinTheta = sin(theta)
                val cosTheta = cos(theta)
                deltaPsi += (nut.a + nut.aPrime * t) * sinTheta + nut.aPrimePrime * cosTheta
                deltaEpsilon += (nut.b + nut.bPrime * t) * cosTheta + nut.bPrimePrime * sinTheta
            }
            deltaPsi *= 1e-7 // convert from units of 0.1uas to arc-seconds. (The paper says mas, but this is an error!)
            deltaEpsilon *= 1e-7
            deltaPsi -= (0.29965 * t + 0.0417750 + 0.0015835)
            deltaEpsilon -= (0.02524 * t + 0.0068192 - 0.0016339)
            cacheDeltaPsi = deltaPsi * arcSec2Rad
            cacheDeltaEpsilon = deltaEpsilon * arcSec2Rad
        }
        var limiter = 1.0
        if (jde < NUTATION_BEGIN) {
            limiter = 1.0 - (NUTATION_BEGIN - jde) / NUTATION_TRANSITION
        }
        if (jde > NUTATION_END) {
            limiter = 1.0 - (jde - NUTATION_END) / NUTATION_TRANSITION
        }
        return NutationValues(cacheDeltaPsi * limiter, cacheDeltaEpsilon * limiter)
    }

    // ---- Precession ----
    // Result cache
    private const val PRECESSION_EPOCH_THRESHOLD = 1.0 / 24 // Re-computation threshold (1 day)
    private var cacheLastJdePrecession = -1e100
    private var cachePsiA = 0.0
    private var cacheEpsilonA = 0.0
    private var cacheOmegaA = 0.0
    private var cacheChiA = 0.0

    // Precession tables

    private val precessionValues = arrayOf(
        arrayOf(
            1.0 / 402.90,
            -22206.325946,
            1267.727824,
            -13765.924050,
            -3243.236469,
            -8571.476251,
            -2206.967126
        ),
        arrayOf(
            1.0 / 256.75,
            12236.649447,
            1702.324248,
            13511.858383,
            -3969.723769,
            5309.796459,
            -4186.752711
        ),
        arrayOf(
            1.0 / 292.00,
            -1589.008343,
            -2970.553839,
            -1455.229106,
            7099.207893,
            -610.393953,
            6737.949677
        ),
        arrayOf(
            1.0 / 537.22,
            2482.103195,
            693.790312,
            1054.394467,
            -1903.696711,
            923.201931,
            -856.922846
        ),
        arrayOf(1.0 / 241.45, 150.322920, -14.724451, 0.0, 146.435014, 3.759055, 0.0),
        arrayOf(
            1.0 / 375.22,
            -13.632066,
            -516.649401,
            -112.300144,
            1300.630106,
            -40.691114,
            957.149088
        ),
        arrayOf(
            1.0 / 157.87,
            389.437420,
            -356.794454,
            202.769908,
            1727.498039,
            80.437484,
            1709.440735
        ),
        arrayOf(
            1.0 / 274.20,
            2031.433792,
            -129.552058,
            1936.050095,
            299.854055,
            807.300668,
            154.425505
        ),
        arrayOf(1.0 / 203.00, 363.748303, 256.129314, 0.0, -1217.125982, 83.712326, 0.0),
        arrayOf(
            1.0 / 440.00,
            -896.747562,
            190.266114,
            -655.484214,
            -471.367487,
            -368.654854,
            -243.520976
        ),
        arrayOf(
            1.0 / 170.72,
            -926.995700,
            95.103991,
            -891.898637,
            -441.682145,
            -191.881064,
            -406.539008
        ),
        arrayOf(1.0 / 713.37, 37.070667, -332.907067, 0.0, -86.169171, -4.263770, 0.0),
        arrayOf(1.0 / 313.00, -597.682468, 131.337633, 0.0, -308.320429, -270.353691, 0.0),
        arrayOf(
            1.0 / 128.38,
            66.282812,
            82.731919,
            -333.322021,
            -422.815629,
            11.602861,
            -446.656435
        ),
        arrayOf(1.0 / 202.00, 0.0, 0.0, 327.517465, 0.0, 0.0, -1049.071786),
        arrayOf(1.0 / 315.00, 0.0, 0.0, -494.780332, 0.0, 0.0, -301.504189),
        arrayOf(1.0 / 136.32, 0.0, 0.0, 585.492621, 0.0, 0.0, 41.348740),
        arrayOf(1.0 / 490.00, 0.0, 0.0, 110.512834, 0.0, 0.0, 142.525186)
    )

    private val precessionEpsilonValues = arrayOf(
        arrayOf(1.0 / 409.90, -6908.287473, 753.872780, -2845.175469, -1704.720302),
        arrayOf(1.0 / 396.15, -3198.706291, -247.805823, 449.844989, -862.308358),
        arrayOf(1.0 / 537.22, 1453.674527, 379.471484, -1255.915323, 447.832178),
        arrayOf(1.0 / 402.90, -857.748557, -53.880558, 886.736783, -889.571909),
        arrayOf(1.0 / 417.15, 1173.231614, -90.109153, 418.887514, 190.402846),
        arrayOf(1.0 / 288.92, -156.981465, -353.600190, 997.912441, -56.564991),
        arrayOf(1.0 / 4043.00, 371.836550, -63.115353, -240.979710, -296.222622),
        arrayOf(1.0 / 306.00, -216.619040, -28.248187, 76.541307, -75.859952),
        arrayOf(1.0 / 277.00, 193.691479, 17.703387, -36.788069, 67.473503),
        arrayOf(1.0 / 203.00, 11.891524, 38.911307, -170.964086, 3.014055)
    )

    /**
     * Stores precession values.
     * @param epsilonA Value for epsilon A (radians)
     * @param chiA Value for chi A (radians)
     * @param omegaA Value for omega A (radians)
     * @param psiA Value for psi A (radians)
     */
    data class PrecessionValues(
        val epsilonA: Double,
        val chiA: Double,
        val omegaA: Double,
        val psiA: Double
    )

    /**
     * Gets parameters for Earth's precession.
     *
     * @param jde Julian Day in TT
     * @return A [PrecessionValues] object
     */
    fun getPrecessionAnglesVondrak(jde: Double): PrecessionValues {
        if (abs(jde - cacheLastJdePrecession) > PRECESSION_EPOCH_THRESHOLD) {
            cacheLastJdePrecession = jde
            val t = (jde - 2451545.0) * (1.0 / 36525.0) // Julian centuries from J2000.0
            if (abs(t) > 2000) {
                throw IllegalArgumentException("JDE is more than 2,000,000 years from epoch")
            }
            val t2Pi = t * (2.0 * PI) // Julian centuries from J2000.0, pre-multiplied by 2 pi

            var psiA = 0.0
            var omegaA = 0.0
            var chiA = 0.0
            var epsilonA = 0.0
            for (row in precessionValues) {
                val invP = row[0]
                val phase = t2Pi * invP
                val sinPhase = sin(phase)
                val cosPhase = cos(phase)
                psiA += row[1] * cosPhase + row[4] * sinPhase
                omegaA += row[2] * cosPhase + row[5] * sinPhase
                chiA += row[3] * cosPhase + row[6] * sinPhase
            }

            for (row in precessionEpsilonValues)
            {
                val invP = row[0]
                val phase = t2Pi * invP
                val sinPhase = sin(phase)
                val cosPhase = cos(phase)
                epsilonA += row[2] * cosPhase + row[4] * sinPhase
            }

            psiA += ((289.0e-9 * t - 0.00740913) * t + 5042.7980307) * t + 8473.343527
            omegaA += ((151.0e-9 * t + 0.00000146) * t - 0.4436568) * t + 84283.175915
            chiA += ((-61.0e-9 * t + 0.00001472) * t + 0.0790159) * t - 19.657270
            epsilonA += ((-110.0e-9 * t - 0.00004039) * t + 0.3624445) * t + 84028.206305

            cachePsiA = arcSec2Rad * psiA
            cacheOmegaA = arcSec2Rad * omegaA
            cacheChiA = arcSec2Rad * chiA
            cacheEpsilonA = arcSec2Rad * epsilonA
        }
        return PrecessionValues(
            cacheEpsilonA,
            cacheChiA,
            cacheOmegaA,
            cachePsiA
        )
    }
}