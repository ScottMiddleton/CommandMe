package com.middleton.scott.customboxingworkout.datasource.local.typeconverters

import androidx.room.TypeConverter
import com.middleton.scott.customboxingworkout.datasource.local.enums.CombinationFrequencyType


class CombinationFrequencyTypeConverter {
    @TypeConverter
    fun toValue(enumValue: CombinationFrequencyType?): String? {
        return enumValue?.name
    }

    @TypeConverter
    fun fromValue(value: String?): CombinationFrequencyType? {
        return value?.let { CombinationFrequencyType.valueOf(it) }
    }
}