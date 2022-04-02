package com.example.astroview.math

import kotlin.math.cos
import kotlin.math.sin

class Mat4(
    m00: Number, m01: Number, m02: Number, m03: Number,
    m10: Number, m11: Number, m12: Number, m13: Number,
    m20: Number, m21: Number, m22: Number, m23: Number,
    m30: Number, m31: Number, m32: Number, m33: Number
) {
    val r: Array<Double> = arrayOf(
        m00.toDouble(), m01.toDouble(), m02.toDouble(), m03.toDouble(),
        m10.toDouble(), m11.toDouble(), m12.toDouble(), m13.toDouble(),
        m20.toDouble(), m21.toDouble(), m22.toDouble(), m23.toDouble(),
        m30.toDouble(), m31.toDouble(), m32.toDouble(), m33.toDouble()
    )

    constructor(v0: Vec4, v1: Vec4, v2: Vec4, v3: Vec4) : this(
        v0.x, v0.y, v0.z, v0.w,
        v1.x, v1.y, v1.z, v1.w,
        v2.x, v2.y, v2.z, v2.w,
        v3.x, v3.y, v3.z, v3.w
    )

    companion object {
        fun rotationX(angle: Number): Mat4 {
            val c = cos(angle.toDouble())
            val s = sin(angle.toDouble())

            return Mat4(
                1, 0, 0, 0,
                0, c, s, 0,
                0, -s, c, 0,
                0, 0, 0, 1
            )
        }

        fun rotationY(angle: Number): Mat4 {
            val c = cos(angle.toDouble())
            val s = sin(angle.toDouble())

            return Mat4(
                c, 0, -s, 0,
                0, 1, 0, 0,
                s, 0, c, 0,
                0, 0, 0, 1
            )
        }

        fun rotationZ(angle: Number): Mat4 {
            val c = cos(angle.toDouble())
            val s = sin(angle.toDouble())

            return Mat4(
                c, s, 0, 0,
                -s, c, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
            )
        }
    }

    private fun matMul(row: Int, column: Int, o: Mat4): Double {
        return r[column] * o.r[4 * row] + r[column + 4] * o.r[4 * row + 1] + r[column + 8] * o.r[4 * row + 2] + r[column + 12] * o.r[4 * row + 3]
    }

    operator fun times(v: Vec4): Vec4 {
        return Vec4.fromXYZ(
            r[0] * v.x + r[4] * v.y + r[8] * v.z + r[12] * v.w,
            r[1] * v.x + r[5] * v.y + r[9] * v.z + r[13] * v.w,
            r[2] * v.x + r[6] * v.y + r[10] * v.z + r[14] * v.w,
            1
        )
    }

    operator fun times(m: Mat4): Mat4 {
        return Mat4(
            matMul(0, 0, m), matMul(0, 1, m), matMul(0, 2, m), matMul(0, 3, m),
            matMul(1, 0, m), matMul(1, 1, m), matMul(1, 2, m), matMul(1, 3, m),
            matMul(2, 0, m), matMul(2, 1, m), matMul(2, 2, m), matMul(2, 3, m),
            matMul(3, 0, m), matMul(3, 1, m), matMul(3, 2, m), matMul(3, 3, m)
        )
    }

    fun transpose(): Mat4 {
        return Mat4(
            r[0], r[4], r[8], r[12],
            r[1], r[5], r[9], r[13],
            r[2], r[6], r[10], r[14],
            r[3], r[7], r[11], r[15]
        )
    }
}