package com.middleton.scott.cmboxing.datasource.local.model

import androidx.room.*
import com.middleton.scott.cmboxing.datasource.local.enums.CombinationFrequencyType

@Entity(
    tableName = "boxing_workout_combinations", primaryKeys = ["boxing_workout_id", "combination_id"], foreignKeys = [
        ForeignKey(
            entity = BoxingWorkout::class,
            parentColumns = ["_id"],
            childColumns = ["boxing_workout_id"],
            onDelete = ForeignKey.CASCADE
        ), ForeignKey(
            entity = Command::class,
            parentColumns = ["_id"],
            childColumns = ["combination_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class SelectedCombinationsCrossRef constructor(
    @ColumnInfo
    var boxing_workout_id: Long,
    @ColumnInfo
    val combination_id: Long,
    @ColumnInfo
    var frequency: CombinationFrequencyType = CombinationFrequencyType.AVERAGE
) : BaseDbModel()

class BoxingWorkoutWithCombinations {
    @Embedded
    var boxingWorkout: BoxingWorkout? = null

    @Relation(
        parentColumn = "_id",
        entityColumn = "_id",
        entity = Command::class,
        associateBy = Junction(
            value = SelectedCombinationsCrossRef::class,
            parentColumn = "boxing_workout_id",
            entityColumn = "combination_id"
        )
    )
    lateinit var commands: List<Command>
}