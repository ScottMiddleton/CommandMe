package com.middleton.scott.customboxingworkout.datasource.local

import androidx.lifecycle.LiveData
import com.middleton.scott.customboxingworkout.datasource.local.model.Exercise
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutExercises
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithExercises

class LocalDataSourceImpl(
    private val database: AppDatabase
) : LocalDataSource {

    override fun getWorkoutById(id: Long): Workout? {
        return database.workoutDao().getWorkoutWithId(id)
    }

    override fun getWorkouts(): LiveData<List<Workout>> {
        return database.workoutDao().getWorkouts()
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
