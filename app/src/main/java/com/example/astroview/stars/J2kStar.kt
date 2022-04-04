package com.example.astroview.stars

import com.example.astroview.math.Vec3

/**
 * A star with its J2k position.
 * @param star The star
 * @param j2k The star's J2k position
 */
data class J2kStar(val star: Star, val j2k: Vec3)
