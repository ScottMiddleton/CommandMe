package com.middleton.scott.cmboxing.ui.createworkout.boxing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.local.LocalDataSource
import com.middleton.scott.cmboxing.datasource.local.model.Combination
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCombinationsCrossRef
import com.middleton.scott.cmboxing.datasource.local.model.BoxingWorkout
import com.middleton.scott.cmboxing.ui.combinations.CombinationsViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CreateBoxingWorkoutSharedViewModel(
    private val localDataSource: LocalDataSource,
    var workoutId: Long
) : CombinationsViewModel(localDataSource) {

    var subscribe = true
    var workout = BoxingWorkout()
    var savedWorkout = BoxingWorkout()
    var userHasAttemptedToSave = false

    var selectedCombinations = ArrayList<Combination>()
    var selectedCombinationsCrossRefs = ArrayList<SelectedCombinationsCrossRef>()
    var savedSelectedCombinationsCrossRefs = ArrayList<SelectedCombinationsCrossRef>()

    private val combinationsFlow = localDataSource.getCombinations()
    private val selectedCombinationCrossRefsFlow =
        localDataSource.getSelectedCombinationCrossRefsFlow(workoutId)

    val workoutLD = localDataSource.getBoxingWorkoutByIdFlow(workoutId).map {
        it?.let {
            this.workout = it
        }
        this.workout
    }.asLiveData()

    init {
        localDataSource.getBoxingWorkoutById(workoutId)?.let { savedWorkout = it }

        savedSelectedCombinationsCrossRefs =
            localDataSource.getSelectedCombinationCrossRefs(workoutId) as ArrayList<SelectedCombinationsCrossRef>
    }

    val selectedCombinationsLD: LiveData<List<Combination>> =
        combinationsFlow.combine(selectedCombinationCrossRefsFlow) { combinations, selectedCombinationsCrossRefs ->
            selectedCombinations.clear()
            if (workoutId != -1L) {
                this.selectedCombinationsCrossRefs =
                    selectedCombinationsCrossRefs as ArrayList<SelectedCombinationsCrossRef>
            }

            // Iterate through all combinations, find the ones with matching combination Ids to
            // list of selected combination id, and add them to selectedCombinations list
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
    val showCancellationDialogLD = MutableLiveData<Boolean>()
    val workoutNameValidatedLD = MutableLiveData<Boolean>()
    val combinationsValidatedLD = MutableLiveData<Boolean>()
    val requiredSummaryFieldLD = MutableLiveData<Boolean>()

    fun upsertWorkout() {
        viewModelScope.launch {
            subscribe = false
            localDataSource.upsertWorkoutCombinations(selectedCombinationsCrossRefs)
            localDataSource.upsertBoxingWorkout(workout)
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

    fun addCombination(
        selectedCombinationsCrossRef: SelectedCombinationsCrossRef
    ) {
        if (workoutId == -1L) {
            viewModelScope.launch {
                // Upsert the workout and assign its ID
                val newWorkoutId = localDataSource.upsertBoxingWorkout(workout)
                workout.id = newWorkoutId
                // Assign the workout Id to this selected combinations
                selectedCombinationsCrossRef.boxing_workout_id = newWorkoutId
                selectedCombinationsCrossRefs.add(selectedCombinationsCrossRef)
                localDataSource.upsertWorkoutCombination(
                    selectedCombinationsCrossRef
                )
            }
        } else {
            selectedCombinationsCrossRef.boxing_workout_id = workoutId
            viewModelScope.launch {
                localDataSource.upsertWorkoutCombination(
                    selectedCombinationsCrossRef
                )
            }
        }
    }

    fun removeCombination(selectedCombinationsCrossRef: SelectedCombinationsCrossRef) {
        selectedCombinationsCrossRef.boxing_workout_id = workout.id

        if (workoutId == -1L) {
            selectedCombinationsCrossRefs.remove(selectedCombinationsCrossRef)
        }

        viewModelScope.launch {
            localDataSource.deleteWorkoutCombination(selectedCombinationsCrossRef)
        }
    }

    fun setWorkoutName(name: String) {
        if (name.isNotEmpty()) {
            workout.name = name
        } else {
            workout.name = ""
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

    fun cancelChanges() {
        if (workoutId == -1L) {
            viewModelScope.launch {
                localDataSource.deleteBoxingWorkout(workout)
                localDataSource.deleteWorkoutCombinations(workout.id)
                dbUpdateLD.value = true
            }
        } else {
            viewModelScope.launch {
                localDataSource.deleteBoxingWorkout(workout)
                localDataSource.deleteWorkoutCombinations(workout.id)
                localDataSource.upsertBoxingWorkout(savedWorkout)
                localDataSource.upsertWorkoutCombinations(savedSelectedCombinationsCrossRefs)
                dbUpdateLD.value = true
            }
        }
    }

    fun onCancel() {
        showCancellationDialogLD.value =
            !(savedWorkout == workout && savedSelectedCombinationsCrossRefs == selectedCombinationsCrossRefs)
    }

    fun validateSaveAttempt() {
        userHasAttemptedToSave = true
        if (workout.name.isNullOrBlank()) {
            workoutNameValidatedLD.value = false
        }

        if (selectedCombinations.isEmpty()) {
            combinationsValidatedLD.value = false
        }

        if (selectedCombinations.isNotEmpty() && !workout.name.isNullOrBlank()) {
            upsertWorkout()
        }

        if (selectedCombinations.isNotEmpty() && workout.name.isNullOrBlank()) {
            requiredSummaryFieldLD.value = true
        }
    }
}