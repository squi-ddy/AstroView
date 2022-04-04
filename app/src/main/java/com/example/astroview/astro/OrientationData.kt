package com.example.astroview.astro

import com.example.astroview.math.Mat4
import com.example.astroview.math.Vec3

/**
 * Stores orientation data from the phone.
 * @param azimuth Angle from magnetic north
 * @param pitch Angle between a plane parallel to the screen surface and a plane parallel to the ground.
 * @param roll Angle between a plane perpendicular to the screen surface and a plane perpendicular to the ground.
 */
class OrientationData(val azimuth: Float, val pitch: Float, val roll: Float) {
    companion object {
        val DOWN = Vec3.fromXYZ(0, 0, -1) // Default viewport
    }

    /**
     * Get the unit vector representing the orientation of the plane perpendicular to the phone's screen.
     * Z points due North, Y points upwards.
     * @return A rectangular [Vec3] for the phone's orientation.
     */
    fun getVector(): Vec3 {
        return Mat4.rotationZ(-azimuth) * Mat4.rotationY(-pitch) * Mat4.rotationX(roll) * DOWN
    }
}