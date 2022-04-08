package com.example.astroview.starmap.stars

import com.example.astroview.starmap.math.Vec3
import com.example.astroview.starmap.stars.data.Star

class ZoneData(
    val stars: Array<Star>,
    val size: Int
) {
    lateinit var center: Vec3
    lateinit var axis0: Vec3
    lateinit var axis1: Vec3

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ZoneData

        if (!stars.contentEquals(other.stars)) return false
        if (size != other.size) return false
        if (center != other.center) return false
        if (axis0 != other.axis0) return false
        if (axis1 != other.axis1) return false

        return true
    }

    override fun hashCode(): Int {
        return stars.contentHashCode() * size.hashCode() * center.hashCode() * axis0.hashCode() * axis1.hashCode()
    }
}