package com.example.astroview.stars

import com.example.astroview.math.Vec2

/**
 * A star projected on a 2D plane.
 * @param position The star's x-y position.
 * @param star The star.
 */
data class ProjectedStar(val position: Vec2, val star: DetailedStar)