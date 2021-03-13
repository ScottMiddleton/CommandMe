package com.middleton.scott.cmboxing.datasource.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.middleton.scott.cmboxing.datasource.local.model.*
import kotlinx.coroutines.flow.Flow
import java.util.ArrayList

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

    fun getWorkoutWithCommands(workoutId: Long): WorkoutWithCommands {
        return database.workoutDao().getWorkoutWithCombinations(workoutId)
    }

    suspend fun deleteWorkout(workout: Workout) {
        database.workoutDao().delete(workout)
    }

    fun getCommands(): Flow<List<Command>> {
        return database.commandDao().getCommands()
    }

    fun getCommandById(id: Long): Command {
        return database.commandDao().getCommandById(id)
    }

    fun getAllWorkoutsWithCommands(): Flow<List<WorkoutWithCommands>> {
        return database.workoutDao().getAllWorkoutsWithCombinations()
    }

    fun getSelectedCommandCrossRefsFlow(workoutId: Long): Flow<List<SelectedCommandCrossRef>> {
        return database.selectedCommandCrossRefDao().getSelectedCombinationCrossRefsFlow(workoutId)
    }

    fun getSelectedCommandCrossRefs(workoutId: Long): List<SelectedCommandCrossRef> {
        return database.selectedCommandCrossRefDao().getSelectedCombinationCrossRefs(workoutId)
    }

    fun getStructuredCommandCrossRefs(workoutId: Long): List<StructuredCommandCrossRef> {
        return database.structuredCommandCrossRefDao().getSelectedCombinationCrossRefs(workoutId)
    }

    fun getStructuredCommandCrossRefsFlow(workoutId: Long): Flow<List<StructuredCommandCrossRef>> {
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

    suspend fun upsertStructuredCommandCrossRefs(structuredCommandCrossRefs: List<StructuredCommandCrossRef>) {
        database.structuredCommandCrossRefDao().upsert(structuredCommandCrossRefs)
    }

    suspend fun deleteStructuredCommandCrossRef(structuredCommandCrossRef: StructuredCommandCrossRef) {
        database.structuredCommandCrossRefDao().delete(structuredCommandCrossRef)
    }

    suspend fun deleteStructuredCommandCrossRefs(workoutId: Long) {
        database.structuredCommandCrossRefDao().deleteByWorkoutId(workoutId)
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

    suspend fun deleteSelectedCommandCrossRefByCommandId(commandId: Long) {
        database.selectedCommandCrossRefDao().deleteByCommandId(commandId)
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
