package com.middleton.scott.customboxingworkout.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dayMonthYearFormat = SimpleDateFormat("dd/MM/yyyy", Locale.UK)
    private val dayMonthYearTimeFormat = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.UK)
    private val dayOfWeekMonthYearFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.UK)
    private val dayOfWeekMonthFormat = SimpleDateFormat("EEEE dd MMMM", Locale.UK)
    private val dayFormat = SimpleDateFormat("d", Locale.UK)
    private val dayOfWeekFormat = SimpleDateFormat("EE", Locale.UK)
    private val monthFormat = SimpleDateFormat("MMM", Locale.UK)

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

    fun getDaysUntilCollection(todaysDate: Date, collectionDate: Date): Int {
        val collectionDateCal = Calendar.getInstance()
        val todaysDateCal = Calendar.getInstance()
        collectionDateCal.time = collectionDate
        todaysDateCal.time = todaysDate

        var numberOfDays = 0
        while (todaysDateCal.before(collectionDateCal)) {
            numberOfDays++
            todaysDateCal.add(Calendar.DATE, 1)
        }
        return numberOfDays
    }
}