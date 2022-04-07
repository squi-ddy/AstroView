package com.example.astroview.core

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.astroview.R
import com.example.astroview.astro.Coordinates
import com.example.astroview.astro.OrientationData
import com.example.astroview.math.Vec2
import com.example.astroview.math.Vec3
import com.example.astroview.phys.TemperatureConverter
import com.example.astroview.phys.WavelengthConverter
import com.example.astroview.projection.GeodesicGrid
import com.example.astroview.stars.StarManager
import com.example.astroview.stars.data.DetailedStar
import com.example.astroview.stars.data.ProjectedStar
import com.example.astroview.stars.data.Star
import com.example.astroview.util.ArrayTriple
import com.example.astroview.util.StarUtils
import kotlin.math.*

class CoreInterface private constructor() {
    private var geodesicGrid: GeodesicGrid? = null
    var starManager: StarManager? = null

    companion object {
        val magBaseline = 100.0.pow(-0.2) // F_x / F_0 = magBaseline ** m_x

        fun getCore(context: Context): CoreInterface {
            val core = CoreInterface()
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
    fun getStarsInViewport(altAz: Vec3, cosLimitFov: Double): List<DetailedStar> {
        val grid = geodesicGrid!!
        val resultList = mutableListOf<DetailedStar>()
        grid.searchAround(
            grid.maxLevel,
            altAzToJ2k(altAz).norm(),
            cosLimitFov,
            starManager!!,
            resultList
        )
        return resultList
    }

    /**
     * Converts a vector in Alt-Az to J2k coordinates.
     * @param altAz point in Alt-Az
     * @return Point in J2k coordinates
     */
    fun altAzToJ2k(altAz: Vec3): Vec3 {
        return Coordinates.matAltAzToJ2k * altAz
    }

    /**
     * Get the visible magnitude of this star.
     * @param star Star to find magnitude of.
     * @param level This star's level in the grid.
     * @return The star's magnitude.
     */
    fun getVMagnitude(star: Star, level: Int): Double {
        return starManager!!.getVMagnitude(star, level)
    }

    /**
     * Project the stars onto a circle.
     * @param stars The list of stars.
     * @param axes The 3 axes, as an [ArrayTriple] of [Vec3].
     * @param r Circle radius.
     * @param cosLimitFov The current FOV.
     * @return A list of [ProjectedStar]
     */
    fun projectStars(
        stars: List<DetailedStar>,
        axes: ArrayTriple<Vec3>,
        r: Double,
        cosLimitFov: Double
    ): List<ProjectedStar> {
        val invCapRadius = 1 / sqrt(1 - cosLimitFov * cosLimitFov)
        val xAxis = altAzToJ2k(axes[0])
        val yAxis = altAzToJ2k(axes[1])
        return List(stars.size) { i ->
            ProjectedStar(
                Vec2.fromXY(
                    (r * invCapRadius * -xAxis.dot(stars[i].j2k)),
                    (r * invCapRadius * yAxis.dot(stars[i].j2k))
                ),
                stars[i]
            )
        }
    }

    /**
     * Returns an ImageView of this star.
     * @param context the current context.
     * @param star the star to render.
     * @param r the viewport's radius.
     * @param margin Margin around the viewport where stars should start disappearing.
     * @return ImageView representing the star.
     */
    fun renderStar(context: Context, star: ProjectedStar, r: Double, margin: Double): ImageView? {
        val renderedStar = ImageView(context)
        renderedStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.star))
        setStarSize(renderedStar)
        if (star.position.magSquared >= (r - margin) * (r - margin)
        ) {
            return null
        }
        setStarColour(renderedStar, star)
        renderedStar.translationX = (star.position.x + r - renderedStar.width / 2).toFloat()
        renderedStar.translationY = (star.position.y + r - renderedStar.height / 2).toFloat()
        return renderedStar
    }

    /**
     * Set the size of the star ImageView.
     * @param star The ImageView representing the star.
     */
    private fun setStarSize(star: ImageView) {
        star.apply {
            maxHeight = CoreConstants.STAR_RADIUS
            maxWidth = CoreConstants.STAR_RADIUS
            minimumHeight = CoreConstants.STAR_RADIUS
            minimumWidth = CoreConstants.STAR_RADIUS
        }
    }

    /**
     * Rotates the text to render the North pointer.
     * @param compass The textview with the North indicator.
     * @param axes The current orientation axes.
     * @param r The radius of the circle.
     * @param c The centre of the circle.
     */
    fun renderNorth(
        compass: TextView,
        axes: ArrayTriple<Vec3>,
        r: Double,
        c: Vec2
    ) {
        val xAxis = axes[0]
        val yAxis = axes[1]
        val northVector = Vec2.fromXY(
            -xAxis.dot(OrientationData.NORTH),
            yAxis.dot(OrientationData.NORTH)
        ).norm()
        compass.translationX = (r * northVector.x + c.x - compass.width / 2).toFloat()
        compass.translationY = (r * northVector.y + c.y - compass.height / 2).toFloat()
        compass.rotation = (northVector.getTheta() / PI * 180 + 90).toFloat()
    }

    /**
     * Gets an opacity based on the visible flux of the star.
     * @param flux Star's magnitude flux.
     */
    private fun getOpacityFromFlux(flux: Double): Double {
        return min(
            1.0,
            exp((flux / 2 - 1) * CoreConstants.FLUX_FACTOR)
        )
    }

    /**
     * Set the colour of a star given its B-V value.
     * @param renderedStar The ImageView of the star.
     * @param star The actual star object.
     */
    private fun setStarColour(renderedStar: ImageView, star: ProjectedStar) {
        val bV = StarUtils.getBV(star.star.star)
        val fluxV = 1.0
        val fluxB = magBaseline.pow(bV) * fluxV
        val temp = TemperatureConverter.bVToTemperature(bV)
        val rgb = WavelengthConverter.calculateRGBFromTemperature(temp)
        val filter = ColorMatrix(
            floatArrayOf(
                0f, 0f, 0f, 0f, rgb[0].toFloat(),
                0f, 0f, 0f, 0f, rgb[1].toFloat(),
                0f, 0f, 0f, 0f, rgb[2].toFloat(),
                0f, 0f, 0f, getOpacityFromFlux(fluxV + fluxB).toFloat(), 0f
            )
        )
        renderedStar.colorFilter = ColorMatrixColorFilter(filter)
    }
}