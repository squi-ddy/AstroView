package com.example.astroview.core

import com.example.astroview.projection.GeodesicGrid

object AstroViewCore {
    private var geodesicGrid: GeodesicGrid? = null

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
}