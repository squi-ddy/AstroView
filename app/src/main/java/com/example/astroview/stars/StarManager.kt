package com.example.astroview.stars

import android.content.Context
import com.example.astroview.R
import com.example.astroview.core.AstroViewCore
import com.example.astroview.math.Triangle

class StarManager(private val context: Context) {
    companion object {
        private var initialised = false

        // Only using 4 due to memory reasons
        private val files = arrayOf(
            R.raw.stars_0_0v0_8,
            R.raw.stars_1_0v0_8,
            R.raw.stars_2_0v0_8,
            R.raw.stars_3_1v0_4
        )
    }

    val gridLevels = arrayListOf<ZoneArray>()

    fun init() {
        if (initialised) throw IllegalStateException("StarManager already initialised")

        for (i in files.indices) {
            val za = CatalogueReader(context.resources.openRawResource(files[i])).read()
            if (za.level < gridLevels.size) {
                throw IllegalStateException("Duplicated level")
            } else if (za.level != gridLevels.size) {
                throw IllegalStateException("Invalid level ${za.level}: Expected ${gridLevels.size}")
            }
            gridLevels.add(za)
        }

        AstroViewCore.getGeodesicGrid(gridLevels.size).visitTriangles(gridLevels.size, this)
    }

    fun initTriangle(level: Int, index: Int, t: Triangle) {
        gridLevels[level].initTriangle(index, t);
    }
}