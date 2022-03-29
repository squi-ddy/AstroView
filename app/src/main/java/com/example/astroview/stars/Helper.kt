package com.example.astroview.stars

import android.util.Log

class Helper {
    companion object {
        fun bytesToInt(bytes: ByteArray, isLittleEndian: Boolean): Long {
            if (bytes.size > 4) throw IllegalArgumentException("ByteArray of size ${bytes.size} will not fit in UInt")
            var result = 0L
            for (i in (if (isLittleEndian) bytes.indices.reversed() else bytes.indices)) {
                result = result.shl(8)
                result += bytes[i].toUByte().toLong()
            }
            return result
        }

        fun bytesToInt(bytes: List<Byte>, isLittleEndian: Boolean): Long {
            return bytesToInt(bytes.toByteArray(), isLittleEndian)
        }
    }
}