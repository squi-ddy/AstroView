package com.example.astroview.starmap.math

import kotlin.math.sqrt

class Vec4 private constructor(vx: Number, vy: Number, vz: Number, vw: Number) {
    val x = vx.toDouble()
    val y = vy.toDouble()
    val z = vz.toDouble()
    val w = vw.toDouble()

    companion object {
        fun fromXYZ(x: Number, y: Number, z: Number, w: Number): Vec4 {
            return Vec4(x, y, z, w)
        }
    }

    val magSquared by lazy {
        x * x + y * y + z * z + w * w
    }

    val mag by lazy {
        sqrt(magSquared)
    }

    operator fun plus(o: Vec4): Vec4 {
        return Vec4(x + o.x, y + o.y, z + o.z, w + o.w)
    }

    operator fun minus(o: Vec4): Vec4 {
        return Vec4(x - o.x, y - o.y, z - o.z, w - o.w)
    }

    operator fun times(s: Number): Vec4 {
        return Vec4(x * s.toDouble(), y * s.toDouble(), z * s.toDouble(), w * s.toDouble())
    }

    operator fun div(s: Number): Vec4 {
        return Vec4(x / s.toDouble(), y / s.toDouble(), z / s.toDouble(), w / s.toDouble())
    }

    operator fun unaryMinus(): Vec4 {
        return this * -1
    }

    fun dot(o: Vec4): Double {
        return x * o.x + y * o.y + z * o.z + w * o.w
    }

    fun norm(): Vec4 {
        return Vec4(x / mag, y / mag, z / mag, w / mag)
    }

    fun toVec3(): Vec3 {
        return Vec3.fromXYZ(x, y, z)
    }

    override fun toString(): String {
        return "($x, $y, $z, $w)"
    }
}