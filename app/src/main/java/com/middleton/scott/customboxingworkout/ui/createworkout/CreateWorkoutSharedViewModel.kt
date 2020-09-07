package com.middleton.scott.customboxingworkout.ui.createworkout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsViewModel
import kotlinx.coroutines.flow.map

class CreateWorkoutSharedViewModel(
    private val localDataSource: LocalDataSource,
    val workoutId: Long
) : CombinationsViewModel(localDataSource) {
    var workout = Workout()
    val combinations = mutableListOf<Combination>()

    val workoutWithCombinationsLD =
        localDataSource.getWorkoutWithCombinations(workoutId).map { workoutWithCombinations ->
            workoutWithCombinations?.workout?.let { workout -> this.workout = workout }
            workoutWithCombinations?.combinations?.forEach { combination ->
                combinations.add(combination)
            }
            workoutWithCombinations
        }.asLiveData()

    val preparationDurationSecsLD = MutableLiveData<Int>()
    val numberOfRoundsLD = MutableLiveData<Int>()
    val roundDurationSecsLD = MutableLiveData<Int>()
    val restDurationSecsLD = MutableLiveData<Int>()
    val intensityLD = MutableLiveData<Int>()
    val dbUpdateLD = MutableLiveData<Boolean>()

    fun upsertWorkout() {
        workout.let {
            localDataSource.upsertWorkout(
                it,
                combinations
            )
        }
        dbUpdateLD.value = true
    }

    fun setCombination(combination: Combination, checked: Boolean) {
        if (checked) {
            combinations.add(combination)
        } else {
            combinations.remove(combination)
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
            preparationDurationSecsLD.value = preparationDurationSecs
        }
    }

    fun setNumberOfRounds(numberOfRounds: Int) {
        workout.numberOfRounds = numberOfRounds
        numberOfRoundsLD.value = numberOfRounds
    }

    fun setRoundDuration(roundDurationSecs: Int) {
        if (roundDurationSecs >= 0) {
            workout.round_duration_secs = roundDurationSecs
            roundDurationSecsLD.value = roundDurationSecs
        }
    }

    fun setRestDuration(restDurationSecs: Int) {
        if (restDurationSecs >= 0) {
            workout.rest_duration_secs = restDurationSecs
            restDurationSecsLD.value = restDurationSecs
        }
    }

    fun setIntensity(intensity: Int) {
        workout.intensity = intensity
        intensityLD.value = intensity
    }
}