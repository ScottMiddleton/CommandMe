package com.middleton.scott.customboxingworkout.datasource.local.enums

enum class CombinationFrequencyType(val label: String, val position: Int) {
    VERY_OFTEN("Very Often", 1),
    OFTEN("Often", 2),
    AVERAGE("Average", 3),
    RARE("Rare", 4),
    VERY_RARE("Very Rare", 5);

    companion object {
        fun fromString(label: String?): CombinationFrequencyType? {
            for (combinationFrequencyType in values()) {
                if (combinationFrequencyType.label.equals(label, ignoreCase = true)) {
                    return combinationFrequencyType
                }
            }
            return null
        }
    }
}
