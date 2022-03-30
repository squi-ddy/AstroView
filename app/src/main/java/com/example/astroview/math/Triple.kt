package com.example.astroview.math

class Triple<T>(val v0: T, val v1: T, val v2: T) {

    operator fun get(index: Int): T {
        return when (index) {
            0 -> v0
            1 -> v1
            2 -> v2
            else -> throw IndexOutOfBoundsException("Triple has no index $index")
        }
    }
}