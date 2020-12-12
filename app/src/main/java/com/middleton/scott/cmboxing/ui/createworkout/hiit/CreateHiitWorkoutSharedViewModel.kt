package com.middleton.scott.cmboxing.ui.createworkout.hiit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.local.LocalDataSource
import com.middleton.scott.cmboxing.datasource.local.model.*
import com.middleton.scott.cmboxing.ui.combinations.CombinationsViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CreateHiitWorkoutSharedViewModel(
    private val localDataSource: LocalDataSource,
    var workoutId: Long
) : CombinationsViewModel(localDataSource) {

    var subscribe = true
    var workout = HiitWorkout()
    var savedWorkout = HiitWorkout()
    var userHasAttemptedToSave = false

    var selectedExercises = ArrayList<HiitExercise>()
    var selectedHiitExercisesCrossRefs = ArrayList<SelectedHiitExercisesCrossRef>()
    var savedSelectedHiitExercisesCrossRefs = ArrayList<SelectedHiitExercisesCrossRef>()

    private val hiitExercisesFlow = localDataSource.getHiitExercises()
    private val selectedHiitExercisesCrossRefsFlow =
        localDataSource.getSelectedCombinationCrossRefsFlow(workoutId)

    val workoutLD = localDataSource.getHiitWorkoutByIdFlow(workoutId).map {
        it?.let {
            this.workout = it
        }
        this.workout
    }.asLiveData()

    init {
        localDataSource.getHiitWorkoutById(workoutId)?.let { savedWorkout = it }

        savedSelectedHiitExercisesCrossRefs =
            localDataSource.getSelectedHiitExercisesCrossRefs(workoutId) as ArrayList<SelectedHiitExercisesCrossRef>
    }

    val selectedCombinationsLD: LiveData<List<HiitExercise>> =
        hiitExercisesFlow.combine(selectedHiitExercisesCrossRefsFlow) { combinations, selectedHiitExercisesCrossRefs ->
            selectedExercises.clear()
            if (workoutId != -1L) {
                this.selectedHiitExercisesCrossRefs =
                    selectedHiitExercisesCrossRefs as ArrayList<SelectedHiitExercisesCrossRef>
            }

            // Iterate through all combinations, find the ones with matching combination Ids to
            // list of selected combination id, and add them to selectedCombinations list
            combinations.forEach { combination ->
                this.selectedHiitExercisesCrossRefs.forEach { selectedCombinationsCrossRef ->
                    if (combination.id == selectedCombinationsCrossRef.hiit_exercise_id) {
                        this.selectedExercises.add(combination)
                    }
                }
            }
            this.selectedExercises
        }.asLiveData()

    val preparationTimeLD = MutableLiveData<Int>()
    val numberOfRoundsLD = MutableLiveData<Int>()
    val workTimeSecsLD = MutableLiveData<Int>()
    val restTimeSecsLD = MutableLiveData<Int>()
    val intensityLD = MutableLiveData<Int>()
    val dbUpdateLD = MutableLiveData<Boolean>()
    val showCancellationDialogLD = MutableLiveData<Boolean>()
    val workoutNameValidatedLD = MutableLiveData<Boolean>()
    val exercisesValidatedLD = MutableLiveData<Boolean>()
    val requiredSummaryFieldLD = MutableLiveData<Boolean>()

    fun upsertWorkout() {
        viewModelScope.launch {
            subscribe = false
            localDataSource.upsertHiitExercisesCrossRefs(selectedHiitExercisesCrossRefs)
            localDataSource.upsertHiitWorkout(workout)
            dbUpdateLD.value = true
        }
    }

    fun addHiitExercises(
        selectedHiitExercisesCrossRef: SelectedHiitExercisesCrossRef
    ) {
        if (workoutId == -1L) {
            viewModelScope.launch {
                // Upsert the workout and assign its ID
                val newWorkoutId = localDataSource.upsertHiitWorkout(workout)
                workout.id = newWorkoutId
                // Assign the workout Id to this selected combinations
                selectedHiitExercisesCrossRef.hiit_workout_id = newWorkoutId
                selectedHiitExercisesCrossRefs.add(selectedHiitExercisesCrossRef)
                localDataSource.upsertHiitExercisesCrossRef(
                    selectedHiitExercisesCrossRef
                )
            }
        } else {
            selectedHiitExercisesCrossRef.hiit_workout_id = workoutId
            viewModelScope.launch {
                localDataSource.upsertHiitExercisesCrossRef(
                    selectedHiitExercisesCrossRef
                )
            }
        }
    }

    fun removeCombination(selectedCombinationsCrossRef: SelectedCombinationsCrossRef) {
        selectedCombinationsCrossRef.boxing_workout_id = workout.id

        if (workoutId == -1L) {
            selectedHiitExercisesCrossRefs.remove(selectedCombinationsCrossRef)
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

    fun cancelChanges() {
        if (workoutId == -1L) {
            viewModelScope.launch {
                localDataSource.deleteHiitWorkout(workout)
                localDataSource.deleteHiitExercisesCrossRefs(workout.id)
                dbUpdateLD.value = true
            }
        } else {
            viewModelScope.launch {
                localDataSource.deleteHiitWorkout(workout)
                localDataSource.deleteHiitExercisesCrossRefs(workout.id)
                localDataSource.upsertHiitWorkout(savedWorkout)
                localDataSource.upsertHiitExercisesCrossRefs(savedSelectedHiitExercisesCrossRefs)
                dbUpdateLD.value = true
            }
        }
    }

    fun onCancel() {
        showCancellationDialogLD.value =
            !(savedWorkout == workout && savedSelectedHiitExercisesCrossRefs == selectedHiitExercisesCrossRefs)
    }

    fun validateSaveAttempt() {
        userHasAttemptedToSave = true
        if (workout.name.isNullOrBlank()) {
            workoutNameValidatedLD.value = false
        }

        if (selectedExercises.isEmpty()) {
            exercisesValidatedLD.value = false
        }

        if (selectedExercises.isNotEmpty() && !workout.name.isNullOrBlank()) {
            upsertWorkout()
        }

        if (selectedExercises.isNotEmpty() && workout.name.isNullOrBlank()) {
            requiredSummaryFieldLD.value = true
        }
    }
}