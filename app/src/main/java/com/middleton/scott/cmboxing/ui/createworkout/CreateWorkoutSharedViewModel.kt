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
    var isEditMode = false
    var isCancelling = false
    var subscribe = true
    var userHasAttemptedToProceedOne = false
    var userHasAttemptedToProceedTwo = false

    var selectedCommands = ArrayList<Command>()

    var workout = Workout()
    var selectedCommandCrossRefs = ArrayList<SelectedCommandCrossRef>()
    var structuredCommandCrossRefs = ArrayList<StructuredCommandCrossRef>()

    var savedWorkout = Workout()
    var savedSelectedCommandCrossRefs = ArrayList<SelectedCommandCrossRef>()
    var savedStructuredCommandCrossRefs = ArrayList<StructuredCommandCrossRef>()

    private val commandsFlow = dataRepository.getLocalDataSource().getCommandsFlow()

    lateinit var selectedCommandCrossRefsFlow: Flow<List<SelectedCommandCrossRef>>
    lateinit var workoutLD: LiveData<Workout?>
    lateinit var selectedCommandsLD: LiveData<List<Command>>
    lateinit var structuredCommandCrossRefsLD: LiveData<List<StructuredCommandCrossRef>>

    val workoutTypeLD = MutableLiveData<WorkoutType>()
    val preparationTimeLD = MutableLiveData<Int>()
    val numberOfRoundsLD = MutableLiveData<Int>()
    val workTimeSecsLD = MutableLiveData<Int>()
    val defaultRestTimeSecsLD = MutableLiveData<Int>(60)
    val intensityLD = MutableLiveData<Int>()
    val dbUpdateLD = MutableLiveData<Boolean>()
    val showCancellationDialogLD = MutableLiveData<Boolean>()
    val tabOneValidatedLD = MutableLiveData(false)
    val tabTwoValidatedLD = MutableLiveData(false)
    val requiredSummaryFieldLD = MutableLiveData<Boolean>()
    val totalLengthSecsLD = MutableLiveData(0)

    val subscribeLD = MutableLiveData(false)
    var setRoundsAdapter = true

    init {
        // If a new workout
        if (workoutId == -1L) {
            viewModelScope.launch {
                // Insert a new workout
                val newWorkoutId = dataRepository.getLocalDataSource().upsertWorkout(workout)

                dataRepository.getLocalDataSource().getWorkoutById(newWorkoutId)
                    ?.let { savedWorkout = it }

                workoutLD =
                    dataRepository.getLocalDataSource().getWorkoutByIdFlow(newWorkoutId).map {
                        it?.let {
                            workout = it
                            workoutTypeLD.value = it.workout_type
                            numberOfRoundsLD.value = it.numberOfRounds
                        }
                        it
                    }.asLiveData()

                selectedCommandCrossRefsFlow = dataRepository.getLocalDataSource()
                    .getSelectedCommandCrossRefsFlow(newWorkoutId)

                selectedCommandsLD =
                    commandsFlow.combine(selectedCommandCrossRefsFlow) { commands, it ->
                        selectedCommands.clear()
                        selectedCommandCrossRefs = it as ArrayList<SelectedCommandCrossRef>

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
                        .getStructuredCommandCrossRefsFlow(newWorkoutId).map {
                            structuredCommandCrossRefs = it as ArrayList<StructuredCommandCrossRef>
                            it
                        }.asLiveData()

                workoutId = newWorkoutId
                subscribeLD.value = true
            }
        } else {
            // If an existing workout
            isEditMode = true
            selectedCommandCrossRefsFlow = dataRepository.getLocalDataSource()
                .getSelectedCommandCrossRefsFlow(workoutId)

            workoutLD = dataRepository.getLocalDataSource().getWorkoutByIdFlow(workoutId).map {
                it?.let {
                    workout = it
                    workoutTypeLD.value = it.workout_type
                    numberOfRoundsLD.value = it.numberOfRounds
                }
                it
            }.asLiveData()

            dataRepository.getLocalDataSource().getWorkoutById(workoutId)
                ?.let { savedWorkout = it }

            savedSelectedCommandCrossRefs =
                dataRepository.getLocalDataSource()
                    .getSelectedCommandCrossRefs(workoutId) as ArrayList<SelectedCommandCrossRef>

            savedStructuredCommandCrossRefs =
                dataRepository.getLocalDataSource()
                    .getStructuredCommandCrossRefs(workoutId) as ArrayList<StructuredCommandCrossRef>

            structuredCommandCrossRefs = savedStructuredCommandCrossRefs

            selectedCommandsLD =
                commandsFlow.combine(selectedCommandCrossRefsFlow) { commands, it ->
                    selectedCommands.clear()
                    selectedCommandCrossRefs = it as ArrayList<SelectedCommandCrossRef>

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
                    .getStructuredCommandCrossRefsFlow(workoutId).map {
                        structuredCommandCrossRefs = it as ArrayList<StructuredCommandCrossRef>
                        it
                    }.asLiveData()
            subscribeLD.value = true
        }
    }

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

    fun addSelectedCommand(
        selectedCommandCrossRef: SelectedCommandCrossRef
    ) {
        selectedCommandCrossRef.workout_id = workoutId

        viewModelScope.launch {
            dataRepository.getLocalDataSource().upsertSelectedCommandCrossRef(
                selectedCommandCrossRef
            )
        }
    }

    fun removeSelectedCommand(selectedCommandCrossRef: SelectedCommandCrossRef) {
        selectedCommandCrossRef.workout_id = workoutId

        viewModelScope.launch {
            dataRepository.getLocalDataSource()
                .deleteSelectedCommandCrossRef(selectedCommandCrossRef)
            dataRepository.getLocalDataSource()
                .deleteStructuredCommandCrossRefForWorkout(selectedCommandCrossRef.command_id, workoutId)
        }
    }

    fun deleteStructuredCommandCrossRef(structuredCommandCrossRef: StructuredCommandCrossRef) {
        viewModelScope.launch {
            dataRepository.getLocalDataSource()
                .deleteStructuredCommandCrossRef(structuredCommandCrossRef)
        }
    }

    fun upsertStructuredCommandCrossRefs(structuredCommandCrossRefs: List<StructuredCommandCrossRef>) {
        viewModelScope.launch {
            structuredCommandCrossRefs.forEachIndexed { index, structuredCommandCrossRef ->
                structuredCommandCrossRef.workout_id = workoutId
                structuredCommandCrossRef.position_index = index

            }
            dataRepository.getLocalDataSource()
                .upsertStructuredCommandCrossRefs(structuredCommandCrossRefs)
        }
    }

    fun pasteStructuredCommandCrossRefs(copiedRound: Int, roundsToPaste: List<Int>) {
        val copiedRefs = mutableListOf<StructuredCommandCrossRef>()
        val crossRefs = structuredCommandCrossRefs

        // Get the cross refs for the copied round
        crossRefs.forEach {
            if (it.round == copiedRound) {
                copiedRefs.add(it)
            }
        }

        // Delete the cross refs for the rounds to paste before pasting
        roundsToPaste.forEach { round ->
            crossRefs.removeAll { it.round == round }
        }

        // Copy the refs by changing the round value for each
        roundsToPaste.forEach { round ->
            if (copiedRefs.isNotEmpty()) {
                copiedRefs.forEach {
                    crossRefs.add(
                        StructuredCommandCrossRef(
                            workoutId,
                            it.command_id,
                            round,
                            it.time_allocated_secs,
                            it.position_index
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            setRoundsAdapter = false
            dataRepository.getLocalDataSource().deleteStructuredCommandCrossRefs(workoutId)
            setRoundsAdapter = true
            dataRepository.getLocalDataSource().upsertStructuredCommandCrossRefs(crossRefs)
        }
    }

    fun setWorkoutType(type: WorkoutType) {
        workout.workout_type = type
        workoutTypeLD.value = type
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

    fun setDefaultRestTime(restTimeSecs: Int) {
        if (restTimeSecs >= 0) {
            workout.default_rest_time_secs = restTimeSecs
            defaultRestTimeSecsLD.value = restTimeSecs
        }
    }

    fun setIntensity(intensity: Int) {
        workout.intensity = intensity
        intensityLD.value = intensity
    }

    fun cancelChanges() {
        viewModelScope.launch {
            isCancelling = true
            dataRepository.getLocalDataSource().deleteWorkoutAndCrossRefs(workout)

            if (isEditMode) {
                dataRepository.getLocalDataSource().upsertWorkout(savedWorkout)
                dataRepository.getLocalDataSource()
                    .upsertWorkoutCommandsList(savedSelectedCommandCrossRefs)
                dataRepository.getLocalDataSource()
                    .upsertStructuredCommandCrossRefs(savedStructuredCommandCrossRefs)
            }
            dbUpdateLD.value = true
        }
    }

    fun onCancel() {
        showCancellationDialogLD.value =
            !(savedWorkout == workout && savedSelectedCommandCrossRefs == selectedCommandCrossRefs && savedStructuredCommandCrossRefs == structuredCommandCrossRefs)
    }

    fun validateTabOne() {
        tabOneValidatedLD.value = workout.name.isNotBlank()
    }

    fun validateTabTwo() {
        tabTwoValidatedLD.value = selectedCommandCrossRefs.isNotEmpty()
    }

    fun setTotalLength() {
        var totalTimeSecs = 0
        if (structuredCommandCrossRefs.isNotEmpty()) {
            structuredCommandCrossRefs.forEach {
                totalTimeSecs += it.time_allocated_secs
            }
        }

        val restBetweenRounds = defaultRestTimeSecsLD.value ?: 0
        val numberOfRounds = numberOfRoundsLD.value ?: 0
        val totalRestTime = restBetweenRounds * (numberOfRounds - 1)

        totalLengthSecsLD.value = totalTimeSecs + totalRestTime
    }

    fun validateRoundsNotEmpty(): Boolean {
        var round = 1
        var valid = true
        repeat(workout.numberOfRounds) {
            val filtered = structuredCommandCrossRefs.firstOrNull { it.round == round }
            if (filtered == null) {
                valid = false
            }
            round++
        }

        if(workoutTypeLD.value == WorkoutType.RANDOM){
            valid = true
        }

        return valid
    }
}