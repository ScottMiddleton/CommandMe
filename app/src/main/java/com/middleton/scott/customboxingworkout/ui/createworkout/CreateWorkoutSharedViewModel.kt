package com.middleton.scott.customboxingworkout.ui.createworkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutCombinations
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CreateWorkoutSharedViewModel(
    private val localDataSource: LocalDataSource,
    var workoutId: Long
) : CombinationsViewModel(localDataSource) {

    var subscribe = true
    var workout = Workout()

    var combinations = ArrayList<Combination>()
    var workoutCombinations = ArrayList<WorkoutCombinations>()

    private val workoutWithCombinationsFlow = localDataSource.getWorkoutWithCombinations(workoutId)
    private val workoutCombinationsFlow = localDataSource.getWorkoutCombinations(workoutId)

    val workoutLD = localDataSource.getWorkoutById(workoutId).map {
        it?.let {
            this.workout = it
        }
    }

    val combinationsAndWorkoutCombinationsLD: LiveData<WorkoutWithCombinationsAndWorkoutCombinations?> =
        workoutWithCombinationsFlow.combine(workoutCombinationsFlow) { workoutWithCombinations, workoutCombinations ->

            workoutWithCombinations?.workout?.let {
                workout = it
            }

            if (workoutId != -1L) {
                this.combinations = workoutWithCombinations?.combinations as ArrayList<Combination>
                this.workoutCombinations = workoutCombinations as ArrayList<WorkoutCombinations>
            }

            val combined = workoutWithCombinations?.let {
                WorkoutWithCombinationsAndWorkoutCombinations(
                    it,
                    workoutCombinations as ArrayList<WorkoutCombinations>
                )
            }
            combined
        }.asLiveData()


    data class WorkoutWithCombinationsAndWorkoutCombinations(
        val workoutWithCombinations: WorkoutWithCombinations,
        val workoutCombinations: ArrayList<WorkoutCombinations>
    )

    val preparationTimeLD = MutableLiveData<Int>()
    val numberOfRoundsLD = MutableLiveData<Int>()
    val workTimeSecsLD = MutableLiveData<Int>()
    val restTimeSecsLD = MutableLiveData<Int>()
    val intensityLD = MutableLiveData<Int>()
    val dbUpdateLD = MutableLiveData<Boolean>()

    fun upsertWorkout() {
        viewModelScope.launch {
            subscribe = false
            localDataSource.upsertWorkout(workout)
            dbUpdateLD.value = true
        }
    }

    fun setCombination(workoutCombination: WorkoutCombinations, isChecked: Boolean) {
        combinations.removeIf {
            it.id == workoutCombination.combination_id
        }

        workoutCombinations.removeIf {
            it.combination_id == workoutCombination.combination_id
        }

        if (isChecked) {
            workoutCombinations.add(workoutCombination)
            allCombinations.forEach {
                if (it.id == workoutCombination.combination_id) {
                    combinations.add(it)
                }
            }
        }

        viewModelScope.launch {
            localDataSource.deleteWorkoutCombinations(workoutId)
            if (workoutId == -1L) {
                val newWorkoutId = localDataSource.upsertWorkout(workout)
                workout.id = newWorkoutId
                workoutCombinations.forEach {
                    it.workout_id = newWorkoutId
                }
                localDataSource.upsertWorkoutCombinations(workoutCombinations)
            } else {
                localDataSource.upsertWorkoutCombinations(workoutCombinations)
            }
        }
    }

    fun setWorkoutName(name: String) {
        if (name.isNotEmpty()) {
            workout.name = name
        }
    }

    fun setPreparationTime(preparationDurationSecs: Int) {
        if (preparationDurationSecs >= 0) {
            workout.preparation_time_secs = preparationDurationSecs
            preparationTimeLD.value = preparationDurationSecs
        }
    }

    fun setNumberOfRounds(numberOfRounds: Int) {
        workout.numberOfRounds = numberOfRounds
        numberOfRoundsLD.value = numberOfRounds
    }

    fun setWorkTime(workTimeSecs: Int) {
        if (workTimeSecs >= 0) {
            workout.work_time_secs = workTimeSecs
            workTimeSecsLD.value = workTimeSecs
        }
    }

    fun setRestTime(restTimeSecs: Int) {
        if (restTimeSecs >= 0) {
            workout.rest_time_secs = restTimeSecs
            restTimeSecsLD.value = restTimeSecs
        }
    }

    fun setIntensity(intensity: Int) {
        workout.intensity = intensity
        intensityLD.value = intensity
    }
}