package com.example.customboxingworkout.datasource.local.model

import androidx.room.*

@Entity(tableName = "workout")

data class Workout constructor(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "number_of_rounds")
    var numberOfRounds: Int,
    @ColumnInfo(name = "round_duration_secs")
    var round_duration_secs: Int,
    @ColumnInfo(name = "rest_duration_secs")
    var rest_duration_secs: Int): BaseDbModel()

data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "exerciseId",
        associateBy = Junction(WorkoutExerciseCrossRef::class)
    )
    val exercises: List<Exercise>
)
