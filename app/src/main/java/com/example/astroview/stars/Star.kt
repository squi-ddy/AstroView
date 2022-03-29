package com.example.astroview.stars

import java.lang.IllegalArgumentException

class Star(bytes: ByteArray) {
    val hip: Int
    val componentIds: Int
    val x0: Int
    val x1: Int
    val bV: Int
    val mag: Int
    val spInt: Int
    val dx0: Int
    val dx1: Int
    val plx: Int

    init {
        // Spec from https://github.com/Stellarium/stellarium/blob/73149c4c00d433909c2fbfd7cb7a2e44a152141c/src/core/modules/Star.hpp
        if (bytes.size != 28) {
            throw IllegalArgumentException("Invalid size")
        }
        hip = Helper.bytesToInt(bytes.slice(0..2), true).toInt()
        componentIds = bytes[3].toInt()
        x0 = Helper.bytesToInt(bytes.slice(4..7), true).toUInt().toInt()
        x1 = Helper.bytesToInt(bytes.slice(8..11), true).toUInt().toInt()
        bV = bytes[12].toInt()
        mag = bytes[13].toInt()
        spInt = Helper.bytesToInt(bytes.slice(14..15), false).toInt()
        dx0 = Helper.bytesToInt(bytes.slice(16..19), true).toUInt().toInt()
        dx1 = Helper.bytesToInt(bytes.slice(20..23), true).toUInt().toInt()
        plx = Helper.bytesToInt(bytes.slice(24..27), true).toUInt().toInt()
    }
}