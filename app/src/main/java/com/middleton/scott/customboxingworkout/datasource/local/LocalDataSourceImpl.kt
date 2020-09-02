package com.middleton.scott.customboxingworkout.datasource.local

import com.middleton.scott.customboxingworkout.datasource.local.model.Exercise
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutExercises
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl(
    private val database: AppDatabase
) : LocalDataSource {

    override fun getWorkoutById(id: Long): Flow<Workout?> {
        return database.workoutDao().getWorkoutWithId(id)
    }

    override fun getWorkouts(): Flow<List<Workout>> {
        return database.workoutDao().getWorkouts()
    }

    override fun upsertWorkout(workout: Workout, exerciseIds: List<Long>?) {
        val id = database.workoutDao().upsert(workout)
        if (exerciseIds != null) {
            for (exerciseId in exerciseIds) {
                database.workoutExercisesDao().upsert(WorkoutExercises(id, exerciseId))
            }
        }
    }

    override fun upsertExercise(exercise: Exercise): Long {
        return database.exerciseDao().upsert(exercise)
    }

    override fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>> {
        return database.workoutDao().getWorkoutsWithExercises()
    }


}
