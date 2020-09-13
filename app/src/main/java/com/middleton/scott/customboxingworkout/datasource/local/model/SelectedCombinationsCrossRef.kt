package com.middleton.scott.customboxingworkout.datasource.local.model

import androidx.room.*
import com.middleton.scott.customboxingworkout.datasource.local.enums.CombinationFrequencyType

@Entity(
    tableName = "workout_combinations", primaryKeys = ["workout_id", "combination_id"], foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["_id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE
        ), ForeignKey(
            entity = Combination::class,
            parentColumns = ["_id"],
            childColumns = ["combination_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class SelectedCombinationsCrossRef constructor(
    @ColumnInfo
    var workout_id: Long,
    @ColumnInfo
    val combination_id: Long,
    @ColumnInfo
    var frequency: CombinationFrequencyType = CombinationFrequencyType.AVERAGE
) : BaseDbModel()

class WorkoutWithCombinations {
    @Embedded
    var workout: Workout? = null

    @Relation(
        parentColumn = "_id",
        entityColumn = "_id",
        entity = Combination::class,
        associateBy = Junction(
            value = SelectedCombinationsCrossRef::class,
            parentColumn = "workout_id",
            entityColumn = "combination_id"
        )
    )
    lateinit var combinations: List<Combination>
}