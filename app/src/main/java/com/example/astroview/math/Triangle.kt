package com.example.astroview.math

class Triangle(
    val c0: Vec3,
    val c1: Vec3,
    val c2: Vec3
) {
    companion object {
        val NULL = Triangle(
            Vec3.fromXYZ(0, 0, 0),
            Vec3.fromXYZ(0, 0, 0),
            Vec3.fromXYZ(0, 0, 0)
        )
    }

    operator fun get(index: Int): Vec3 {
        return when (index) {
            0 -> c0
            1 -> c1
            2 -> c2
            else -> throw IndexOutOfBoundsException("Triangle has no index $index")
        }
    }

    fun contains(other: Vec3): Boolean {
        return (c0.cross(c1).dot(other) >= 0 && c1.cross(c2).dot(other) >= 0 && c2.cross(c0).dot(other) >= 0)
    }
}