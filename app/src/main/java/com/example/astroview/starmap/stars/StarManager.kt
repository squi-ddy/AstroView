package com.example.astroview.starmap.stars

import android.content.Context
import com.example.astroview.R
import com.example.astroview.starmap.core.CoreInterface
import com.example.astroview.starmap.math.Triangle
import com.example.astroview.starmap.math.Vec3
import com.example.astroview.starmap.stars.data.DetailedStar
import com.example.astroview.starmap.stars.data.Star
import java.util.*

class StarManager(private val context: Context) {
    companion object {
        private var initialised = false

        // Only using 1 due to memory reasons
        private val files = arrayOf(
            R.raw.stars_0_0v0_8,
            // R.raw.stars_1_0v0_8,
            // R.raw.stars_2_0v0_8
        )
    }

    private val names = mutableMapOf<Int, String>()
    private val gridLevels = arrayListOf<ZoneArray>()

    fun init(core: CoreInterface) {
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

        readNames()

        core.starManager = this
    }

    private fun readNames() {
        val scanner = Scanner(context.resources.openRawResource(R.raw.extra_names))
        while (scanner.hasNext()) {
            val line = scanner.nextLine().trim()
            val (hip, name) = line.split('|')
            names[hip.toInt()] = name.replace('_', ' ')
        }
    }

    fun getName(hip: Int): String? {
        return names[hip]
    }

    fun initTriangle(level: Int, index: Int, t: Triangle) {
        gridLevels[level].initTriangle(index, t)
    }

    fun searchAround(level: Int, index: Int, v: Vec3, cosLimFov: Double, resultSet: MutableList<DetailedStar>) {
        gridLevels[level].searchAround(index, v, cosLimFov, resultSet)
    }

    fun getVMagnitude(star: Star, level: Int): Double {
        return gridLevels[level].getVMagnitude(star)
    }
}