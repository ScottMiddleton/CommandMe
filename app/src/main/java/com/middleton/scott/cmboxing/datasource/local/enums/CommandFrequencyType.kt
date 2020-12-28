package com.middleton.scott.cmboxing.datasource.local.enums

import com.middleton.scott.cmboxing.R

enum class CommandFrequencyType(val textResId: Int, val position: Int, val multiplicationValue: Int) {
    VERY_OFTEN(R.string.very_often, 0, 10),
    OFTEN(R.string.often, 1, 7),
    AVERAGE(R.string.average, 2, 4),
    RARE(R.string.rare, 3, 2),
    VERY_RARE(R.string.very_rare, 4, 1);

    companion object {
        fun fromPosition(position: Int): CommandFrequencyType {
            for (combinationFrequencyType in values()) {
                if (combinationFrequencyType.position == position) {
                    return combinationFrequencyType
                }
            }
            return AVERAGE
        }
    }
}
