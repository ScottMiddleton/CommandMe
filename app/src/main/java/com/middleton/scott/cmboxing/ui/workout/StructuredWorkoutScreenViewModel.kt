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
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.playStartBellLD
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

    private val _workoutStateLD = MutableLiveData<RandomWorkoutState>()
    val workoutStateLD: LiveData<RandomWorkoutState>
        get() = _workoutStateLD

    private val _currentCommandLD = MutableLiveData<Command>()
    val currentCommandLD: LiveData<Command>
        get() = _currentCommandLD

    private var countDownTimer: CountDownTimer? = null

    init {
        MainActivity.currentWorkoutId = workoutId
        initWorkout()
    }

    private fun initWorkout() {
        if (workoutHasPreparation) {
            initWorkoutState(RandomWorkoutState.PREPARE)
        } else {
            initWorkoutState(RandomWorkoutState.WORK)
        }
        setTotalSecondsElapsed()
    }

    private fun initWorkoutState(state: RandomWorkoutState) {
        serviceWorkoutStateLD.value = state

        when (state) {
            RandomWorkoutState.PREPARE -> {
                _countdownSecondsLD.value = preparationTimeSecs
//                millisRemainingAtPause = preparationTimeSecs * 1000L
            }
            RandomWorkoutState.WORK -> {
//                millisRemainingAtPause = currentCommandCrossRef.time_allocated_secs * 1000L
            }
            RandomWorkoutState.REST -> {
                _countdownSecondsLD.value = restTimeSecs
//                millisRemainingAtPause = restTimeSecs * 1000L
            }
            RandomWorkoutState.COMPLETE -> {
                onComplete()
            }
        }

        if (workoutInProgress) {
            when (state) {
                RandomWorkoutState.PREPARE -> {
                    _workoutStateLD.value = state
                    initCountdown(preparationTimeSecs * 1000L)
                }
                RandomWorkoutState.WORK -> {
                    initCommand(currentCommandCrossRefIndex)
                }
                RandomWorkoutState.REST -> {
                    _workoutStateLD.value = state
                    initCountdown(restTimeSecs * 1000L)
                }
            }
        } else {
            when (state) {
                RandomWorkoutState.PREPARE -> {
                    _workoutStateLD.value = state
                }
                RandomWorkoutState.REST -> {
                    _workoutStateLD.value = state
                }
            }
        }

    }

    private fun initCountdown(countdownMillis: Long) {
        workoutInProgress = true
        firstTick = true

        if (workoutStateLD.value == RandomWorkoutState.REST) {
            playEndBellLD.value = true
        }

        millisRemainingAtPause = countdownMillis

        countDownTimer =
            object : CountDownTimer(countdownMillis, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    _countdownSecondsLD.value =
                        (ceil(millisUntilFinished.toDouble() / 1000).toInt())

                    val countdownStr =
                        DateTimeUtils.toMinuteSeconds(ceil(millisUntilFinished.toDouble() / 1000).toInt())
                    serviceCountdownSecondsLD.value = countdownStr
                    millisRemainingAtPause = millisUntilFinished

                    restartCommandOnPrevious = countdownMillis - millisUntilFinished > 1000

                    if (!firstTick) {
                        onSecondElapsed()
                    }

                    firstTick = false
                }

                override fun onFinish() {
                    onSecondElapsed()

                    when (workoutStateLD.value) {
                        RandomWorkoutState.PREPARE -> {
                            initWorkoutState(RandomWorkoutState.WORK)
                        }

                        RandomWorkoutState.WORK -> {
                            currentCommandCrossRefIndex++
                            initCommand(currentCommandCrossRefIndex)
                        }

                        RandomWorkoutState.REST -> {
                            _currentRoundLD.value = currentRoundLD.value!! + 1
                            initWorkoutState(RandomWorkoutState.WORK)
                        }
                    }

                    serviceCountdownSecondsLD.value = DateTimeUtils.toMinuteSeconds(0)
                }
            }.start()
    }

    private fun initCommand(commandIndex: Int) {
        when {
            structuredCommandCrossRefs.size == commandIndex -> {
                // If workout complete
                // Set the round progress to max
                _roundProgressLD.value = getLengthOfRoundSecs(currentRoundLD.value!!)
                initWorkoutState(RandomWorkoutState.COMPLETE)
            }
            _currentRoundLD.value == structuredCommandCrossRefs[commandIndex].round - 1 && workoutHasRest -> {
                // If round complete
                // Set the round progress to max
                _roundProgressLD.value = getLengthOfRoundSecs(currentRoundLD.value!!)

                initWorkoutState(RandomWorkoutState.REST)
            }
            else -> {
                // If another command in round
                currentCommandCrossRef = structuredCommandCrossRefs[commandIndex]
                val nextCommand =
                    commands.firstOrNull { it.id == currentCommandCrossRef.command_id }
                _currentCommandLD.value = nextCommand
                serviceCommandAudioLD.value =
                    nextCommand?.file_name?.let { ServiceAudioCommand(it, audioFileBaseDirectory) }
                _workoutStateLD.value = RandomWorkoutState.WORK
                _currentRoundLD.value = currentCommandCrossRef.round
                setCurrentRoundProgress()
                initCountdown((currentCommandCrossRef.time_allocated_secs * 1000).toLong())
            }
        }
    }

    private fun onSecondElapsed() {
        if (workoutStateLD.value == RandomWorkoutState.WORK) {
            val progress = _roundProgressLD.value!!
            _roundProgressLD.value = progress.plus(1)
        }

        if (workoutStateLD.value != RandomWorkoutState.PREPARE) {
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
            RandomWorkoutState.PREPARE -> {
                initWorkoutState(RandomWorkoutState.WORK)
            }

            RandomWorkoutState.WORK -> {
                currentCommandCrossRefIndex++
                initCommand(currentCommandCrossRefIndex)
            }

            RandomWorkoutState.REST -> {
                _currentRoundLD.value = currentRoundLD.value!! + 1
                initWorkoutState(RandomWorkoutState.WORK)
            }
        }
    }

    fun onPrevious() {
        millisRemainingAtPause = preparationTimeSecs * 1000L

        if (workoutHasBegun) {
            countDownTimer?.cancel()
        }

        when (workoutStateLD.value) {
            RandomWorkoutState.PREPARE -> {
                initWorkoutState(RandomWorkoutState.PREPARE)
            }
            RandomWorkoutState.WORK -> {
                if (restartCommandOnPrevious) {
                    // If restart command
                    initCommand(currentCommandCrossRefIndex)
                    restartCommandOnPrevious = false
                } else {
                    when {
                        currentCommandCrossRefIndex == 0 -> {
                            // If first command in workout
                            initWorkoutState(RandomWorkoutState.PREPARE)
                        }
                        currentCommandCrossRefIndex != 0 && currentCommandCrossRef.position_index == 0 -> {
                            // If first command in round
                            _currentRoundLD.value = _currentRoundLD.value!! - 1
                            initWorkoutState(RandomWorkoutState.REST)
                        }
                        else -> {
                            // If go to previous command
                            currentCommandCrossRefIndex--
                            initCommand(currentCommandCrossRefIndex)
                        }
                    }
                }
            }
            RandomWorkoutState.REST -> {
                if (restartCommandOnPrevious) {
                    initWorkoutState(RandomWorkoutState.REST)
                    restartCommandOnPrevious = false
                } else {
                    currentCommandCrossRefIndex--
                    initCommand(currentCommandCrossRefIndex)
                }
            }
        }
        setTotalSecondsElapsed()
    }

    fun onPlay() {
        if (workoutHasBegun) {
            initCountdown(millisRemainingAtPause)
        } else {
            var timeSecs = 0
            when (workoutStateLD.value) {
                RandomWorkoutState.PREPARE -> timeSecs = preparationTimeSecs
                RandomWorkoutState.WORK -> timeSecs = currentCommandCrossRef.time_allocated_secs
                RandomWorkoutState.REST -> timeSecs = restTimeSecs
            }
            if (workoutStateLD.value != RandomWorkoutState.PREPARE) {
                playStartBellLD.value = true
            }
            initCountdown(timeSecs * 1000L)
            workoutHasBegun = true
        }
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
        _workoutStateLD.value = RandomWorkoutState.COMPLETE
        serviceWorkoutStateLD.value = RandomWorkoutState.COMPLETE
    }

    fun getCountdownProgressBarMax(randomWorkoutState: RandomWorkoutState): Int {
        return when (randomWorkoutState) {
            RandomWorkoutState.PREPARE -> preparationTimeSecs
            RandomWorkoutState.WORK -> currentCommandCrossRef.time_allocated_secs
            RandomWorkoutState.REST -> restTimeSecs
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
            RandomWorkoutState.PREPARE -> {
                totalSecondsElapsed = 0
            }
            RandomWorkoutState.WORK, RandomWorkoutState.REST -> {
                var indexToGetTime = currentCommandCrossRefIndex - 1
                var totalCommandTimeSecs = 0
                repeat(currentCommandCrossRefIndex) {
                    totalCommandTimeSecs =
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