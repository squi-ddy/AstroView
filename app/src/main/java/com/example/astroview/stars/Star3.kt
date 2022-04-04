package com.example.astroview.stars

import com.example.astroview.math.Vec3
import java.lang.IllegalArgumentException

class Star3(bytes: ByteArray): Star() {
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

    override val x0 by lazy {
        bytes[0].toUInt().or(bytes[1].toUInt().shl(8)).or(bytes[2].toUInt().and(0x3u).shl(16)).toInt()
    }

    override val x1 by lazy {
        bytes[2].toUInt().shr(2).or(bytes[3].toUInt().shl(6)).or(bytes[4].toUInt().and(0xFu).shl(14)).toInt()
    }

    override val bV by lazy {
        bytes[4].toUInt().shr(4).or(bytes[5].toUInt().and(0x7u).shl(4)).toInt()
    }

    override val mag by lazy {
        bytes[5].toUByte().toUInt().shr(3).toInt()
    }

    override fun getJ2kPos(z: ZoneData, movementFactor: Double): Vec3 {
        var pos = z.axis0
        pos *= x0
        pos += z.center
        pos += z.axis1 * x1
        return pos
    }

    init {
        if (bytes.size != byteCount) {
            throw IllegalArgumentException("Invalid size")
        }
    }
}