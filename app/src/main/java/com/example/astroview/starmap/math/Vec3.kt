package com.example.astroview.starmap.math

import kotlin.math.*

class Vec3 private constructor(vx: Number, vy: Number, vz: Number) {
    val x = vx.toDouble()
    val y = vy.toDouble()
    val z = vz.toDouble()

    companion object {
        fun fromXYZ(x: Number, y: Number, z: Number): Vec3 {
            return Vec3(x, y, z)
        }

        fun fromSpherical(r: Number, theta: Number, phi: Number): Vec3 {
            return Vec3(
                r.toDouble() * cos(phi.toDouble()) * sin(theta.toDouble()),
                r.toDouble() * sin(phi.toDouble()) * cos(theta.toDouble()),
                r.toDouble() * cos(theta.toDouble())
            )
        }
    }

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

    operator fun unaryMinus(): Vec3 {
        return this * -1
    }

    fun dot(o: Vec3): Double {
        return x * o.x + y * o.y + z * o.z
    }

    fun cross(o: Vec3): Vec3 {
        return Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    }

    fun norm(): Vec3 {
        return this / mag
    }

    override fun toString(): String {
        return "($x, $y, $z)"
    }
}