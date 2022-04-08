package com.example.astroview.starmap.stars.data

import com.example.astroview.starmap.math.Vec3
import com.example.astroview.starmap.stars.ZoneData

class Star2(bytes: ByteArray) : Star(bytes) {
    /*
              _______________
    0     x0 |               |
    1        |_______        |
    2     x1 |       |_______|
    3        |               |
    4        |_______________|
    5    dx0 |___            |
    6    dx1 |   |___________|
    7        |_______        |
    8     bV |_______|_______|
    9    mag |_________|_____| bV

    */

    companion object {
        const val byteCount = 10
        const val maxPosVal = 0x7FFFF
    }

    override fun getX0(): Int {
        return getByte(0).toUInt().or(getByte(1).toUInt().shl(8))
            .or(getByte(2).toUInt().and(0xFu).shl(16))
            .toInt()
    }

    override fun getX1(): Int {
        return getByte(2).toUInt().shr(4).or(getByte(3).toUInt().shl(4))
            .or(getByte(4).toUInt().shl(12)).toInt()
    }

    fun getDx0(): Int {
        return getByte(5).toUInt().or(getByte(6).toUInt().and(0x3Fu).shl(8)).toInt()
    }

    fun getDx1(): Int {
        return getByte(6).toUInt().shr(6).or(getByte(7).toUInt().shl(2))
            .or(getByte(8).toUInt().and(0xFu))
            .toInt()
    }

    override fun getBVIndex(): Int {
        return getByte(8).toUInt().shr(4).or(getByte(9).toUInt().and(0x1Fu).shl(4)).toInt()
    }

    override fun getMag(): Int {
        return getByte(9).toUInt().shr(3).toInt()
    }

    init {
        if (bytes.size != byteCount) {
            throw IllegalArgumentException("Invalid size")
        }
    }

    override fun getJ2kPos(z: ZoneData, movementFactor: Double): Vec3 {
        var pos = z.axis0
        pos *= getX0() + movementFactor * getDx0()
        pos += z.axis1 * (getX1() + movementFactor * getDx1())
        pos += z.center
        return pos
    }
}