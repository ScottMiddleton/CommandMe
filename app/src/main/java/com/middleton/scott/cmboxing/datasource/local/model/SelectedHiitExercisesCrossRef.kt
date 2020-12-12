package com.middleton.scott.cmboxing.datasource.local.model

import androidx.room.*

@Entity(
    tableName = "hiit_workout_combinations", primaryKeys = ["hiit_workout_id", "hiit_exercise_id"], foreignKeys = [
        ForeignKey(
            entity = HiitWorkout::class,
            parentColumns = ["_id"],
            childColumns = ["hiit_workout_id"],
            onDelete = ForeignKey.CASCADE
        ), ForeignKey(
            entity = Combination::class,
            parentColumns = ["_id"],
            childColumns = ["hiit_exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class SelectedHiitExercisesCrossRef constructor(
    @ColumnInfo
    var hiit_workout_id: Long,
    @ColumnInfo
    val hiit_exercise_id: Long
) : BaseDbModel()

class HiitWorkoutWithExercises {
    @Embedded
    var hiitWorkout: HiitWorkout? = null

    @Relation(
        parentColumn = "_id",
        entityColumn = "_id",
        entity = HiitExercise::class,
        associateBy = Junction(
            value = SelectedHiitExercisesCrossRef::class,
            parentColumn = "hiit_workout_id",
            entityColumn = "hiit_exercise_id"
        )
    )
    lateinit var hiitExercises: List<HiitExercise>
}