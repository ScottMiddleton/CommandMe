package com.example.customboxingworkout.datasource.local

import com.example.customboxingworkout.datasource.local.model.Exercise
import com.example.customboxingworkout.datasource.local.model.Workout
import com.example.customboxingworkout.datasource.local.model.WorkoutExercises
import com.example.customboxingworkout.datasource.local.model.WorkoutWithExercises

class LocalDataSourceImpl(
    private val database: AppDatabase
) : LocalDataSource {

    override fun getWorkoutById(id: Long): Workout? {
        return database.workoutDao().getWorkoutWithId(id)
    }

    override fun upsertWorkout(workout: Workout, exerciseIds: List<Long>) {
        val id = database.workoutDao().upsert(workout)
        for (exerciseId in exerciseIds) {
            database.workoutExercisesDao().upsert(WorkoutExercises(id, exerciseId))
        }
    }

    override fun upsertExercise(exercise: Exercise): Long {
        return database.exerciseDao().upsert(exercise)
    }

    override fun getWorkoutsWithExercises(): List<WorkoutWithExercises> {
        return database.workoutDao().getWorkoutsWithExercises()
    }


}
