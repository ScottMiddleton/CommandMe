package com.middleton.scott.customboxingworkout.ui.createworkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.SelectedCombinationsCrossRef
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
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

    var selectedCombinations = ArrayList<Combination>()
    var selectedCombinationsCrossRefs = ArrayList<SelectedCombinationsCrossRef>()

    private val combinationsFlow = localDataSource.getCombinations()
    private val selectedCombinationCrossRefsFlow =
        localDataSource.getSelectedCombinationCrossRefsFlow(workoutId)

    val workoutLD = localDataSource.getWorkoutById(workoutId).map {
        it?.let {
            this.workout = it
        }
        this.workout
    }.asLiveData()

    val selectedCombinationsLD: LiveData<List<Combination>> =
        combinationsFlow.combine(selectedCombinationCrossRefsFlow) { combinations, selectedCombinationsCrossRefs ->
            selectedCombinations.clear()
            if (workoutId != -1L) {
                this.selectedCombinationsCrossRefs =
                    selectedCombinationsCrossRefs as ArrayList<SelectedCombinationsCrossRef>
            }

            combinations.forEach { combination ->
                this.selectedCombinationsCrossRefs.forEach { selectedCombinationsCrossRef ->
                    if (combination.id == selectedCombinationsCrossRef.combination_id) {
                        this.selectedCombinations.add(combination)
                    }
                }
            }

            this.selectedCombinations
        }.asLiveData()

    val preparationTimeLD = MutableLiveData<Int>()
    val numberOfRoundsLD = MutableLiveData<Int>()
    val workTimeSecsLD = MutableLiveData<Int>()
    val restTimeSecsLD = MutableLiveData<Int>()
    val intensityLD = MutableLiveData<Int>()
    val dbUpdateLD = MutableLiveData<Boolean>()

    fun upsertWorkout() {
        viewModelScope.launch {
            subscribe = false
            localDataSource.upsertWorkoutCombinations(selectedCombinationsCrossRefs)
            localDataSource.upsertWorkout(workout)
            dbUpdateLD.value = true
        }
    }

    fun setCombinationFrequency(selectedCombinationsCrossRef: SelectedCombinationsCrossRef) {
        selectedCombinationsCrossRefs.forEach {
            if (it.combination_id == selectedCombinationsCrossRef.combination_id) {
                it.frequency = selectedCombinationsCrossRef.frequency
            }
        }
    }

    fun setCombination(
        selectedCombinationsCrossRef: SelectedCombinationsCrossRef,
        isChecked: Boolean
    ) {

        if (workoutId == -1L) {
            viewModelScope.launch {
                val newWorkoutId = localDataSource.upsertWorkout(workout)
                workout.id = newWorkoutId
                localDataSource.deleteWorkoutCombinations(newWorkoutId)
            }
        }

        selectedCombinationsCrossRef.workout_id = workoutId
        if (isChecked) {
            selectedCombinationsCrossRefs.add(selectedCombinationsCrossRef)
            selectedCombinationsCrossRefs.forEach {
                if (it.combination_id == selectedCombinationsCrossRef.combination_id) {
                    viewModelScope.launch {
                        localDataSource.upsertWorkoutCombination(
                            selectedCombinationsCrossRef
                        )
                    }
                }
            }
        } else {
            viewModelScope.launch {
                localDataSource.deleteWorkoutCombination(selectedCombinationsCrossRef)
            }
        }

//        selectedCombinations.clear()
//        if (isChecked) {
//            selectedCombinationsCrossRef.workout_id = workoutId
//            selectedCombinationsCrossRefs.add(selectedCombinationsCrossRef)
//        }


//
//                localDataSource.upsertWorkoutCombinations(selectedCombinationsCrossRefs)
//            } else {
//                localDataSource.deleteWorkoutCombinations(workoutId)
//                localDataSource.upsertWorkoutCombinations(selectedCombinationsCrossRefs)
//            }
//        }
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