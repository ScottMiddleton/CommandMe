package com.middleton.scott.cmboxing.datasource.local

import com.middleton.scott.cmboxing.datasource.local.model.*
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl(
    private val database: AppDatabase
) : LocalDataSource {

    // Boxing

    override fun getBoxingWorkoutWithCombinationsFlow(workoutId: Long): Flow<BoxingWorkoutWithCombinations?> {
        return database.boxingWorkoutDao().getWorkoutWithCombinationsFlow(workoutId)
    }

    override fun getBoxingWorkoutWithCombinations(workoutId: Long): BoxingWorkoutWithCombinations? {
        return database.boxingWorkoutDao().getWorkoutWithCombinations(workoutId)
    }

    override fun getBoxingWorkouts(): Flow<List<BoxingWorkout>> {
        return database.boxingWorkoutDao().getWorkouts()
    }

    override suspend fun deleteBoxingWorkout(boxingWorkout: BoxingWorkout) {
        database.boxingWorkoutDao().delete(boxingWorkout)
    }

    override fun getCombinations(): Flow<List<Combination>> {
        return database.combinationDao().getCombinations()
    }

    override fun getAllBoxingWorkoutsWithCombinations(): Flow<List<BoxingWorkoutWithCombinations>> {
        return database.boxingWorkoutDao().getAllWorkoutsWithCombinations()
    }

    override fun getSelectedCombinationCrossRefsFlow(workoutId: Long): Flow<List<SelectedCombinationsCrossRef>> {
        return database.selectedCombinationsCrossRefDao().getSelectedCombinationCrossRefsFlow(workoutId)
    }

    override fun getSelectedCombinationCrossRefs(workoutId: Long): List<SelectedCombinationsCrossRef> {
        return database.selectedCombinationsCrossRefDao().getSelectedCombinationCrossRefs(workoutId)
    }

    override suspend fun upsertBoxingWorkout(
        boxingWorkout: BoxingWorkout
    ): Long {
        return database.boxingWorkoutDao().upsert(boxingWorkout)
    }

    override suspend fun upsertWorkoutCombinations(selectedCombinationCrossRefs: List<SelectedCombinationsCrossRef>) {
        database.selectedCombinationsCrossRefDao().upsert(selectedCombinationCrossRefs)
    }

    override suspend fun upsertWorkoutCombination(selectedCombinationCrossRef: SelectedCombinationsCrossRef) {
        database.selectedCombinationsCrossRefDao().upsert(selectedCombinationCrossRef)
    }

    override suspend fun deleteWorkoutCombination(selectedCombinationCrossRef: SelectedCombinationsCrossRef) {
        database.selectedCombinationsCrossRefDao().delete(selectedCombinationCrossRef)
    }

    override suspend fun deleteWorkoutCombinations(workoutId: Long) {
        database.selectedCombinationsCrossRefDao().deleteByWorkoutId(workoutId)
    }

    override suspend fun deleteWorkoutCombination(combinationId: Long) {
        database.selectedCombinationsCrossRefDao().deleteByCombinationId(combinationId)
    }

    override fun getBoxingWorkoutByIdFlow(workoutId: Long): Flow<BoxingWorkout?> {
        return database.boxingWorkoutDao().getWorkoutByIdFlow(workoutId)
    }

    override fun getBoxingWorkoutById(workoutId: Long): BoxingWorkout? {
        return database.boxingWorkoutDao().getWorkoutById(workoutId)
    }

    override suspend fun upsertCombination(combination: Combination): Long {
        return database.combinationDao().upsert(combination)
    }

    override suspend fun deleteCombination(combination: Combination) {
        database.combinationDao().delete(combination)
    }

    // Hiit

    override fun getHiitWorkouts(): Flow<List<HiitWorkout>> {
       return database.hiitWorkoutDao().getHiitWorkouts()
    }

    override suspend fun deleteHiitWorkout(hiitWorkout: HiitWorkout) {
        database.hiitWorkoutDao().delete(hiitWorkout)
    }

    override fun getHiitExercises(): Flow<List<HiitExercise>> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertHiitWorkout(hiitWorkout: HiitWorkout): Long {
        TODO("Not yet implemented")
    }

    override suspend fun upsertHiitExercises(hiitExercise: HiitExercise): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteHiitExercise(hiitExercise: HiitExercise) {
        TODO("Not yet implemented")
    }

    override suspend fun upsertHiitExercisesCrossRefs(selectedHiitExercisesCrossRef: List<SelectedHiitExercisesCrossRef>) {
        TODO("Not yet implemented")
    }

    override suspend fun upsertHiitExercisesCrossRef(selectedHiitExercisesCrossRef: SelectedHiitExercisesCrossRef) {
        TODO("Not yet implemented")
    }

    override fun getSelectedHiitExercisesCrossRefsFlow(workoutId: Long): Flow<List<SelectedHiitExercisesCrossRef>> {
        TODO("Not yet implemented")
    }

    override fun getSelectedHiitExercisesCrossRefs(workoutId: Long): List<SelectedHiitExercisesCrossRef> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteHiitExercisesCrossRefs(selectedHiitExercisesCrossRef: SelectedHiitExercisesCrossRef) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteHiitExercisesCrossRefs(workoutId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteHiitExercisesCrossRef(combinationId: Long) {
        TODO("Not yet implemented")
    }

    override fun getHiitWorkoutByIdFlow(workoutId: Long): Flow<HiitWorkout?> {
        return database.hiitWorkoutDao().getHiitWorkoutByIdFlow(workoutId)
    }

    override fun getHiitWorkoutById(workoutId: Long): HiitWorkout? {
        TODO("Not yet implemented")
    }

    override fun getHiitWorkoutWithExercisesFlow(workoutId: Long): Flow<HiitWorkoutWithExercises?> {
        TODO("Not yet implemented")
    }

    override fun getHiitWorkoutWithExercises(workoutId: Long): HiitWorkoutWithExercises? {
        TODO("Not yet implemented")
    }

    override fun getAllHiitWorkoutsWithExercises(): Flow<List<HiitWorkoutWithExercises>> {
        TODO("Not yet implemented")
    }

}
