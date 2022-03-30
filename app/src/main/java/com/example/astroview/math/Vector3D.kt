package com.example.astroview.math

import kotlin.math.sqrt

class Vector3D (
    val x: Double,
    val y: Double,
    val z: Double
) {
    val mag by lazy {
        sqrt(x * x + y * y + z * z)
    }

    operator fun plus(o: Vector3D): Vector3D {
        return Vector3D(x + o.x, y + o.y, z + o.z)
    }

    operator fun minus(o: Vector3D): Vector3D {
        return Vector3D(x - o.x, y - o.y, z - o.z)
    }

    operator fun times(s: Double): Vector3D {
        return Vector3D(x * s, y * s, z * s)
    }

    operator fun div(s: Double): Vector3D {
        return Vector3D(x / s, y / s, z / s)
    }

    fun dot(o: Vector3D): Double {
        return x * o.x + y * o.y + z * o.z
    }

    fun cross(o: Vector3D): Vector3D {
        return Vector3D(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    }

    fun norm(): Vector3D {
        return Vector3D(x / mag, y / mag, z / mag)
    }
}