package com.example.astroview.math

class Triangle(
    val c0: Vec3,
    val c1: Vec3,
    val c2: Vec3
) {
    companion object {
        val NULL = Triangle(
            Vec3(0, 0, 0),
            Vec3(0, 0, 0),
            Vec3(0, 0, 0)
        )
    }
}