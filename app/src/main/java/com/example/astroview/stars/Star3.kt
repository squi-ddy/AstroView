package com.example.astroview.stars

import com.example.astroview.math.Vec3
import java.lang.IllegalArgumentException

class Star3(bytes: ByteArray): Star(bytes) {
    /*
              _______________
    0     x0 |               |
    1        |___________    |
    2     x1 |           |___|
    3        |_______        |
    4     bV |_______|_______|
    5    mag |_________|_____| bV

    */

    companion object {
        const val byteCount = 6
        const val maxPosVal = 0x1FFFF
    }

    override fun getX0(): Int {
        return getByte(0).toUInt().or(getByte(1).toUInt().shl(8)).or(getByte(2).toUInt().and(0x3u).shl(16)).toInt()
    }

    override fun getX1(): Int {
        return getByte(2).toUInt().shr(2).or(getByte(3).toUInt().shl(6)).or(getByte(4).toUInt().and(0xFu).shl(14)).toInt()
    }

    override fun getBV(): Int {
        return getByte(4).toUInt().shr(4).or(getByte(5).toUInt().and(0x7u).shl(4)).toInt()
    }

    override fun getMag(): Int {
        return getByte(5).toUInt().shr(3).toInt()
    }

    override fun getJ2kPos(z: ZoneData, movementFactor: Double): Vec3 {
        var pos = z.axis0
        pos *= getX0()
        pos += z.center
        pos += z.axis1 * getX1()
        return pos
    }

    init {
        if (bytes.size != byteCount) {
            throw IllegalArgumentException("Invalid size")
        }
    }
}