package com.example.astroview.core

import android.icu.util.Calendar
import android.icu.util.TimeZone
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.JulianFields
import java.util.*
import kotlin.math.floor
import kotlin.math.round


object AstroTime {
    lateinit var currentTime: JulianDay

    fun getJDFromSystem(): Calendar {
        return Calendar.getInstance(TimeZone.GMT_ZONE)
    }

    fun calendarToJD(jd: Calendar): Double {
        return jd.get(Calendar.JULIAN_DAY) +
                (1.0 / (24 * 60 * 60 * 1000)) * jd.get(Calendar.MILLISECONDS_IN_DAY) - 0.5
    }

    fun JDToCalendar(jd: Double): Calendar {
        val julianDay = floor(jd + 0.5).toLong()
        val julianTime = round((jd + 0.5 - julianDay) * (24 * 60 * 60 * 1000)).toLong()
        val dateTime = LocalDateTime.MIN.with(JulianFields.JULIAN_DAY, julianDay).plusNanos(julianTime * 1000)
        val date = Date.from(dateTime.toInstant(ZoneOffset.UTC))
        val calendar = Calendar.getInstance(TimeZone.GMT_ZONE)
        calendar.time = date
        return calendar
    }

    fun setJD(jd: Calendar) {
        currentTime.date = calendarToJD(jd)
        currentTime.deltaT = computeDeltaT(jd)
    }

    fun setJDE(newJDE: Calendar) {
        currentTime.deltaT = computeDeltaT(newJDE)
        currentTime.date = calendarToJD(newJDE) - currentTime.deltaT / 86400
        // resetSync()
    }

    fun setJDE(newJDE: Double) {
        setJDE(JDToCalendar(newJDE))
    }

    fun getJDE(): Double {
        return currentTime.date + currentTime.deltaT / 86400
    }

    fun computeDeltaT(jd: Calendar): Double {
        return getDeltaT(jd)
        // TODO: Moon stuff
    }

    // Get DeltaT via Espenak & Meeus (2006)

    // Note: the method here is adapted from
    // "Five Millennium Canon of Solar Eclipses" [Espenak and Meeus, 2006]
    // A summary is described here:
    // http://eclipse.gsfc.nasa.gov/SEhelp/deltatpoly2004.html
    fun getDeltaT(jd: Calendar): Double {
        val year = jd.get(Calendar.YEAR)
        val month = jd.get(Calendar.MONTH)
        val day = jd.get(Calendar.DAY_OF_MONTH)

        val y = getYearFraction(year, month, day)

        var u = (y - 1820) / 100
        var r = (-20 + 32 * u * u)

        when {
            y < -500 -> {
                // equal to defaults
            }
            y < 500 -> {
                u = y / 100
                r =
                    (((((0.0090316521 * u + 0.022174192) * u - 0.1798452) * u - 5.952053) * u + 33.78311) * u - 1014.41) * u + 10583.6
            }
            y < 1600 -> {
                u = (y - 1000) / 100
                r =
                    (((((0.0083572073 * u - 0.005050998) * u - 0.8503463) * u + 0.319781) * u + 71.23472) * u - 556.01) * u + 1574.2
            }
            y < 1700 -> {
                val t = y - 1600
                r = ((t / 7129.0 - 0.01532) * t - 0.9808) * t + 120.0
            }
            y < 1800 -> {
                val t = y - 1700
                r = (((-t / 1174000.0 + 0.00013336) * t - 0.0059285) * t + 0.1603) * t + 8.83
            }
            y < 1860 -> {
                val t = y - 1800
                r =
                    ((((((.000000000875 * t - .0000001699) * t + 0.0000121272) * t - 0.00037436) * t + 0.0041116) * t + 0.0068612) * t - 0.332447) * t + 13.72
            }
            y < 1900 -> {
                val t = y - 1860
                r =
                    ((((t / 233174.0 - 0.0004473624) * t + 0.01680668) * t - 0.251754) * t + 0.5737) * t + 7.62
            }
            y < 1920 -> {
                val t = y - 1900
                r = (((-0.000197 * t + 0.0061966) * t - 0.0598939) * t + 1.494119) * t - 2.79
            }
            y < 1941 -> {
                val t = y - 1920
                r = ((0.0020936 * t - 0.076100) * t + 0.84493) * t + 21.20
            }
            y < 1961 -> {
                val t = y - 1950
                r = ((t / 2547.0 - 1.0 / 233.0) * t + 0.407) * t + 29.07
            }
            y < 1986 -> {
                val t = y - 1975
                r = ((-t / 718.0 - 1 / 260.0) * t + 1.067) * t + 45.45
            }
            y < 2005 -> {
                val t = y - 2000
                r =
                    ((((0.00002373599 * t + 0.000651814) * t + 0.0017275) * t - 0.060374) * t + 0.3345) * t + 63.86
            }
            y < 2032 -> {
                val t = y - 2000
                r = (-0.00331233402 * t + 0.404229283) * t + 62.48
            }
            y < 2050 -> {
                val finalPredictedYear = 2032.0
                val finalPredictedDeltaT = 72.07
                val t = y - finalPredictedYear
                val diff = 2050.0 - finalPredictedYear
                r = finalPredictedDeltaT + (93.0 - finalPredictedDeltaT) * t * t / (diff * diff)
            }
            y < 2150 -> {
                r -= 0.5628 * (2150.0 - y)
            }
        }

        return r
    }

    fun getYearFraction(year: Int, month: Int, day: Int): Double {
        val d = getDayNumInYear(year, month, 0) + day
        val daysInYear = if (isLeapYear(year)) 366 else 365
        return year + d.toDouble() / daysInYear
    }

    fun getDayNumInYear(year: Int, month: Int, day: Int): Int {
        val k = if (isLeapYear(year)) 1 else 2
        return (275 * month / 9) - k * ((month + 9) / 12) + day - 30
    }

    fun isLeapYear(year: Int): Boolean {
        if (year > 1582) {
            // Switch from Gregorian to Julian
            if (year % 100 == 0) {
                return (year % 400 == 0)
            } else {
                return (year % 4 == 0)
            }
        } else {
            return (year % 4 == 0)
        }
    }
}