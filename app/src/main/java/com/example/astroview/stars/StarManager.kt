package com.example.astroview.stars

import android.content.Context
import com.example.astroview.R
import com.example.astroview.core.AstroViewCore
import com.example.astroview.math.Triangle
import com.example.astroview.math.Vec3

class StarManager(private val context: Context) {
    companion object {
        private var initialised = false

        // Only using 3 due to memory reasons
        private val files = arrayOf(
            R.raw.stars_0_0v0_8,
            R.raw.stars_1_0v0_8,
            R.raw.stars_2_0v0_8
        )
    }

    val gridLevels = arrayListOf<ZoneArray>()

    fun init(core: AstroViewCore) {
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

        core.getGeodesicGrid(gridLevels.size).visitTriangles(gridLevels.size, this)

        for (z in gridLevels) {
            z.scaleAxis()
        }

        core.starManager = this
    }

    fun initTriangle(level: Int, index: Int, t: Triangle) {
        gridLevels[level].initTriangle(index, t)
    }

    fun searchAround(level: Int, index: Int, v: Vec3, cosLimFov: Double): Set<AltAzStar> {
        return gridLevels[level].searchAround(index, v, cosLimFov)
    }
}