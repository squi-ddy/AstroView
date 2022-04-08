package com.example.astroview.starmap.physics

object TemperatureConverter {
    /**
     * Converts a given B-V value to temperature.
     * Color can then be determined by using blackbody emissions equations.
     *
     * Uses Ballesteros' formula - arXiv:1201.1809
     *
     * @param bV The B-V value.
     * @return Temperature in Kelvin.
     */
    fun bVToTemperature(bV: Double): Double {
        return 4600.0 * (1 / (0.92 * bV + 1.7) + 1 / (0.92 * bV + 0.62))
    }
}