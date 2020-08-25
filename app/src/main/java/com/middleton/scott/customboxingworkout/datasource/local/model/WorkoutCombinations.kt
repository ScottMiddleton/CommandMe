package com.middleton.scott.customboxingworkout.datasource.local.model

import androidx.room.*

@Entity(
    tableName = "workout_exercises", primaryKeys = ["workout_id", "exercise_id"], foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["_id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE
        ), ForeignKey(
            entity = Combination::class,
            parentColumns = ["_id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class WorkoutCombinations constructor(
    @ColumnInfo
    val workout_id: Long,
    @ColumnInfo
    val exercise_id: Long
) : BaseDbModel()

class WorkoutWithCombinations {
    @Embedded
    var workout: Workout? = null

    @Relation(
        parentColumn = "_id",
        entityColumn = "_id",
        entity = Combination::class,
        associateBy = Junction(
            value = WorkoutCombinations::class,
            parentColumn = "workout_id",
            entityColumn = "exercise_id"
        )
    )
    lateinit var exercises: List<Combination>
}