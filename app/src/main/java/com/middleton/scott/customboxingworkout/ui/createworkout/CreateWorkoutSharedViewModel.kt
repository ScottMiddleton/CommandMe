package com.middleton.scott.customboxingworkout.ui.createworkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.CombinationFrequency
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutCombinations
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CreateWorkoutSharedViewModel(
    private val localDataSource: LocalDataSource,
    var workoutId: Long
) : CombinationsViewModel(localDataSource) {

    var subscribe = true
    var workout = Workout()

    var workoutCombinations = mutableListOf<WorkoutCombinations>()
    private var combinationFrequencyList = ArrayList<CombinationFrequency>()

    var addedWorkoutCombinations = mutableListOf<WorkoutCombinations>()
    var addedCombinationFrequenciesList = mutableListOf<CombinationFrequency>()

    private val workoutWithCombinationsFlow = localDataSource.getWorkoutWithCombinations(workoutId)
    private val combinationFrequencyListFlow =
        localDataSource.getCombinationFrequencyList(workoutId)

    val workoutWithCombinationsAndFrequenciesLD: LiveData<WorkoutCombinationsAndFrequencies?> =
        workoutWithCombinationsFlow.combine(combinationFrequencyListFlow) { workoutWithCombinations, combinationFrequencies ->
            workoutWithCombinations?.workout?.let { workout = it }

            combinationFrequencyList = combinationFrequencies as ArrayList<CombinationFrequency>
            workoutCombinations =
                localDataSource.getWorkoutCombinations(workoutId) as MutableList<WorkoutCombinations>

            val combined = workoutWithCombinations?.let {
                WorkoutCombinationsAndFrequencies(
                    it,
                    combinationFrequencyList
                )
            }
            combined
        }.asLiveData()


    data class WorkoutCombinationsAndFrequencies(
        val workoutWithCombinations: WorkoutWithCombinations,
        val combinationsFrequencies: ArrayList<CombinationFrequency>
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
            val totalWorkoutCombinations = mutableListOf<WorkoutCombinations>()
            totalWorkoutCombinations.addAll(workoutCombinations)
            totalWorkoutCombinations.addAll(addedWorkoutCombinations)
            val totalCombinationFrequencies = mutableListOf<CombinationFrequency>()
            totalCombinationFrequencies.addAll(combinationFrequencyList)
            totalCombinationFrequencies.addAll(addedCombinationFrequenciesList)

            val id = localDataSource.upsertWorkout(workout)
            workoutId = id

            totalCombinationFrequencies.forEach {
                it.workout_id = id
            }

            totalWorkoutCombinations.forEach {
                it.workout_id = id
            }

            localDataSource.deleteWorkoutCombinations(id)
            localDataSource.upsertWorkoutCombinations(totalWorkoutCombinations)
            localDataSource.deleteCombinationFrequencies(id)
            localDataSource.upsertCombinationFrequencies(totalCombinationFrequencies)

            dbUpdateLD.value = true
        }
    }

    fun setCombination(workoutCombination: WorkoutCombinations, isChecked: Boolean) {
        if (isChecked) {
            addedWorkoutCombinations.add(workoutCombination)
            workoutCombinations.remove(workoutCombination)
        } else {
            addedWorkoutCombinations.remove(workoutCombination)
            workoutCombinations.remove(workoutCombination)
        }
        val totalWorkoutCombinations = mutableListOf<WorkoutCombinations>()
        totalWorkoutCombinations.addAll(workoutCombinations)
        totalWorkoutCombinations.addAll(addedWorkoutCombinations)
        viewModelScope.launch {
            localDataSource.deleteWorkoutCombinations(workoutId)
            localDataSource.upsertWorkoutCombinations(totalWorkoutCombinations)
        }
    }

    fun setCombinationFrequencyList(combinationFrequencyList: ArrayList<CombinationFrequency>) {
        this.combinationFrequencyList = combinationFrequencyList
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