package com.example.astroview.core

import com.example.astroview.projection.IcosahedronGrid

object AstroViewCore {
    private var geodesicGrid: IcosahedronGrid? = null

    fun getGeodesicGrid(maxLevel: Int): IcosahedronGrid {
        var currentGrid = geodesicGrid
        if (currentGrid == null) {
            currentGrid = IcosahedronGrid(maxLevel)
        } else if (currentGrid.maxLevel < maxLevel) {
            currentGrid = IcosahedronGrid(maxLevel)
        }
        geodesicGrid = currentGrid
        return currentGrid
    }
}