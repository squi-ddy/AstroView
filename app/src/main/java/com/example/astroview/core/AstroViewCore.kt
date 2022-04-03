package com.example.astroview.core

import android.content.Context
import android.util.Log
import com.example.astroview.astro.Coordinates
import com.example.astroview.math.Vec3
import com.example.astroview.projection.GeodesicGrid
import com.example.astroview.stars.AltAzStar
import com.example.astroview.stars.Star
import com.example.astroview.stars.StarManager

class AstroViewCore private constructor() {
    var geodesicGrid: GeodesicGrid? = null
    var starManager: StarManager? = null

    companion object {
        fun getCore(context: Context): AstroViewCore {
            val core = AstroViewCore()
            val manager = StarManager(context)
            manager.init(core)
            return core
        }
    }

    fun getGeodesicGrid(maxLevel: Int): GeodesicGrid {
        var currentGrid = geodesicGrid
        if (currentGrid == null) {
            currentGrid = GeodesicGrid(maxLevel)
        } else if (currentGrid.maxLevel < maxLevel) {
            currentGrid = GeodesicGrid(maxLevel)
        }
        geodesicGrid = currentGrid
        return currentGrid
    }

    /**
     * Get stars in the current viewport.
     * @param altAz current Alt-Az of the viewport
     * @param cosLimitFov Current field-of-view.
     */
    fun getStarsInViewport(altAz: Vec3, cosLimitFov: Double): Set<AltAzStar> {
        Log.e("sus", "get called")
        val grid = geodesicGrid!!
        return grid.searchAround(2, altAz, cosLimitFov, starManager!!)
    }

    /**
     * Converts a vector in Alt-Az to J2k coordinates.
     * @param altAz point in Alt-Az
     * @return Point in J2k coordinates
     */
    fun altAzToJ2k(altAz: Vec3): Vec3 {
        TODO("This")
    }
}