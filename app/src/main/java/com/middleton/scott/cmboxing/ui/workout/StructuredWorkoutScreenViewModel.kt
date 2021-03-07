package com.middleton.scott.cmboxing.ui.workout

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.StructuredCommandCrossRef
import com.middleton.scott.cmboxing.datasource.local.model.WorkoutWithCommands
import com.middleton.scott.cmboxing.service.ServiceAudioCommand
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.playEndBellLD
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.serviceCommandAudioLD
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.serviceCountdownSecondsLD
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.serviceWorkoutStateLD
import com.middleton.scott.cmboxing.utils.DateTimeUtils
import kotlin.math.ceil

class StructuredWorkoutScreenViewModel(
    dataRepository: DataRepository,
    val workoutId: Long
) : ViewModel() {

    // Workout and Commands
    private val structuredCommandCrossRefs =
        dataRepository.getLocalDataSource().getStructuredCommandCrossRefs(workoutId)
    var currentCommandCrossRef: StructuredCommandCrossRef = structuredCommandCrossRefs[0]
    var currentCommandCrossRefIndex = 0
    private val workoutWithCommands: WorkoutWithCommands =
        dataRepository.getLocalDataSource().getWorkoutWithCommands(workoutId)
    val workoutName = workoutWithCommands.workout?.name
    private var commands = workoutWithCommands.commands
    private val preparationTimeSecs = workoutWithCommands.workout?.preparation_time_secs ?: 0
    private val restTimeSecs = workoutWithCommands.workout?.default_rest_time_secs ?: 0
    val numberOfRounds = workoutWithCommands.workout?.numberOfRounds ?: 0
    var audioFileBaseDirectory = ""

    // Flags
    private var restartCommandOnPrevious = false
    var workoutHasPreparation = preparationTimeSecs > 0
    var workoutHasRest = restTimeSecs > 0
    var workoutHasBegun = false
    var workoutInProgress = false
    var firstTick = true

    private var millisRemainingAtPause: Long = 0
    val totalWorkoutLengthSecs = getTotalWorkoutSecs()

    private val _totalSecondsElapsedLD = MutableLiveData<Int>()
    val totalSecondsElapsedLD: LiveData<Int>
        get() = _totalSecondsElapsedLD

    private val _countdownSecondsLD = MutableLiveData<Int>()
    val countdownSecondsLD: LiveData<Int>
        get() = _countdownSecondsLD

    private val _roundProgressLD = MutableLiveData(0)
    val roundProgressLD: LiveData<Int>
        get() = _roundProgressLD

    private val _currentRoundLD = MutableLiveData(1)
    val currentRoundLD: LiveData<Int>
        get() = _currentRoundLD

    private val _workoutStateLD = MutableLiveData<WorkoutState>()
    val workoutStateLD: LiveData<WorkoutState>
        get() = _workoutStateLD

    private val _currentCommandLD = MutableLiveData<Command>()
    val currentCommandLD: LiveData<Command>
        get() = _currentCommandLD

    private val _playCommandAnimationLD = MutableLiveData<Boolean>()
    val playCommandAnimationLD: LiveData<Boolean>
        get() = _playCommandAnimationLD

    private var countDownTimer: CountDownTimer? = null

    init {
        MainActivity.currentWorkoutId = workoutId
        initWorkout()
    }

    private fun initWorkout() {
        if (workoutHasPreparation) {
            initWorkoutState(WorkoutState.PREPARE)
        } else {
            initWorkoutState(WorkoutState.WORK)
            _countdownSecondsLD.value = currentCommandCrossRef.time_allocated_secs
        }
        setTotalSecondsElapsed()
    }

    private fun initWorkoutState(state: WorkoutState) {
        serviceWorkoutStateLD.value = state

        when (state) {
            WorkoutState.PREPARE -> {
                _workoutStateLD.value = state
                _countdownSecondsLD.value = preparationTimeSecs
                millisRemainingAtPause = preparationTimeSecs * 1000L
                setTotalSecondsElapsed()
            }
            WorkoutState.WORK -> {
                // If another command in round
                currentCommandCrossRef = structuredCommandCrossRefs[currentCommandCrossRefIndex]
                millisRemainingAtPause = currentCommandCrossRef.time_allocated_secs * 1000L
                val nextCommand =
                    commands.firstOrNull { it.id == currentCommandCrossRef.command_id }
                _currentCommandLD.value = nextCommand
                _currentRoundLD.value = currentCommandCrossRef.round
                setCurrentRoundProgress()
                _workoutStateLD.value = state
                _countdownSecondsLD.value = currentCommandCrossRef.time_allocated_secs
                setTotalSecondsElapsed()
            }
            WorkoutState.REST -> {
                _workoutStateLD.value = state
                _countdownSecondsLD.value = restTimeSecs
                millisRemainingAtPause = restTimeSecs * 1000L
                setTotalSecondsElapsed()
            }
            WorkoutState.COMPLETE -> {
                onComplete()
                setTotalSecondsElapsed()
            }
        }

        if (workoutInProgress) {
            when (state) {
                WorkoutState.PREPARE -> {
                    initCountdown(preparationTimeSecs * 1000L)
                }
                WorkoutState.WORK -> {
                    initCountdown((currentCommandCrossRef.time_allocated_secs * 1000).toLong())
                }
                WorkoutState.REST -> {
                    initCountdown(restTimeSecs * 1000L)
                }
            }
        }
    }

    private fun initCountdown(countdownMillis: Long) {
        workoutInProgress = true
        firstTick = true

        millisRemainingAtPause = countdownMillis

        countDownTimer =
            object : CountDownTimer(countdownMillis, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    _countdownSecondsLD.value =
                        (ceil(millisUntilFinished.toDouble() / 1000).toInt())

                    val countdownStr =
                        DateTimeUtils.toMinuteSeconds(ceil(millisUntilFinished.toDouble() / 1000).toInt())
                    serviceCountdownSecondsLD.value = countdownStr

                    restartCommandOnPrevious = countdownMillis - millisUntilFinished > 1000

                    if (!firstTick) {
                        onSecondElapsed()
                    } else {
                        if (workoutStateLD.value == WorkoutState.WORK && millisRemainingAtPause == currentCommandCrossRef.time_allocated_secs * 1000L) {
                            val nextCommand =
                                commands.firstOrNull { it.id == currentCommandCrossRef.command_id }
                            serviceCommandAudioLD.value = nextCommand?.file_name?.let {
                                ServiceAudioCommand(
                                    nextCommand.name,
                                    it
                                )
                            }
                            _playCommandAnimationLD.value = true
                        }
                    }

                    millisRemainingAtPause = millisUntilFinished

                    firstTick = false
                }

                override fun onFinish() {
                    onSecondElapsed()

                    when (workoutStateLD.value) {
                        WorkoutState.PREPARE -> {
                            initWorkoutState(WorkoutState.WORK)
                        }

                        WorkoutState.WORK -> {
                            currentCommandCrossRefIndex++
                            initCommand()
                        }

                        WorkoutState.REST -> {
                            _currentRoundLD.value = currentRoundLD.value!! + 1
                            initWorkoutState(WorkoutState.WORK)
                        }
                    }

                    serviceCountdownSecondsLD.value = DateTimeUtils.toMinuteSeconds(0)
                }
            }.start()
    }

    private fun initCommand() {
        when {
            structuredCommandCrossRefs.size == currentCommandCrossRefIndex -> {
                // If workout complete
                // Set the round progress to max
                _roundProgressLD.value = getLengthOfRoundSecs(currentRoundLD.value!!)

                // initiate complete
                initWorkoutState(WorkoutState.COMPLETE)
            }
            _currentRoundLD.value == structuredCommandCrossRefs[currentCommandCrossRefIndex].round - 1 && workoutHasRest -> {
                // If round complete and workout has rests between rounds
                // Set the round progress to max
                _roundProgressLD.value = getLengthOfRoundSecs(currentRoundLD.value!!)

                // initiate rest
                if (workoutInProgress) {
                    playEndBellLD.value = true
                }
                initWorkoutState(WorkoutState.REST)
            }
            _currentRoundLD.value == structuredCommandCrossRefs[currentCommandCrossRefIndex].round - 1 && !workoutHasRest -> {
                // If round complete and workout does not have rests between rounds
                // Set the round progress to max
                _roundProgressLD.value = getLengthOfRoundSecs(currentRoundLD.value!!)

                initWorkoutState(WorkoutState.WORK)
            }
            else -> {
                // If another command in round
                initWorkoutState(WorkoutState.WORK)
            }
        }
    }

    private fun onSecondElapsed() {
        if (workoutStateLD.value == WorkoutState.WORK) {
            val progress = _roundProgressLD.value!!
            _roundProgressLD.value = progress.plus(1)
        }

        if (workoutStateLD.value != WorkoutState.PREPARE) {
            val totalSecondsElapsed = _totalSecondsElapsedLD.value!!
            _totalSecondsElapsedLD.value = totalSecondsElapsed + 1
        }
    }

    fun onRestart() {
        workoutHasBegun = false
        workoutInProgress = false
        currentCommandCrossRefIndex = 0
        initWorkout()
    }

    fun onNext() {
        if (workoutHasBegun) {
            countDownTimer?.cancel()
        }

        when (workoutStateLD.value) {
            WorkoutState.PREPARE -> {
                initWorkoutState(WorkoutState.WORK)
            }

            WorkoutState.WORK -> {
                currentCommandCrossRefIndex++
                initCommand()
            }

            WorkoutState.REST -> {
                _currentRoundLD.value = currentRoundLD.value!! + 1
                initWorkoutState(WorkoutState.WORK)
            }
        }
    }

    fun onPrevious() {
        millisRemainingAtPause = preparationTimeSecs * 1000L

        if (workoutHasBegun) {
            countDownTimer?.cancel()
        }

        when (workoutStateLD.value) {
            WorkoutState.PREPARE -> {
                initWorkoutState(WorkoutState.PREPARE)
            }
            WorkoutState.WORK -> {
                if (restartCommandOnPrevious) {
                    // If restart command
                    initCommand()
                    restartCommandOnPrevious = false
                } else {
                    when {
                        currentCommandCrossRefIndex == 0 && workoutHasPreparation -> {
                            // If first command in workout
                            initWorkoutState(WorkoutState.PREPARE)
                        }
                        currentCommandCrossRefIndex != 0 && currentCommandCrossRef.position_index == 0 -> {
                            // If first command in round
                            _currentRoundLD.value = _currentRoundLD.value!! - 1
                            initWorkoutState(WorkoutState.REST)
                        }
                        else -> {
                            // If go to previous command
                            if (currentCommandCrossRefIndex != 0) {
                                currentCommandCrossRefIndex--
                            }
                            initCommand()
                        }
                    }
                }
            }
            WorkoutState.REST -> {
                if (restartCommandOnPrevious) {
                    initWorkoutState(WorkoutState.REST)
                    restartCommandOnPrevious = false
                } else {
                    currentCommandCrossRefIndex--
                    initCommand()
                }
            }
        }
    }

    fun onPlay() {
            initCountdown(millisRemainingAtPause)
            workoutHasBegun = true
        workoutInProgress = true
    }

    fun onPause() {
        workoutInProgress = false
        countDownTimer?.cancel()
    }

    private fun onComplete() {
        if (workoutInProgress) {
            countDownTimer?.cancel()
        }

        workoutInProgress = false
        _countdownSecondsLD.value = 0
        _workoutStateLD.value = WorkoutState.COMPLETE
        serviceWorkoutStateLD.value = WorkoutState.COMPLETE
    }

    fun getCountdownProgressBarMax(workoutState: WorkoutState): Int {
        return when (workoutState) {
            WorkoutState.PREPARE -> preparationTimeSecs
            WorkoutState.WORK -> currentCommandCrossRef.time_allocated_secs
            WorkoutState.REST -> restTimeSecs
            else -> 0
        }
    }

    fun getLengthOfRoundSecs(round: Int): Int {
        val roundCrossRefs = structuredCommandCrossRefs.filter { it.round == round }

        var roundSecs = 0

        roundCrossRefs.forEach {
            roundSecs += it.time_allocated_secs
        }

        return roundSecs
    }


    /**
     * This function sets the progress of the round bar by taking the getting the current rounds
     * command cross refs and multiplying all the cross ref time_allocated values, prior to the
     * current cross ref
     */
    private fun setCurrentRoundProgress() {
        val roundCrossRefs =
            structuredCommandCrossRefs.filter { it.round == currentRoundLD.value!! }

        var roundProgress = 0

        roundCrossRefs.forEach lit@{
            if (it.position_index >= structuredCommandCrossRefs[currentCommandCrossRefIndex].position_index) return@lit

            roundProgress += it.time_allocated_secs
        }

        _roundProgressLD.value = roundProgress
    }

    private fun setTotalSecondsElapsed() {
        var totalSecondsElapsed = 0
        when (_workoutStateLD.value) {
            WorkoutState.PREPARE -> {
                totalSecondsElapsed = 0
            }
            else -> {
                var indexToGetTime = currentCommandCrossRefIndex - 1
                var totalCommandTimeSecs = 0
                repeat(currentCommandCrossRefIndex) {
                    totalCommandTimeSecs +=
                        structuredCommandCrossRefs[indexToGetTime].time_allocated_secs
                    indexToGetTime--
                }

                val currentRound = _currentRoundLD.value!!
                totalSecondsElapsed = totalCommandTimeSecs + ((currentRound - 1) * restTimeSecs)
            }
        }
        _totalSecondsElapsedLD.value = totalSecondsElapsed
    }

    private fun getTotalWorkoutSecs(): Int {
        var totalTimeSecs = 0
        if (structuredCommandCrossRefs.isNotEmpty()) {
            structuredCommandCrossRefs.forEach {
                totalTimeSecs += it.time_allocated_secs
            }
        }

        val restBetweenRounds = restTimeSecs
        val totalRestTime = restBetweenRounds * (numberOfRounds - 1)

        return totalTimeSecs + totalRestTime
    }

    fun getNumberOfCommandsInRound(): Int {
        val roundCrossRefs =
            structuredCommandCrossRefs.filter { it.round == currentCommandCrossRef.round }
        return roundCrossRefs.size
    }
}