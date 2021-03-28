package com.middleton.scott.cmboxing.ui.workouts

import androidx.lifecycle.*
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Workout
import com.middleton.scott.cmboxing.datasource.local.model.WorkoutWithCommands
import com.middleton.scott.cmboxing.ui.createworkout.WorkoutType
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

const val WORKOUT_NOT_YET_VALIDATED = 0
const val WORKOUT_VALID_TRUE = 1
const val WORKOUT_VALID_FALSE = 2

class WorkoutsViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private var allWorkouts = mutableListOf<Workout>()
    private lateinit var previouslyDeletedWorkout: Workout
    lateinit var previouslyClickedWorkout: Workout

    val workoutValidatedLD = MutableLiveData<Int>(WORKOUT_NOT_YET_VALIDATED)

    fun getWorkoutsWithCommandsLD(): LiveData<List<WorkoutWithCommands>> {
        return dataRepository.getLocalDataSource().getAllWorkoutsWithCommands().map {
            allWorkouts.clear()
            it.forEach { workoutWithCombinations ->
                workoutWithCombinations.workout?.let { workout ->
                    allWorkouts.add(workout) }
            }
            it
        }.asLiveData()
    }

    fun deleteWorkout(position: Int): Workout {
        val workout = allWorkouts[position]
        viewModelScope.launch {
            dataRepository.getLocalDataSource().deleteWorkoutAndCrossRefs(workout)
        }
        previouslyDeletedWorkout = workout
        return workout
    }

    fun undoPreviouslyDeletedWorkout() {
        viewModelScope.launch {
            dataRepository.getLocalDataSource().upsertWorkout(previouslyDeletedWorkout)
        }
    }

    fun validateWorkout(workout: Workout?) {
        viewModelScope.launch {
            val structuredCommandCrossRefs = dataRepository.getLocalDataSource().getStructuredCommandCrossRefs(workout?.id ?: -1)
            var round = 1
            var valid = true
            repeat(workout?.numberOfRounds ?: 0) {
                val filtered = structuredCommandCrossRefs.firstOrNull { it.round == round }
                if (filtered == null) {
                    valid = false
                }
                round++
            }

            if(workout?.workout_type == WorkoutType.RANDOM){
                valid = true
            }

            val response = when(valid) {
                true -> WORKOUT_VALID_TRUE
                false -> WORKOUT_VALID_FALSE
            }

            workoutValidatedLD.value = response
        }
    }

    fun userHasPurchasedUnlimitedCommands(): Boolean {
        return dataRepository.getLocalDataSource().userHasPurchasedUnlimitedCommands()
    }
}