package com.example.astroview.stars

import android.util.Log
import com.example.astroview.util.ByteConverter
import com.example.astroview.util.StarUtils
import java.io.InputStream

class CatalogueReader(private val stream: InputStream) {
    private var isLittleEndian = false
    private var magic = 0L

    companion object {
        private const val FILE_MAGIC = 0x835f040aL
        private const val FILE_MAGIC_OTHER_ENDIAN = 0x0a045f83L
        private const val FILE_MAGIC_NATIVE = 0x835f040bL
    }

    fun read(): ZoneArray {
        val bytes = ByteArray(4)
        stream.read(bytes)
        magic = ByteConverter.bytesToInt(bytes, isLittleEndian)

        when (magic) {
            FILE_MAGIC -> isLittleEndian = false
            FILE_MAGIC_OTHER_ENDIAN -> isLittleEndian = true
        }

        val params = LongArray(8)
        // magic,type,major,minor,level,mag_min,mag_range,mag_steps
        params[0] = magic

        for (i in 1 until 8) {
            stream.read(bytes)
            params[i] = if (i < 4) {
                ByteConverter.bytesToInt(bytes, isLittleEndian)
            } else {
                ByteConverter.bytesToInt(bytes, isLittleEndian).toUInt().toInt().toLong()
            }
        }

        val zoneCount = (20.shl(params[4].toInt().shl(1)))
        val zoneSizes = IntArray(zoneCount)

        for (i in zoneSizes.indices) {
            stream.read(bytes)
            zoneSizes[i] = ByteConverter.bytesToInt(bytes, isLittleEndian).toInt()
        }

        val zones = Array(zoneCount) { i ->
            val starBytes = ByteArray(StarUtils.getByteCount(params[1]))
            ZoneData(
                Array(zoneSizes[i]) {
                    stream.read(starBytes)
                    StarUtils.createStar(starBytes)
                },
                zoneSizes[i]
            )
        }

        Log.e("Cat File Read", params.asList().toString())

        stream.close()

        return ZoneArray(zones, params[4].toInt())
    }
}