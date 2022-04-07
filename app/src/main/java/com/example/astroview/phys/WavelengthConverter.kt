package com.example.astroview.phys

import com.example.astroview.util.ArrayTriple
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.pow

object WavelengthConverter {
    // Algorithm translated to Kotlin from
    // https://stackoverflow.com/a/39446403

    private const val LEN_MIN = 380
    private const val LEN_MAX = 780
    private const val LEN_STEP = 5

    private val X = arrayOf(
        0.000160,
        0.000662,
        0.002362,
        0.007242,
        0.019110,
        0.043400,
        0.084736,
        0.140638,
        0.204492,
        0.264737,
        0.314679,
        0.357719,
        0.383734,
        0.386726,
        0.370702,
        0.342957,
        0.302273,
        0.254085,
        0.195618,
        0.132349,
        0.080507,
        0.041072,
        0.016172,
        0.005132,
        0.003816,
        0.015444,
        0.037465,
        0.071358,
        0.117749,
        0.172953,
        0.236491,
        0.304213,
        0.376772,
        0.451584,
        0.529826,
        0.616053,
        0.705224,
        0.793832,
        0.878655,
        0.951162,
        1.014160,
        1.074300,
        1.118520,
        1.134300,
        1.123990,
        1.089100,
        1.030480,
        0.950740,
        0.856297,
        0.754930,
        0.647467,
        0.535110,
        0.431567,
        0.343690,
        0.268329,
        0.204300,
        0.152568,
        0.112210,
        0.081261,
        0.057930,
        0.040851,
        0.028623,
        0.019941,
        0.013842,
        0.009577,
        0.006605,
        0.004553,
        0.003145,
        0.002175,
        0.001506,
        0.001045,
        0.000727,
        0.000508,
        0.000356,
        0.000251,
        0.000178,
        0.000126,
        0.000090,
        0.000065,
        0.000046,
        0.000033
    )

    private val Y = arrayOf(
        0.000017,
        0.000072,
        0.000253,
        0.000769,
        0.002004,
        0.004509,
        0.008756,
        0.014456,
        0.021391,
        0.029497,
        0.038676,
        0.049602,
        0.062077,
        0.074704,
        0.089456,
        0.106256,
        0.128201,
        0.152761,
        0.185190,
        0.219940,
        0.253589,
        0.297665,
        0.339133,
        0.395379,
        0.460777,
        0.531360,
        0.606741,
        0.685660,
        0.761757,
        0.823330,
        0.875211,
        0.923810,
        0.961988,
        0.982200,
        0.991761,
        0.999110,
        0.997340,
        0.982380,
        0.955552,
        0.915175,
        0.868934,
        0.825623,
        0.777405,
        0.720353,
        0.658341,
        0.593878,
        0.527963,
        0.461834,
        0.398057,
        0.339554,
        0.283493,
        0.228254,
        0.179828,
        0.140211,
        0.107633,
        0.081187,
        0.060281,
        0.044096,
        0.031800,
        0.022602,
        0.015905,
        0.011130,
        0.007749,
        0.005375,
        0.003718,
        0.002565,
        0.001768,
        0.001222,
        0.000846,
        0.000586,
        0.000407,
        0.000284,
        0.000199,
        0.000140,
        0.000098,
        0.000070,
        0.000050,
        0.000036,
        0.000025,
        0.000018,
        0.000013
    )

    private val Z = arrayOf(
        0.000705,
        0.002928,
        0.010482,
        0.032344,
        0.086011,
        0.197120,
        0.389366,
        0.656760,
        0.972542,
        1.282500,
        1.553480,
        1.798500,
        1.967280,
        2.027300,
        1.994800,
        1.900700,
        1.745370,
        1.554900,
        1.317560,
        1.030200,
        0.772125,
        0.570060,
        0.415254,
        0.302356,
        0.218502,
        0.159249,
        0.112044,
        0.082248,
        0.060709,
        0.043050,
        0.030451,
        0.020584,
        0.013676,
        0.007918,
        0.003988,
        0.001091,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000,
        0.000000
    )

    private val MAT_XYZ_TO_RGB = arrayOf(
        3.2404542, -1.5371385, -0.4985314,
        -0.9692660, 1.8760108, 0.0415560,
        0.0556434, -0.2040259, 1.0572252
    )

    private val H = 6.62607015e-34
    private val C = 299792458.0
    private val K_B = 1.380649e-23

    fun calculateRGBFromTemperature(temp: Double): ArrayTriple<Double> {
        val wLenBase = 350.0
        val wLenStep = 4.5
        var rgbSum = ArrayTriple(0.0, 0.0, 0.0)
        var totalWt = 0.0
        for (i in 0..100) {
            val wLen = wLenBase + wLenStep * i
            val wt = getPlanckSpectrum(wLen, temp)
            val rgb = calculateRGBFromWavelength(wLen)
            totalWt += wt
            rgbSum = ArrayTriple(
                rgbSum[0] + wt * rgb[0],
                rgbSum[1] + wt * rgb[1],
                rgbSum[2] + wt * rgb[2]
            )
        }
        return ArrayTriple(
            255 * clip(gammaCorrectSRGB(rgbSum[0] / totalWt)),
            255 * clip(gammaCorrectSRGB(rgbSum[1] / totalWt)),
            255 * clip(gammaCorrectSRGB(rgbSum[2] / totalWt))
        )
    }

    fun getPlanckSpectrum(wavelength: Double, temp: Double): Double {
        val wLen = wavelength * 1e-9
        return 2 * H * C * C / (wLen.pow(5) * (exp(H * C / wLen / K_B / temp) - 1))
    }

    fun calculateRGBFromWavelength(wavelength: Double): ArrayTriple<Double> {
        if (wavelength < LEN_MIN || wavelength > LEN_MAX)
            return ArrayTriple(0.0, 0.0, 0.0)

        var wLen = wavelength
        wLen -= LEN_MIN
        val index = floor(wLen / LEN_STEP).toInt()
        val offset = wLen - LEN_STEP * index

        val x = interpolate(X, index, offset)
        val y = interpolate(Y, index, offset)
        val z = interpolate(Z, index, offset)

        val m = MAT_XYZ_TO_RGB

        var r = m[0] * x + m[1] * y + m[2] * z
        var g = m[3] * x + m[4] * y + m[5] * z
        var b = m[6] * x + m[7] * y + m[8] * z

        r = clip(r)
        g = clip(g)
        b = clip(b)

        return ArrayTriple(r, g, b)
    }

    private fun interpolate(values: Array<Double>, index: Int, offset: Double): Double {
        if (offset == 0.0)
            return values[index]

        val x0 = index * LEN_STEP
        val x1 = x0 + LEN_STEP
        val y0 = values[index]
        val y1 = values[1 + index]

        return y0 + offset * (y1 - y0) / (x1 - x0)
    }

    private fun gammaCorrectSRGB(c: Double): Double {
        if (c <= 0.0031308)
            return 12.92 * c

        val a = 0.055
        return (1 + a) * c.pow(1 / 2.4) - a
    }

    private fun clip(c: Double): Double {
        if (c < 0)
            return 0.0
        if (c > 1)
            return 1.0
        return c
    }
}