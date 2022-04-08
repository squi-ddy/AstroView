package com.example.astroview.starmap.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Vec2 private constructor(vx: Number, vy: Number) {
    val x = vx.toDouble()
    val y = vy.toDouble()

    companion object {
        fun fromXY(x: Number, y: Number): Vec2 {
            return Vec2(x, y)
        }

        fun fromPolar(r: Number, theta: Number): Vec2 {
            return Vec2(
                r.toDouble() * cos(theta.toDouble()),
                r.toDouble() * sin(theta.toDouble())
            )
        }
    }

    val magSquared by lazy {
        x * x + y * y
    }

    val mag by lazy {
        sqrt(magSquared)
    }

    operator fun plus(o: Vec2): Vec2 {
        return Vec2(x + o.x, y + o.y)
    }

    operator fun minus(o: Vec2): Vec2 {
        return Vec2(x - o.x, y - o.y)
    }

    operator fun times(s: Number): Vec2 {
        return Vec2(x * s.toDouble(), y * s.toDouble())
    }

    operator fun div(s: Number): Vec2 {
        return Vec2(x / s.toDouble(), y / s.toDouble())
    }

    operator fun unaryMinus(): Vec2 {
        return this * -1
    }

    fun dot(o: Vec2): Double {
        return x * o.x + y * o.y
    }

    fun norm(): Vec2 {
        return this / mag
    }

    fun getTheta(): Double {
        return atan2(y, x)
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}