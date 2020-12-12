package com.middleton.scott.cmboxing.datasource.local

import com.middleton.scott.cmboxing.datasource.local.model.*
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    // Boxing
    fun getBoxingWorkouts(): Flow<List<BoxingWorkout>>
    suspend fun deleteBoxingWorkout(boxingWorkout: BoxingWorkout)
    fun getCombinations(): Flow<List<Combination>>
    suspend fun upsertBoxingWorkout(boxingWorkout: BoxingWorkout): Long
    suspend fun upsertCombination(combination: Combination): Long
    suspend fun deleteCombination(combination: Combination)
    suspend fun upsertWorkoutCombinations(selectedCombinationCrossRefs: List<SelectedCombinationsCrossRef>)
    suspend fun upsertWorkoutCombination(selectedCombinationCrossRef: SelectedCombinationsCrossRef)
    fun getSelectedCombinationCrossRefsFlow(workoutId: Long): Flow<List<SelectedCombinationsCrossRef>>
    fun getSelectedCombinationCrossRefs(workoutId: Long): List<SelectedCombinationsCrossRef>
    suspend fun deleteWorkoutCombination(selectedCombinationCrossRef: SelectedCombinationsCrossRef)

    suspend fun deleteWorkoutCombinations(workoutId: Long)
    suspend fun deleteWorkoutCombination(combinationId: Long)
    fun getBoxingWorkoutByIdFlow(workoutId: Long): Flow<BoxingWorkout?>
    fun getBoxingWorkoutById(workoutId: Long): BoxingWorkout?
    fun getBoxingWorkoutWithCombinationsFlow(workoutId: Long): Flow<BoxingWorkoutWithCombinations?>
    fun getBoxingWorkoutWithCombinations(workoutId: Long): BoxingWorkoutWithCombinations?
    fun getAllBoxingWorkoutsWithCombinations(): Flow<List<BoxingWorkoutWithCombinations>>


    // HIIT

    fun getHiitWorkouts(): Flow<List<HiitWorkout>>
    suspend fun deleteHiitWorkout(hiitWorkout: HiitWorkout)
    fun getHiitExercises(): Flow<List<HiitExercise>>
    suspend fun upsertHiitWorkout(hiitWorkout: HiitWorkout): Long
    suspend fun upsertHiitExercises(hiitExercise: HiitExercise): Long
    suspend fun deleteHiitExercise(hiitExercise: HiitExercise)
    suspend fun upsertHiitExercisesCrossRefs(selectedHiitExercisesCrossRef: List<SelectedHiitExercisesCrossRef>)
    suspend fun upsertHiitExercisesCrossRef(selectedHiitExercisesCrossRef: SelectedHiitExercisesCrossRef)
    fun getSelectedHiitExercisesCrossRefsFlow(workoutId: Long): Flow<List<SelectedHiitExercisesCrossRef>>
    fun getSelectedHiitExercisesCrossRefs(workoutId: Long): List<SelectedHiitExercisesCrossRef>
    suspend fun deleteHiitExercisesCrossRefs(selectedHiitExercisesCrossRef: SelectedHiitExercisesCrossRef)

    suspend fun deleteHiitExercisesCrossRefs(workoutId: Long)
    suspend fun deleteHiitExercisesCrossRef(combinationId: Long)
    fun getHiitWorkoutByIdFlow(workoutId: Long): Flow<HiitWorkout?>
    fun getHiitWorkoutById(workoutId: Long): HiitWorkout?
    fun getHiitWorkoutWithExercisesFlow(workoutId: Long): Flow<HiitWorkoutWithExercises?>
    fun getHiitWorkoutWithExercises(workoutId: Long): HiitWorkoutWithExercises?
    fun getAllHiitWorkoutsWithExercises(): Flow<List<HiitWorkoutWithExercises>>

}