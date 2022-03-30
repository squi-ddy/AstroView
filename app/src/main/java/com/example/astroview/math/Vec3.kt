package com.example.astroview.math

import kotlin.math.sqrt

class Vec3(vx: Number, vy: Number, vz: Number) {
    val x = vx.toDouble()
    val y = vy.toDouble()
    val z = vz.toDouble()

    val magSquared by lazy {
        x * x + y * y + z * z
    }

    val mag by lazy {
        sqrt(magSquared)
    }

    operator fun plus(o: Vec3): Vec3 {
        return Vec3(x + o.x, y + o.y, z + o.z)
    }

    operator fun minus(o: Vec3): Vec3 {
        return Vec3(x - o.x, y - o.y, z - o.z)
    }

    operator fun times(s: Number): Vec3 {
        return Vec3(x * s.toDouble(), y * s.toDouble(), z * s.toDouble())
    }

    operator fun div(s: Number): Vec3 {
        return Vec3(x / s.toDouble(), y / s.toDouble(), z / s.toDouble())
    }

    fun dot(o: Vec3): Double {
        return x * o.x + y * o.y + z * o.z
    }

    fun cross(o: Vec3): Vec3 {
        return Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    }

    fun norm(): Vec3 {
        return Vec3(x / mag, y / mag, z / mag)
    }
}