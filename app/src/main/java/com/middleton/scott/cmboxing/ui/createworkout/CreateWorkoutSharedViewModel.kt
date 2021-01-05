package com.middleton.scott.cmboxing.ui.createworkout

import androidx.lifecycle.*
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Workout
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.SelectedCommandCrossRef
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import com.middleton.scott.cmboxing.ui.commands.CommandsViewModel
import kotlinx.coroutines.flow.Flow
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

    var selectedCommands = ArrayList<Command>()
    var selectedCommandCrossRefs = ArrayList<SelectedCommandCrossRef>()
    var structuredCommandCrossRefs = ArrayList<StructuredCommandCrossRef>()
    var savedSelectedCommandCrossRefs = ArrayList<SelectedCommandCrossRef>()

    private val commandsFlow = dataRepository.getLocalDataSource().getCommands()

    lateinit var selectedCommandCrossRefsFlow: Flow<List<SelectedCommandCrossRef>>
    lateinit var workoutLD: LiveData<Workout>
    lateinit var selectedCommandsLD: LiveData<List<Command>>
    lateinit var structuredCommandCrossRefsLD: LiveData<List<StructuredCommandCrossRef>>

    init {
        viewModelScope.launch {
            if (workoutId == -1L) {
                val newWorkoutId = dataRepository.getLocalDataSource().upsertWorkout(workout)

                dataRepository.getLocalDataSource().getWorkoutById(newWorkoutId)
                    ?.let { savedWorkout = it }

                workoutLD =
                    dataRepository.getLocalDataSource().getWorkoutByIdFlow(newWorkoutId).map {
                        it?.let {
                            workout = it
                        }
                        workout
                    }.asLiveData()

                selectedCommandCrossRefsFlow = dataRepository.getLocalDataSource()
                    .getSelectedCombinationCrossRefsFlow(newWorkoutId)


                savedSelectedCommandCrossRefs =
                    dataRepository.getLocalDataSource()
                        .getSelectedCombinationCrossRefs(newWorkoutId) as ArrayList<SelectedCommandCrossRef>

                selectedCommandsLD =
                    commandsFlow.combine(selectedCommandCrossRefsFlow) { commands, it ->
                        selectedCommands.clear()
                        if (workoutId != -1L) {
                            selectedCommandCrossRefs = it as ArrayList<SelectedCommandCrossRef>
                        }

                        // Iterate through all combinations, find the ones with matching combination Ids to
                        // list of selected combination id, and add them to selectedCombinations list
                        commands.forEach { combination ->
                            it.forEach { selectedCombinationsCrossRef ->
                                if (combination.id == selectedCombinationsCrossRef.command_id) {
                                    selectedCommands.add(combination)
                                }
                            }
                        }
                        selectedCommands
                    }.asLiveData()

                structuredCommandCrossRefsLD =
                    dataRepository.getLocalDataSource()
                        .getStructuredCombinationCrossRefsFlow(newWorkoutId).map {
                            structuredCommandCrossRefs = it as ArrayList<StructuredCommandCrossRef>
                            it
                        }.asLiveData()

                workoutId = newWorkoutId

            } else {

                selectedCommandCrossRefsFlow = dataRepository.getLocalDataSource()
                    .getSelectedCombinationCrossRefsFlow(workoutId)
                workoutLD =
                    dataRepository.getLocalDataSource().getWorkoutByIdFlow(workoutId).map {
                        it?.let {
                            workout = it
                        }
                        workout
                    }.asLiveData()

                dataRepository.getLocalDataSource().getWorkoutById(workoutId)
                    ?.let { savedWorkout = it }

                savedSelectedCommandCrossRefs =
                    dataRepository.getLocalDataSource()
                        .getSelectedCombinationCrossRefs(workoutId) as ArrayList<SelectedCommandCrossRef>

                selectedCommandsLD =
                    commandsFlow.combine(selectedCommandCrossRefsFlow) { commands, it ->
                        selectedCommands.clear()
                        if (workoutId != -1L) {
                            selectedCommandCrossRefs = it as ArrayList<SelectedCommandCrossRef>
                        }

                        // Iterate through all combinations, find the ones with matching combination Ids to
                        // list of selected combination id, and add them to selectedCombinations list
                        commands.forEach { combination ->
                            it.forEach { selectedCombinationsCrossRef ->
                                if (combination.id == selectedCombinationsCrossRef.command_id) {
                                    selectedCommands.add(combination)
                                }
                            }
                        }
                        selectedCommands
                    }.asLiveData()

                structuredCommandCrossRefsLD =
                    dataRepository.getLocalDataSource()
                        .getStructuredCombinationCrossRefsFlow(workoutId).map {
                            structuredCommandCrossRefs = it as ArrayList<StructuredCommandCrossRef>
                            it
                        }.asLiveData()
            }
        }
    }


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
            dataRepository.getLocalDataSource().upsertWorkoutCommandsList(selectedCommandCrossRefs)
            dataRepository.getLocalDataSource().upsertWorkout(workout)
            dbUpdateLD.value = true
        }
    }

    fun setCombinationFrequency(selectedCommandCrossRef: SelectedCommandCrossRef) {
        selectedCommandCrossRefs.forEach {
            if (it.command_id == selectedCommandCrossRef.command_id) {
                it.frequency = selectedCommandCrossRef.frequency
            }
        }
    }

    fun addStructuredCommand(
        structuredCommandCrossRef: StructuredCommandCrossRef
    ) {
        if (workoutId == -1L) {
            viewModelScope.launch {
                // Upsert the workout and assign its ID
                val newWorkoutId = dataRepository.getLocalDataSource().upsertWorkout(workout)
                workout.id = newWorkoutId
                workoutId = newWorkoutId
                // Assign the workout Id to this selected combinations
                structuredCommandCrossRef.workout_id = newWorkoutId
                structuredCommandCrossRefs.add(structuredCommandCrossRef)

            }
        } else {
            structuredCommandCrossRef.workout_id = workoutId
            viewModelScope.launch {
                dataRepository.getLocalDataSource().upsertStructuredCommand(
                    structuredCommandCrossRef
                )
            }
        }
    }

    fun addSelectedCommand(
        selectedCommandCrossRef: SelectedCommandCrossRef
    ) {
        if (workoutId == -1L) {
            viewModelScope.launch {
                // Upsert the workout and assign its ID
                val newWorkoutId = dataRepository.getLocalDataSource().upsertWorkout(workout)
                workout.id = newWorkoutId
                workoutId = newWorkoutId
                // Assign the workout Id to this selected combinations
                selectedCommandCrossRef.workout_id = newWorkoutId
                selectedCommandCrossRefs.add(selectedCommandCrossRef)
                dataRepository.getLocalDataSource().upsertWorkoutCommand(
                    selectedCommandCrossRef
                )

                // Test code
                val structuredCommand = StructuredCommandCrossRef(
                    newWorkoutId,
                    selectedCommandCrossRef.command_id,
                    1,
                    30,
                    0
                )
                dataRepository.getLocalDataSource().upsertStructuredCommand(
                    structuredCommand
                )
            }
        } else {
            selectedCommandCrossRef.workout_id = workoutId
            selectedCommandCrossRefs.add(selectedCommandCrossRef)
            viewModelScope.launch {
                dataRepository.getLocalDataSource().upsertWorkoutCommand(
                    selectedCommandCrossRef
                )
            }
        }
    }

    fun removeSelectedCommand(selectedCommandCrossRef: SelectedCommandCrossRef) {
        selectedCommandCrossRef.workout_id = workout.id

        if (workoutId == -1L) {
            selectedCommandCrossRefs.remove(selectedCommandCrossRef)
        }

        viewModelScope.launch {
            dataRepository.getLocalDataSource().deleteWorkoutCommand(selectedCommandCrossRef)
        }
    }

    fun setWorkoutType(type: WorkoutType) {
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
                dataRepository.getLocalDataSource().deleteWorkout(workout)
                dataRepository.getLocalDataSource().deleteWorkoutCombinations(workout.id)
                dbUpdateLD.value = true
            }
        } else {
            viewModelScope.launch {
                dataRepository.getLocalDataSource().deleteWorkout(workout)
                dataRepository.getLocalDataSource().deleteWorkoutCombinations(workout.id)
                dataRepository.getLocalDataSource().upsertWorkout(savedWorkout)
                dataRepository.getLocalDataSource()
                    .upsertWorkoutCommandsList(savedSelectedCommandCrossRefs)
                dbUpdateLD.value = true
            }
        }
    }

    fun onCancel() {
        showCancellationDialogLD.value =
            !(savedWorkout == workout && savedSelectedCommandCrossRefs == selectedCommandCrossRefs)
    }

    fun validateTabOne() {
        tabOneValidatedLD.value = workout.name.isNotBlank()
    }

    fun validateTabTwo() {
        tabTwoValidatedLD.value = selectedCommandCrossRefs.isNotEmpty()
    }
}