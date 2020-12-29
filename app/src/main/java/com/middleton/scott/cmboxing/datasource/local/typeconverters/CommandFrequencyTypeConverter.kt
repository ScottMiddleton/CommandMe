package com.middleton.scott.cmboxing.datasource.local.typeconverters

import androidx.room.TypeConverter
import com.middleton.scott.cmboxing.datasource.local.enums.CommandFrequencyType


class CommandFrequencyTypeConverter {
    @TypeConverter
    fun toValue(enumValue: CommandFrequencyType?): String? {
        return enumValue?.name
    }

    @TypeConverter
    fun fromValue(value: String?): CommandFrequencyType? {
        return value?.let { CommandFrequencyType.valueOf(it) }
    }
}