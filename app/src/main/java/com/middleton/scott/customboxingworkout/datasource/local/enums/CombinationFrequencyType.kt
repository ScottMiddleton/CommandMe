package com.middleton.scott.customboxingworkout.datasource.local.enums

import com.middleton.scott.commandMeBoxing.R

enum class CombinationFrequencyType(val textResId: Int, val position: Int) {
    VERY_OFTEN(R.string.very_often, 0),
    OFTEN(R.string.often, 1),
    AVERAGE(R.string.average, 2),
    RARE(R.string.rare, 3),
    VERY_RARE(R.string.very_rare, 4);

    companion object {
        fun fromPosition(position: Int): CombinationFrequencyType {
            for (combinationFrequencyType in values()) {
                if (combinationFrequencyType.position == position) {
                    return combinationFrequencyType
                }
            }
            return AVERAGE
        }
    }
}
