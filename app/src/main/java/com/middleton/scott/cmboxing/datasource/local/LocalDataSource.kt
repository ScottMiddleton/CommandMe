package com.middleton.scott.cmboxing.datasource.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
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

    fun getCurrentUser() : LiveData<User> {
       return database.userDao().getUser()
    }

    suspend fun insertCurrentUser(user: User){
        database.userDao().nukeTable()
        database.userDao().insert(user)
    }

    // Boxing

    fun getBoxingWorkoutWithCombinationsFlow(workoutId: Long): Flow<WorkoutWithCommands?> {
        return database.workoutDao().getWorkoutWithCombinationsFlow(workoutId)
    }

    fun getBoxingWorkoutWithCombinations(workoutId: Long): WorkoutWithCommands? {
        return database.workoutDao().getWorkoutWithCombinations(workoutId)
    }

    fun getBoxingWorkouts(): Flow<List<Workout>> {
        return database.workoutDao().getWorkouts()
    }

    suspend fun deleteBoxingWorkout(workout: Workout) {
        database.workoutDao().delete(workout)
    }

    fun getCombinations(): Flow<List<Command>> {
        return database.commandnDao().getCommands()
    }

    fun getAllBoxingWorkoutsWithCombinations(): Flow<List<WorkoutWithCommands>> {
        return database.workoutDao().getAllWorkoutsWithCombinations()
    }

    fun getSelectedCombinationCrossRefsFlow(workoutId: Long): Flow<List<SelectedCommandCrossRef>> {
        return database.selectedCommandCrossRefDao().getSelectedCombinationCrossRefsFlow(workoutId)
    }

    fun getSelectedCombinationCrossRefs(workoutId: Long): List<SelectedCommandCrossRef> {
        return database.selectedCommandCrossRefDao().getSelectedCombinationCrossRefs(workoutId)
    }

    suspend fun upsertBoxingWorkout(
        workout: Workout
    ): Long {
        return database.workoutDao().upsert(workout)
    }

    suspend fun upsertWorkoutCombinations(selectedCombinationCrossRefs: List<SelectedCommandCrossRef>) {
        database.selectedCommandCrossRefDao().upsert(selectedCombinationCrossRefs)
    }

    suspend fun upsertWorkoutCombination(selectedCombinationCrossRef: SelectedCommandCrossRef) {
        database.selectedCommandCrossRefDao().upsert(selectedCombinationCrossRef)
    }

    suspend fun deleteWorkoutCombination(selectedCombinationCrossRef: SelectedCommandCrossRef) {
        database.selectedCommandCrossRefDao().delete(selectedCombinationCrossRef)
    }

     suspend fun deleteWorkoutCombinations(workoutId: Long) {
        database.selectedCommandCrossRefDao().deleteByWorkoutId(workoutId)
    }

     suspend fun deleteWorkoutCombination(combinationId: Long) {
        database.selectedCommandCrossRefDao().deleteByCombinationId(combinationId)
    }

     fun getBoxingWorkoutByIdFlow(workoutId: Long): Flow<Workout?> {
        return database.workoutDao().getWorkoutByIdFlow(workoutId)
    }

     fun getBoxingWorkoutById(workoutId: Long): Workout? {
        return database.workoutDao().getWorkoutById(workoutId)
    }

     suspend fun upsertCombination(command: Command): Long {
        return database.commandnDao().upsert(command)
    }

     suspend fun deleteCombination(command: Command) {
        database.commandnDao().delete(command)
    }

}
