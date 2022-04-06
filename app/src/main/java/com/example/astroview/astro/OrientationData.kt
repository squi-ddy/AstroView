package com.example.astroview.astro

import com.example.astroview.math.Mat4
import com.example.astroview.math.Vec3
import com.example.astroview.util.ArrayTriple

/**
 * Stores orientation data from the phone.
 * @param azimuth Angle from magnetic north
 * @param pitch Angle between a plane parallel to the screen surface and a plane parallel to the ground.
 * @param roll Angle between a plane perpendicular to the screen surface and a plane perpendicular to the ground.
 */
class OrientationData(val azimuth: Float, val pitch: Float, val roll: Float) {
    companion object {
        // These axes describe default phone orientation.
        // x -> due south
        // y -> ????
        // z -> up from phone
        val AXIS_X = Vec3.fromXYZ(0, -1, 0)
        val AXIS_Y = Vec3.fromXYZ(1, 0, 0)
        val AXIS_Z = Vec3.fromXYZ(0, 0, 1)
        val NORTH = Vec3.fromXYZ(-1, 0, 0)
    }

    /**
     * Get the unit vector representing the orientation of the plane perpendicular to the phone's screen.
     * -X points due North, Z points upwards.
     * @return A rectangular [Vec3] for the phone's orientation.
     */
    fun getVector(): Vec3 {
        return Mat4.rotationZ(-azimuth) * Mat4.rotationY(-pitch) * Mat4.rotationX(-roll) * -AXIS_Z
    }

    /**
     * Get all 3 axes of the phone.
     * @return An [ArrayTriple] of the 3 axes.
     */
    fun getAxes(): ArrayTriple<Vec3> {
        val mat = Mat4.rotationZ(-azimuth) * Mat4.rotationY(-pitch) * Mat4.rotationX(-roll)
        return ArrayTriple(
            mat * AXIS_X,
            mat * AXIS_Y,
            mat * AXIS_Z
        )
    }

    // Convenience for interpolation
    operator fun plus(o: OrientationData): OrientationData {
        return OrientationData(
            azimuth + o.azimuth,
            pitch + o.pitch,
            roll + o.roll
        )
    }

    operator fun minus(o: OrientationData): OrientationData {
        return OrientationData(
            azimuth - o.azimuth,
            pitch - o.pitch,
            roll - o.roll
        )
    }

    operator fun times(s: Number): OrientationData {
        return OrientationData(
            azimuth * s.toFloat(),
            pitch * s.toFloat(),
            roll * s.toFloat()
        )
    }

    operator fun div(s: Number): OrientationData {
        return OrientationData(
            azimuth / s.toFloat(),
            pitch / s.toFloat(),
            roll / s.toFloat()
        )
    }
}