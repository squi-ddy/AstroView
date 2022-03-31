package com.example.astroview.core

import java.util.*


object Time {
    lateinit var currentTime: JulianDate

    fun getJDFromSystem(): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    }

    fun setJD(jd: Calendar) {
        currentTime.dateCalendar = jd
        currentTime.deltaT = getDeltaT(jd)

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

        val u = (y - 1820) / 100
        val r = (-20 + 32 * u * u)
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