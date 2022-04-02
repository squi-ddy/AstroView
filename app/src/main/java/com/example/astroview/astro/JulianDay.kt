package com.example.astroview.astro

/**
 * Stores the current Julian day, along with delta-T information.
 * @param date Julian day double
 * @param deltaT Delta-T (difference between UTC and TT)
 */
data class JulianDay(
    var date: Double,
    var deltaT: Double
)