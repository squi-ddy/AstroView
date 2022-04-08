package com.example.astroview.starmap.stars.data

import com.example.astroview.starmap.math.Vec3

/**
 * A star with its level and position.
 * @param star The star
 * @param j2k The star's J2k position
 * @param level The star's level on the grid.
 */
data class DetailedStar(val star: Star, val j2k: Vec3, val level: Int)
