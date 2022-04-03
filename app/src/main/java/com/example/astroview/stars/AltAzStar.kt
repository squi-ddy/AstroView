package com.example.astroview.stars

import com.example.astroview.math.Vec3

/**
 * A star with its Alt-Az position.
 * @param star The star
 * @param altAz The star's Alt-Az position
 */
data class AltAzStar(val star: Star, val altAz: Vec3)
