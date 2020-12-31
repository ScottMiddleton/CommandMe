package com.middleton.scott.cmboxing.datasource.local.typeconverters

import androidx.room.TypeConverter
import com.middleton.scott.cmboxing.ui.createworkout.WorkoutType

class WorkoutTypeConverter {
    @TypeConverter
    fun toValue(enumValue: WorkoutType?): String? {
        return enumValue?.name
    }

    @TypeConverter
    fun fromValue(value: String?): WorkoutType? {
        return value?.let { WorkoutType.valueOf(it) }
    }
}