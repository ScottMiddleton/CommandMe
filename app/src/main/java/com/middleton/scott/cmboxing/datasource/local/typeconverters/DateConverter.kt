package com.middleton.scott.cmboxing.datasource.local.typeconverters

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun toValue(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromValue(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}