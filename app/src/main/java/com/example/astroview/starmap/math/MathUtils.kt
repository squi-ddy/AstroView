package com.example.astroview.starmap.math

object MathUtils {
    private val pow2 = arrayListOf(1)

    private fun initPow2(depth: Int) {
        while (pow2.size < depth + 1) {
            pow2.add(pow2.last() * 2)
        }
    }

    /**
     * Subdivide a line into many points, in-place.
     * @param points The array of points. Initially, should contain the 2 endpoints.
     * @param depth Amount of times to subdivide.
     */
    fun genLine(points: ArrayList<Vec3>, depth: Int) {
        val start = points[0]
        val end = points[1]

        points.clear()

        initPow2(depth)

        for (i in 0 until pow2[depth] - 1) {
            points.add(Vec3.fromXYZ(0, 0, 0))
        }

        for (i in 0 until depth) {
            for (j in 0 until pow2[i]) {
                var vector = Vec3.fromXYZ(0, 0, 0)
                for (k in j..j + 1) {
                    val gcd = gcd(pow2[i], k)
                    val denominator = pow2[i] / gcd
                    val num = k / gcd

                    vector += when (num) {
                        0 -> start
                        denominator -> end
                        else -> points[(num + denominator - 1) / 2 - 1]
                    }
                }
                points[pow2[i] + j - 1] = vector / 2
            }
        }
    }

    fun gcd(a: Int, b: Int): Int {
        if (b == 0) return a
        return gcd(b, a % b)
    }
}