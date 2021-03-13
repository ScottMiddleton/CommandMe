package com.middleton.scott.cmboxing.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    private val dayMonthYearFormat = SimpleDateFormat("dd/MM/yyyy", Locale.UK)
    private val dayMonthYearTimeFormat = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.UK)
    private val dayOfWeekMonthYearFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.UK)
    private val dayOfWeekMonthFormat = SimpleDateFormat("EEEE dd MMMM", Locale.UK)
    private val dayFormat = SimpleDateFormat("d", Locale.UK)
    private val dayOfWeekFormat = SimpleDateFormat("EE", Locale.UK)
    private val monthFormat = SimpleDateFormat("MMM", Locale.UK)
    private val minutesSecondsFormat = SimpleDateFormat("mm:ss", Locale.UK)

    fun fromDayMonthYear(date: String): Date? {
        return dayMonthYearFormat.parse(date)
    }

    fun toDayMonthYear(date: Date): String {
        return dayMonthYearFormat.format(date)
    }

    fun toDayMonthYearTime(date: Date): String {
        return dayMonthYearTimeFormat.format(date)
    }

    fun toDayOfWeekMonthYear(date: Date): String {
        return dayOfWeekMonthYearFormat.format(date)
    }

    fun toDayOfWeekMonth(date: Date): String {
        return dayOfWeekMonthFormat.format(date)
    }

    fun toDay(date: Date): String {
        return dayFormat.format(date)
    }

    fun toDayOfWeek(date: Date): String {
        return dayOfWeekFormat.format(date)
    }

    fun toMonth(date: Date): String {
        return monthFormat.format(date)
    }

    fun toMinuteSeconds(seconds: Int): String {
        return minutesSecondsFormat.format(seconds * 1000)
    }

    fun toHoursMinuteSeconds(millis: Long): String {
        return String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        )
    }

    fun Long.formatAsTime(): String {
        val seconds = (TimeUnit.MILLISECONDS.toSeconds(this) % 60).toInt()
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(this) % 60).toInt()

        return when (val hours = (TimeUnit.MILLISECONDS.toHours(this)).toInt()) {
            0 -> String.format("%02d:%02d", minutes, seconds)
            else -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}