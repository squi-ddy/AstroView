package com.example.astroview.util

import com.example.astroview.stars.*

object StarUtils {
    const val noStarTypes = 3

    fun createStar(type: Long, bytes: ByteArray): Star {
        return when (type) {
            0L -> Star1(bytes)
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

    fun createStar(bytes: ByteArray): Star {
        // infer from length
        return when (bytes.size) {
            Star1.byteCount -> createStar(0L, bytes)
            Star2.byteCount -> createStar(1L, bytes)
            Star3.byteCount -> createStar(2L, bytes)
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
}