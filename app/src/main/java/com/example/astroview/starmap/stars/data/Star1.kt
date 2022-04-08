package com.example.astroview.starmap.stars.data

import com.example.astroview.starmap.math.Vec3
import com.example.astroview.starmap.stars.ZoneData
import com.example.astroview.util.ByteConverter
import java.lang.IllegalArgumentException

class Star1(bytes: ByteArray) : Star(bytes) {
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
        const val maxPosVal = 0x7FFFFFFF
    }

    fun getHip(): Int {
        return ByteConverter.bytesToInt(bytes.slice(0..2), true).toInt()
    }

    fun getComponentIds(): Int {
        return getByte(3).toInt()
    }

    override fun getX0(): Int {
        return ByteConverter.bytesToInt(bytes.slice(4..7), true).toUInt().toInt()
    }

    override fun getX1(): Int {
        return ByteConverter.bytesToInt(bytes.slice(8..11), true).toUInt().toInt()
    }

    override fun getBVIndex(): Int {
        return getByte(12).toInt()
    }

    override fun getMag(): Int {
        return getByte(13).toInt()
    }

    fun getSpInt(): Int {
        return ByteConverter.bytesToInt(bytes.slice(14..15), false).toInt()
    }

    fun getDx0(): Int {
        return ByteConverter.bytesToInt(bytes.slice(16..19), true).toUInt().toInt()
    }

    fun getDx1(): Int {
        return ByteConverter.bytesToInt(bytes.slice(20..23), true).toUInt().toInt()
    }

    fun getPlx(): Int {
        return ByteConverter.bytesToInt(bytes.slice(24..27), true).toUInt().toInt()
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