package com.middleton.scott.cmboxing.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.middleton.scott.cmboxing.datasource.local.model.BoxingWorkout
import com.middleton.scott.cmboxing.datasource.local.model.BoxingWorkoutWithCombinations
import com.middleton.scott.cmboxing.datasource.local.model.HiitWorkout
import com.middleton.scott.cmboxing.datasource.local.model.HiitWorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
abstract class HiitWorkoutDao : BaseDao<HiitWorkout>() {
    @Query("SELECT * FROM hiitworkout")
    abstract fun getAllHiitWorkoutsWithExercises(): Flow<List<HiitWorkoutWithExercises>>

    @Query("SELECT * FROM hiitworkout")
    abstract fun getHiitWorkouts(): Flow<List<HiitWorkout>>

    @Query("SELECT * FROM hiitworkout WHERE _id = :id")
    abstract fun getHiitWorkoutWithExercisesFlow(id: Long): Flow<HiitWorkoutWithExercises?>

    @Query("SELECT * FROM hiitworkout WHERE _id = :id")
    abstract fun getHiitWorkoutWithExercises(id: Long): HiitWorkoutWithExercises?

    @Query("SELECT * FROM hiitworkout WHERE _id = :id")
    abstract fun getHiitWorkoutByIdFlow(id: Long): Flow<HiitWorkout?>

    @Query("SELECT * FROM hiitworkout WHERE _id = :id")
    abstract fun getHiitWorkoutById(id: Long): HiitWorkout?
}

