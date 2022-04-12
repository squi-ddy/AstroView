package com.example.astroview.starmap.stars.data

import com.example.astroview.starmap.math.Vec3
import com.example.astroview.starmap.stars.ZoneData

abstract class Star(val bytes: ByteArray) {
    /**
     * A convenience method to get a raw, unsigned byte.
     * @param index Index in this star's bytes.
     * @return The (unsigned) byte at this position.
     */
    protected fun getByte(index: Int): UByte {
        return bytes[index].toUByte()
    }

    abstract fun getX0(): Int
    abstract fun getX1(): Int
    abstract fun getMag(): Int
    abstract fun getBVIndex(): Int

    abstract fun getJ2kPos(z: ZoneData, movementFactor: Double): Vec3

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Star) return false

        return other.bytes.contentEquals(this.bytes)
    }
}