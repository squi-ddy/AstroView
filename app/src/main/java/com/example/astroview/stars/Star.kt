package com.example.astroview.stars

import com.example.astroview.math.Vec3

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
    abstract fun getBV(): Int

    abstract fun getJ2kPos(z: ZoneData, movementFactor: Double): Vec3
}