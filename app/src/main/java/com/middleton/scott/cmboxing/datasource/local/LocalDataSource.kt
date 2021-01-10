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

    fun getWorkoutWithCombinationsFlow(workoutId: Long): Flow<WorkoutWithCommands?> {
        return database.workoutDao().getWorkoutWithCombinationsFlow(workoutId)
    }

    fun getWorkoutWithCombinations(workoutId: Long): WorkoutWithCommands? {
        return database.workoutDao().getWorkoutWithCombinations(workoutId)
    }

    fun getWorkouts(): Flow<List<Workout>> {
        return database.workoutDao().getWorkouts()
    }

    suspend fun deleteWorkout(workout: Workout) {
        database.workoutDao().delete(workout)
    }

    fun getCommands(): Flow<List<Command>> {
        return database.commandDao().getCommands()
    }

    fun getAllWorkoutsWithCombinations(): Flow<List<WorkoutWithCommands>> {
        return database.workoutDao().getAllWorkoutsWithCombinations()
    }

    fun getSelectedCombinationCrossRefsFlow(workoutId: Long): Flow<List<SelectedCommandCrossRef>> {
        return database.selectedCommandCrossRefDao().getSelectedCombinationCrossRefsFlow(workoutId)
    }

    fun getSelectedCombinationCrossRefs(workoutId: Long): List<SelectedCommandCrossRef> {
        return database.selectedCommandCrossRefDao().getSelectedCombinationCrossRefs(workoutId)
    }

    fun getStructuredCombinationCrossRefs(workoutId: Long): List<StructuredCommandCrossRef> {
        return database.structuredCommandCrossRefDao().getSelectedCombinationCrossRefs(workoutId)
    }

    fun getStructuredCombinationCrossRefsFlow(workoutId: Long): Flow<List<StructuredCommandCrossRef>> {
        return database.structuredCommandCrossRefDao().getStructuredCommandCrossRefsFlow(workoutId)
    }

    suspend fun upsertWorkout(
        workout: Workout
    ): Long {
        return database.workoutDao().upsert(workout)
    }

    suspend fun upsertWorkoutCommandsList(selectedCombinationCrossRefs: List<SelectedCommandCrossRef>) {
        database.selectedCommandCrossRefDao().upsert(selectedCombinationCrossRefs)
    }

    suspend fun upsertSelectedCommandCrossRef(selectedCombinationCrossRef: SelectedCommandCrossRef) {
        database.selectedCommandCrossRefDao().upsert(selectedCombinationCrossRef)
    }

    suspend fun upsertStructuredCommandCrossRef(structuredCommandCrossRef: StructuredCommandCrossRef) {
        database.structuredCommandCrossRefDao().upsert(structuredCommandCrossRef)
    }

    suspend fun upsertStructuredCommandCrossRefs(structuredCommandCrossRefs: List<StructuredCommandCrossRef>) {
        database.structuredCommandCrossRefDao().upsert(structuredCommandCrossRefs)
    }

    suspend fun deleteStructuredCommandCrossRef(structuredCommandCrossRef: StructuredCommandCrossRef) {
        database.structuredCommandCrossRefDao().delete(structuredCommandCrossRef)
    }


    suspend fun deleteSelectedCommandCrossRef(selectedCommandCrossRef: SelectedCommandCrossRef) {
        database.selectedCommandCrossRefDao().delete(selectedCommandCrossRef)
    }

     suspend fun deleteStructuredCommandCrossRefById(commandId: Long) {
        database.structuredCommandCrossRefDao().deleteByCommandId(commandId)
    }

    suspend fun deleteSelectedCommandCrossRefById(workoutId: Long) {
        database.selectedCommandCrossRefDao().deleteByWorkoutId(workoutId)
    }

     fun getWorkoutByIdFlow(workoutId: Long): Flow<Workout?> {
        return database.workoutDao().getWorkoutByIdFlow(workoutId)
    }

     fun getWorkoutById(workoutId: Long): Workout? {
        return database.workoutDao().getWorkoutById(workoutId)
    }

     suspend fun upsertCommand(command: Command): Long {
        return database.commandDao().upsert(command)
    }

     suspend fun deleteCommand(command: Command) {
        database.commandDao().delete(command)
    }



}
