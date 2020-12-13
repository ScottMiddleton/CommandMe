package com.middleton.scott.cmboxing.ui.createworkout.hiit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.*
import com.middleton.scott.cmboxing.ui.combinations.CombinationsViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CreateHiitWorkoutSharedViewModel(
    private val dataRepository: DataRepository,
    var workoutId: Long
) : CombinationsViewModel(dataRepository) {

    var subscribe = true
    var workout = HiitWorkout()
    var savedWorkout = HiitWorkout()
    var userHasAttemptedToSave = false

    var selectedExercises = ArrayList<HiitExercise>()
    var selectedHiitExercisesCrossRefs = ArrayList<SelectedHiitExercisesCrossRef>()
    var savedSelectedHiitExercisesCrossRefs = ArrayList<SelectedHiitExercisesCrossRef>()

    private val hiitExercisesFlow = dataRepository.getLocalDataSource().getHiitExercises()
    private val selectedHiitExercisesCrossRefsFlow =
        dataRepository.getLocalDataSource().getSelectedCombinationCrossRefsFlow(workoutId)

    val workoutLD = dataRepository.getLocalDataSource().getHiitWorkoutByIdFlow(workoutId).map {
        it?.let {
            this.workout = it
        }
        this.workout
    }.asLiveData()

    init {
        dataRepository.getLocalDataSource().getHiitWorkoutById(workoutId)?.let { savedWorkout = it }

        savedSelectedHiitExercisesCrossRefs =
            dataRepository.getLocalDataSource().getSelectedHiitExercisesCrossRefs(workoutId) as ArrayList<SelectedHiitExercisesCrossRef>
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
            dataRepository.getLocalDataSource().upsertHiitExercisesCrossRefs(selectedHiitExercisesCrossRefs)
            dataRepository.getLocalDataSource().upsertHiitWorkout(workout)
            dbUpdateLD.value = true
        }
    }

    fun addHiitExercises(
        selectedHiitExercisesCrossRef: SelectedHiitExercisesCrossRef
    ) {
        if (workoutId == -1L) {
            viewModelScope.launch {
                // Upsert the workout and assign its ID
                val newWorkoutId = dataRepository.getLocalDataSource().upsertHiitWorkout(workout)
                workout.id = newWorkoutId
                // Assign the workout Id to this selected combinations
                selectedHiitExercisesCrossRef.hiit_workout_id = newWorkoutId
                selectedHiitExercisesCrossRefs.add(selectedHiitExercisesCrossRef)
                dataRepository.getLocalDataSource().upsertHiitExercisesCrossRef(
                    selectedHiitExercisesCrossRef
                )
            }
        } else {
            selectedHiitExercisesCrossRef.hiit_workout_id = workoutId
            viewModelScope.launch {
                dataRepository.getLocalDataSource().upsertHiitExercisesCrossRef(
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
            dataRepository.getLocalDataSource().deleteWorkoutCombination(selectedCombinationsCrossRef)
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
                dataRepository.getLocalDataSource().deleteHiitWorkout(workout)
                dataRepository.getLocalDataSource().deleteHiitExercisesCrossRefs(workout.id)
                dbUpdateLD.value = true
            }
        } else {
            viewModelScope.launch {
                dataRepository.getLocalDataSource().deleteHiitWorkout(workout)
                dataRepository.getLocalDataSource().deleteHiitExercisesCrossRefs(workout.id)
                dataRepository.getLocalDataSource().upsertHiitWorkout(savedWorkout)
                dataRepository.getLocalDataSource().upsertHiitExercisesCrossRefs(savedSelectedHiitExercisesCrossRefs)
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