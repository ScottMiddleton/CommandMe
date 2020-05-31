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
            entity = Exercise::class,
            parentColumns = ["_id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class WorkoutExercises constructor(
    @ColumnInfo
    val workout_id: Long,
    @ColumnInfo
    val exercise_id: Long
) : BaseDbModel()

class WorkoutWithExercises {
    @Embedded
    var workout: Workout? = null

    @Relation(
        parentColumn = "_id",
        entityColumn = "_id",
        entity = Exercise::class,
        associateBy = Junction(
            value = WorkoutExercises::class,
            parentColumn = "workout_id",
            entityColumn = "exercise_id"
        )
    )
    lateinit var exercises: List<Exercise>
}