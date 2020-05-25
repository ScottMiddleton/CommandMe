package com.example.customboxingworkout.datasource.local

import com.example.customboxingworkout.datasource.local.model.Exercise
import com.example.customboxingworkout.datasource.local.model.Workout
import com.example.customboxingworkout.datasource.local.model.WorkoutExerciseCrossRef
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class LocalDataSourceImpl(
    private val database: AppDatabase
) : LocalDataSource {

    override fun upsertWorkout(workout: Workout, exercises: List<Exercise>) {
        val id = database.workoutDao().upsert(workout)
        for(exercise in exercises){
            database.workoutWithExercisesDao().insert(WorkoutExerciseCrossRef(id, exercise.id))
        }
    }

    override fun upsertExercise(exercise: Exercise): Long {
        return database.exerciseDao().upsert(exercise)
    }


}
