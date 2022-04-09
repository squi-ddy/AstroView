@file:Suppress("MemberVisibilityCanBePrivate")

package com.example.astroview.api.data

import com.google.gson.annotations.SerializedName

class DateTime : Comparable<DateTime> {
    @SerializedName("day")
    var day: Int? = null

    @SerializedName("month")
    var month: Int? = null

    @SerializedName("year")
    var year: Int? = null

    @SerializedName("hour")
    var hour: Int? = null

    @SerializedName("minute")
    var minute: Int? = null

    @SerializedName("second")
    var second: Int? = null

    fun getDateString(): String? {
        val currDay = day
        val currMonth = month
        val currYear = year
        if (currDay == null || currMonth == null || currYear == null) return null
        return "$currDay/$currMonth/$currYear"
    }

    fun getTimeString(): String? {
        val currHour = hour
        val currMinute = minute
        val currSecond = second
        if (currHour == null || currMinute == null || currSecond == null) return null
        return "%02d:%02d:%02d".format(currHour, currMinute, currSecond)
    }

    fun getDateTimeString(): String? {
        val currTimeString = getTimeString()
        val currDateString = getDateString()
        if (currTimeString == null || currDateString == null) return null
        return "$currDateString $currTimeString"
    }

    override fun compareTo(other: DateTime): Int {
        val currDay = day ?: return 0
        val currMonth = month ?: return 0
        val currYear = year ?: return 0
        val currHour = hour ?: return 0
        val currMinute = minute ?: return 0
        val currSecond = second ?: return 0
        val otherDay = other.day ?: return 0
        val otherMonth = other.month ?: return 0
        val otherYear = other.year ?: return 0
        val otherHour = other.hour ?: return 0
        val otherMinute = other.minute ?: return 0
        val otherSecond = other.second ?: return 0
        val currParams = listOf(currYear, currMonth, currDay, currHour, currMinute, currSecond)
        val otherParams = listOf(otherYear, otherMonth, otherDay, otherHour, otherMinute, otherSecond)
        for (i in 0..5) {
            val comp = currParams[i].compareTo(otherParams[i])
            if (comp != 0) return comp
        }
        return 0
    }
}