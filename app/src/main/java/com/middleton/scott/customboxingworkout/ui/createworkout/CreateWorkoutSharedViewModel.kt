package com.middleton.scott.customboxingworkout.ui.createworkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.CombinationFrequency
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CreateWorkoutSharedViewModel(
    private val localDataSource: LocalDataSource,
    val workoutId: Long
) : CombinationsViewModel(localDataSource) {

    var workout = Workout()
    val checkedCombinations = mutableListOf<Combination>()
    val combinationFrequencyList = mutableListOf<CombinationFrequency>()

    val workoutWithCombinationsLD =
        localDataSource.getWorkoutWithCombinations(workoutId).map { workoutWithCombinations ->
            workoutWithCombinations?.workout?.let { workout -> this.workout = workout }
            workoutWithCombinations?.combinations?.forEach { combination ->
                checkedCombinations.add(combination)
            }
            workoutWithCombinations
        }.asLiveData()

    val preparationTimeLD = MutableLiveData<Int>()
    val numberOfRoundsLD = MutableLiveData<Int>()
    val workTimeSecsLD = MutableLiveData<Int>()
    val restTimeSecsLD = MutableLiveData<Int>()
    val intensityLD = MutableLiveData<Int>()
    val dbUpdateLD = MutableLiveData<Boolean>()

    fun upsertWorkout() {
        GlobalScope.launch {
            workout.let {
                localDataSource.upsertWorkout(
                    it,
                    checkedCombinations,
                    combinationFrequencyList
                )
            }
        }
        dbUpdateLD.value = true
    }

    fun setCombination(combination: Combination, checked: Boolean) {
        if (checked) {
            checkedCombinations.add(combination)
        } else {
            checkedCombinations.remove(combination)
        }
    }

    fun setCombinationFrequency(combinationId: Long, frequency: Long) {
        val combinationFrequency = CombinationFrequency(workoutId, combinationId, frequency)

        combinationFrequencyList.filterNot {
            it.id == combinationId
        }

        combinationFrequencyList.add(combinationFrequency)
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

    fun getCombinationFrequencyListLD(): LiveData<List<CombinationFrequency>> {
        return localDataSource.getCombinationFrequencyList(workoutId).asLiveData()
    }
}