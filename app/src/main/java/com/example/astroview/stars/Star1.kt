package com.example.astroview.stars

import com.example.astroview.math.Vec3
import com.example.astroview.util.ByteConverter
import java.lang.IllegalArgumentException

class Star1(bytes: ByteArray) : Star() {
    /*
              _______________
    0    hip |               |
    1        |               |
    2        |_______________|
    3   cIds |_______________|
    4     x0 |               |
    5        |               |
    6        |               |
    7        |_______________|
    8     x1 |               |
    9        |               |
    10       |               |
    11       |_______________|
    12    bV |_______________|
    13   mag |_______________|
    14 spInt |               |
    15       |_______________|
    16   dx0 |               |
    17       |               |
    18       |               |
    19       |_______________|
    20   dx1 |               |
    21       |               |
    22       |               |
    23       |_______________|
    24   plx |               |
    25       |               |
    26       |               |
    27       |_______________|

    */

    companion object {
        const val byteCount = 28
    }

    val hip by lazy {
        ByteConverter.bytesToInt(bytes.slice(0..2), true).toInt()
    }

    val componentIds by lazy {
        bytes[3].toInt()
    }

    override val x0 by lazy {
        ByteConverter.bytesToInt(bytes.slice(4..7), true).toUInt().toInt()
    }

    override val x1 by lazy {
        ByteConverter.bytesToInt(bytes.slice(8..11), true).toUInt().toInt()
    }

    override val bV by lazy {
        bytes[12].toInt()
    }

    override val mag by lazy {
        bytes[13].toInt()
    }

    val spInt by lazy {
        ByteConverter.bytesToInt(bytes.slice(14..15), false).toInt()
    }

    val dx0 by lazy {
        ByteConverter.bytesToInt(bytes.slice(16..19), true).toUInt().toInt()
    }

    val dx1 by lazy {
        ByteConverter.bytesToInt(bytes.slice(20..23), true).toUInt().toInt()
    }

    val plx by lazy {
        ByteConverter.bytesToInt(bytes.slice(24..27), true).toUInt().toInt()
    }

    init {
        if (bytes.size != byteCount) {
            throw IllegalArgumentException("Invalid size")
        }
    }

    override fun getJ2kPos(z: ZoneData, movementFactor: Double): Vec3 {
        var pos = z.axis0
        pos *= x0.toDouble() + movementFactor * dx0
        pos += z.axis1 * (x1.toDouble() + movementFactor * dx1)
        pos += z.center
        return pos
    }
}