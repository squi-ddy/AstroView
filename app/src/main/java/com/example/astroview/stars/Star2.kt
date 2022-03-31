package com.example.astroview.stars

import com.example.astroview.math.Vec3

class Star2(bytes: ByteArray) : Star() {
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
    }

    override val x0 by lazy {
        bytes[0].toUInt().or(bytes[1].toUInt().shl(8)).or(bytes[2].toUInt().and(0xFu).shl(16))
            .toInt()
    }

    override val x1 by lazy {
        bytes[2].toUInt().shr(4).or(bytes[3].toUInt().shl(4)).or(bytes[4].toUInt().shl(12)).toInt()
    }

    val dx0 by lazy {
        bytes[5].toUInt().or(bytes[6].toUInt().and(0x3Fu).shl(8)).toInt()
    }

    val dx1 by lazy {
        bytes[6].toUInt().shr(6).or(bytes[7].toUInt().shl(2)).or(bytes[8].toUInt().and(0xFu))
            .toInt()
    }

    override val bV by lazy {
        bytes[8].toUInt().shr(4).or(bytes[9].toUInt().and(0x1Fu).shl(4)).toInt()
    }

    override val mag by lazy {
        bytes[9].toUInt().shr(3).toInt()
    }

    init {
        if (bytes.size != byteCount) {
            throw IllegalArgumentException("Invalid size")
        }
    }

    override fun getJ2kPos(z: ZoneData, movementFactor: Double): Vec3 {
        var pos = z.axis0
        pos *= x0 + movementFactor * dx0
        pos += z.axis1 * (x1 + movementFactor * dx1)
        pos += z.center
        return pos
    }
}