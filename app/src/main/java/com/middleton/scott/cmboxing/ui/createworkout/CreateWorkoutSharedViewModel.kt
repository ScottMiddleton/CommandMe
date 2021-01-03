package com.middleton.scott.cmboxing.ui.createworkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Workout
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCommandCrossRef
import com.middleton.scott.cmboxing.ui.commands.CommandsViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CreateWorkoutSharedViewModel(
    private val dataRepository: DataRepository,
    var workoutId: Long
) : CommandsViewModel(dataRepository) {

    var subscribe = true
    var workout = Workout()
    var savedWorkout = Workout()
    var userHasAttemptedToProceedOne = false
    var userHasAttemptedToProceedTwo = false

    var selectedCombinations = ArrayList<Command>()
    var selectedCombinationsCrossRefs = ArrayList<SelectedCommandCrossRef>()
    var savedSelectedCombinationsCrossRefs = ArrayList<SelectedCommandCrossRef>()

    private val combinationsFlow = dataRepository.getLocalDataSource().getCombinations()
    private val selectedCombinationCrossRefsFlow =
        dataRepository.getLocalDataSource().getSelectedCombinationCrossRefsFlow(workoutId)

    val workoutLD = dataRepository.getLocalDataSource().getBoxingWorkoutByIdFlow(workoutId).map {
        it?.let {
            this.workout = it
        }
        this.workout
    }.asLiveData()

    init {
        dataRepository.getLocalDataSource().getBoxingWorkoutById(workoutId)?.let { savedWorkout = it }

        savedSelectedCombinationsCrossRefs =
            dataRepository.getLocalDataSource().getSelectedCombinationCrossRefs(workoutId) as ArrayList<SelectedCommandCrossRef>
    }

    val selectedCombinationsLD: LiveData<List<Command>> =
        combinationsFlow.combine(selectedCombinationCrossRefsFlow) { combinations, selectedCombinationsCrossRefs ->
            selectedCombinations.clear()
            if (workoutId != -1L) {
                this.selectedCombinationsCrossRefs =
                    selectedCombinationsCrossRefs as ArrayList<SelectedCommandCrossRef>
            }

            // Iterate through all combinations, find the ones with matching combination Ids to
            // list of selected combination id, and add them to selectedCombinations list
            combinations.forEach { combination ->
                this.selectedCombinationsCrossRefs.forEach { selectedCombinationsCrossRef ->
                    if (combination.id == selectedCombinationsCrossRef.command_id) {
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
    val tabOneValidatedLD = MutableLiveData(false)
    val tabTwoValidatedLD = MutableLiveData(false)
    val requiredSummaryFieldLD = MutableLiveData<Boolean>()

    fun upsertWorkout() {
        viewModelScope.launch {
            subscribe = false
            dataRepository.getLocalDataSource().upsertWorkoutCombinations(selectedCombinationsCrossRefs)
            dataRepository.getLocalDataSource().upsertBoxingWorkout(workout)
            dbUpdateLD.value = true
        }
    }

    fun setCombinationFrequency(selectedCommandCrossRef: SelectedCommandCrossRef) {
        selectedCombinationsCrossRefs.forEach {
            if (it.command_id == selectedCommandCrossRef.command_id) {
                it.frequency = selectedCommandCrossRef.frequency
            }
        }
    }

    fun addCombination(
        selectedCommandCrossRef: SelectedCommandCrossRef
    ) {
        if (workoutId == -1L) {
            viewModelScope.launch {
                // Upsert the workout and assign its ID
                val newWorkoutId = dataRepository.getLocalDataSource().upsertBoxingWorkout(workout)
                workout.id = newWorkoutId
                // Assign the workout Id to this selected combinations
                selectedCommandCrossRef.workout_id = newWorkoutId
                selectedCombinationsCrossRefs.add(selectedCommandCrossRef)
                dataRepository.getLocalDataSource().upsertWorkoutCombination(
                    selectedCommandCrossRef
                )
            }
        } else {
            selectedCommandCrossRef.workout_id = workoutId
            viewModelScope.launch {
                dataRepository.getLocalDataSource().upsertWorkoutCombination(
                    selectedCommandCrossRef
                )
            }
        }
    }

    fun removeCombination(selectedCommandCrossRef: SelectedCommandCrossRef) {
        selectedCommandCrossRef.workout_id = workout.id

        if (workoutId == -1L) {
            selectedCombinationsCrossRefs.remove(selectedCommandCrossRef)
        }

        viewModelScope.launch {
            dataRepository.getLocalDataSource().deleteWorkoutCombination(selectedCommandCrossRef)
        }
    }

    fun setWorkoutType(type: WorkoutType){
        workout.workout_type = type
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
                dataRepository.getLocalDataSource().deleteBoxingWorkout(workout)
                dataRepository.getLocalDataSource().deleteWorkoutCombinations(workout.id)
                dbUpdateLD.value = true
            }
        } else {
            viewModelScope.launch {
                dataRepository.getLocalDataSource().deleteBoxingWorkout(workout)
                dataRepository.getLocalDataSource().deleteWorkoutCombinations(workout.id)
                dataRepository.getLocalDataSource().upsertBoxingWorkout(savedWorkout)
                dataRepository.getLocalDataSource().upsertWorkoutCombinations(savedSelectedCombinationsCrossRefs)
                dbUpdateLD.value = true
            }
        }
    }

    fun onCancel() {
        showCancellationDialogLD.value =
            !(savedWorkout == workout && savedSelectedCombinationsCrossRefs == selectedCombinationsCrossRefs)
    }

    fun validateTabOne() {
        tabOneValidatedLD.value = workout.name.isNotBlank()
    }

    fun validateTabTwo() {
        tabTwoValidatedLD.value = selectedCombinations.isNotEmpty()
    }
}