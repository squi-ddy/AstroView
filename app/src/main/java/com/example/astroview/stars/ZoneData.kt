package com.example.astroview.stars

data class ZoneData(
    val stars: Array<Star>,
    val size: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ZoneData

        if (!stars.contentEquals(other.stars)) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        return stars.contentHashCode() * size.hashCode()
    }
}