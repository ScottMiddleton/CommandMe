package com.middleton.scott.cmboxing.datasource.local

import android.content.SharedPreferences
import com.middleton.scott.cmboxing.datasource.local.model.*
import kotlinx.coroutines.flow.Flow

class LocalDataSource(
    private val sharedPreferences: SharedPreferences,
    private val database: AppDatabase
) {

    companion object {
        private const val USER_IS_LOGGED_IN_KEY = "user_is_logged_in"
    }

    var userIsLoggedIn: Boolean?
        get() = sharedPreferences.getBoolean(USER_IS_LOGGED_IN_KEY, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(USER_IS_LOGGED_IN_KEY, value ?: false)
            editor.apply()
        }

    // Boxing

    fun getBoxingWorkoutWithCombinationsFlow(workoutId: Long): Flow<BoxingWorkoutWithCombinations?> {
        return database.boxingWorkoutDao().getWorkoutWithCombinationsFlow(workoutId)
    }

    fun getBoxingWorkoutWithCombinations(workoutId: Long): BoxingWorkoutWithCombinations? {
        return database.boxingWorkoutDao().getWorkoutWithCombinations(workoutId)
    }

    fun getBoxingWorkouts(): Flow<List<BoxingWorkout>> {
        return database.boxingWorkoutDao().getWorkouts()
    }

    suspend fun deleteBoxingWorkout(boxingWorkout: BoxingWorkout) {
        database.boxingWorkoutDao().delete(boxingWorkout)
    }

    fun getCombinations(): Flow<List<Combination>> {
        return database.combinationDao().getCombinations()
    }

    fun getAllBoxingWorkoutsWithCombinations(): Flow<List<BoxingWorkoutWithCombinations>> {
        return database.boxingWorkoutDao().getAllWorkoutsWithCombinations()
    }

    fun getSelectedCombinationCrossRefsFlow(workoutId: Long): Flow<List<SelectedCombinationsCrossRef>> {
        return database.selectedCombinationsCrossRefDao().getSelectedCombinationCrossRefsFlow(workoutId)
    }

    fun getSelectedCombinationCrossRefs(workoutId: Long): List<SelectedCombinationsCrossRef> {
        return database.selectedCombinationsCrossRefDao().getSelectedCombinationCrossRefs(workoutId)
    }

    suspend fun upsertBoxingWorkout(
        boxingWorkout: BoxingWorkout
    ): Long {
        return database.boxingWorkoutDao().upsert(boxingWorkout)
    }

    suspend fun upsertWorkoutCombinations(selectedCombinationCrossRefs: List<SelectedCombinationsCrossRef>) {
        database.selectedCombinationsCrossRefDao().upsert(selectedCombinationCrossRefs)
    }

    suspend fun upsertWorkoutCombination(selectedCombinationCrossRef: SelectedCombinationsCrossRef) {
        database.selectedCombinationsCrossRefDao().upsert(selectedCombinationCrossRef)
    }

    suspend fun deleteWorkoutCombination(selectedCombinationCrossRef: SelectedCombinationsCrossRef) {
        database.selectedCombinationsCrossRefDao().delete(selectedCombinationCrossRef)
    }

     suspend fun deleteWorkoutCombinations(workoutId: Long) {
        database.selectedCombinationsCrossRefDao().deleteByWorkoutId(workoutId)
    }

     suspend fun deleteWorkoutCombination(combinationId: Long) {
        database.selectedCombinationsCrossRefDao().deleteByCombinationId(combinationId)
    }

     fun getBoxingWorkoutByIdFlow(workoutId: Long): Flow<BoxingWorkout?> {
        return database.boxingWorkoutDao().getWorkoutByIdFlow(workoutId)
    }

     fun getBoxingWorkoutById(workoutId: Long): BoxingWorkout? {
        return database.boxingWorkoutDao().getWorkoutById(workoutId)
    }

     suspend fun upsertCombination(combination: Combination): Long {
        return database.combinationDao().upsert(combination)
    }

     suspend fun deleteCombination(combination: Combination) {
        database.combinationDao().delete(combination)
    }

    // Hiit

     fun getHiitWorkouts(): Flow<List<HiitWorkout>> {
        return database.hiitWorkoutDao().getHiitWorkouts()
    }

     suspend fun deleteHiitWorkout(hiitWorkout: HiitWorkout) {
        database.hiitWorkoutDao().delete(hiitWorkout)
    }

     fun getHiitExercises(): Flow<List<HiitExercise>> {
        TODO("Not yet implemented")
    }

     suspend fun upsertHiitWorkout(hiitWorkout: HiitWorkout): Long {
        TODO("Not yet implemented")
    }

     suspend fun upsertHiitExercises(hiitExercise: HiitExercise): Long {
        TODO("Not yet implemented")
    }

     suspend fun deleteHiitExercise(hiitExercise: HiitExercise) {
        TODO("Not yet implemented")
    }

     suspend fun upsertHiitExercisesCrossRefs(selectedHiitExercisesCrossRef: List<SelectedHiitExercisesCrossRef>) {
        TODO("Not yet implemented")
    }

     suspend fun upsertHiitExercisesCrossRef(selectedHiitExercisesCrossRef: SelectedHiitExercisesCrossRef) {
        TODO("Not yet implemented")
    }

     fun getSelectedHiitExercisesCrossRefsFlow(workoutId: Long): Flow<List<SelectedHiitExercisesCrossRef>> {
        TODO("Not yet implemented")
    }

     fun getSelectedHiitExercisesCrossRefs(workoutId: Long): List<SelectedHiitExercisesCrossRef> {
        TODO("Not yet implemented")
    }

     suspend fun deleteHiitExercisesCrossRefs(selectedHiitExercisesCrossRef: SelectedHiitExercisesCrossRef) {
        TODO("Not yet implemented")
    }

     suspend fun deleteHiitExercisesCrossRefs(workoutId: Long) {
        TODO("Not yet implemented")
    }

     suspend fun deleteHiitExercisesCrossRef(combinationId: Long) {
        TODO("Not yet implemented")
    }

     fun getHiitWorkoutByIdFlow(workoutId: Long): Flow<HiitWorkout?> {
        return database.hiitWorkoutDao().getHiitWorkoutByIdFlow(workoutId)
    }

     fun getHiitWorkoutById(workoutId: Long): HiitWorkout? {
        TODO("Not yet implemented")
    }

     fun getHiitWorkoutWithExercisesFlow(workoutId: Long): Flow<HiitWorkoutWithExercises?> {
        TODO("Not yet implemented")
    }

     fun getHiitWorkoutWithExercises(workoutId: Long): HiitWorkoutWithExercises? {
        TODO("Not yet implemented")
    }

     fun getAllHiitWorkoutsWithExercises(): Flow<List<HiitWorkoutWithExercises>> {
        TODO("Not yet implemented")
    }

}
