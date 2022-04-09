package com.example.astroview.starmap.stars

import com.example.astroview.starmap.stars.data.Star
import com.example.astroview.starmap.stars.data.Star1
import com.example.astroview.starmap.stars.data.Star2
import com.example.astroview.starmap.stars.data.Star3

object StarUtils {
    const val noStarTypes = 3

    fun createStar(manager: StarManager, type: Long, bytes: ByteArray): Star {
        return when (type) {
            0L -> {
                val star = Star1(bytes)
                manager.associateHipparcos(star.getHip(), star)
                star
            }
            1L -> Star2(bytes)
            2L -> Star3(bytes)
            else -> throw IllegalArgumentException("No star of type $type")
        }
    }

    fun getByteCount(type: Long): Int {
        return when (type) {
            0L -> Star1.byteCount
            1L -> Star2.byteCount
            2L -> Star3.byteCount
            else -> throw IllegalArgumentException("No star of type $type")
        }
    }

    fun createStar(manager: StarManager, bytes: ByteArray): Star {
        // infer from length
        return when (bytes.size) {
            Star1.byteCount -> createStar(manager, 0L, bytes)
            Star2.byteCount -> createStar(manager, 1L, bytes)
            Star3.byteCount -> createStar(manager, 2L, bytes)
            else -> throw IllegalArgumentException("No star with ${bytes.size} bytes")
        }
    }

    fun getMaxPosVal(star: Star): Int {
        return when (star) {
            is Star1 -> Star1.maxPosVal
            is Star2 -> Star2.maxPosVal
            is Star3 -> Star3.maxPosVal
            else -> throw IllegalArgumentException("Unknown star type")
        }
    }

    /**
     * Retrieves the star's B-V value.
     * @param star The star to find B-V for.
     */
    fun getBV(star: Star): Double {
        return star.getBVIndex() * (4.0 / 127) - 0.5
    }

    /**
     * Retrieves the star's Hipparcos Index.
     * @param star The star to find Hipparcos Index for
     * @return The Hipparcos Index, or -1 if it is not on the list.
     */
    fun getHipparcosIndex(star: Star): Int {
        if (star !is Star1) return -1
        return star.getHip()
    }
}