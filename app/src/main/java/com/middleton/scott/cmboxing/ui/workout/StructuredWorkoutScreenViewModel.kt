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
    var currentCommandCrossRefIndex = -1
    private val workoutWithCommands: WorkoutWithCommands =
        dataRepository.getLocalDataSource().getWorkoutWithCommands(workoutId)
    val workoutName = workoutWithCommands.workout?.name
    private var commands = workoutWithCommands.commands
    private val preparationTimeSecs = workoutWithCommands.workout?.preparation_time_secs ?: 0
    private val restTimeSecs = workoutWithCommands.workout?.default_rest_time_secs ?: 0
    private val numberOfRounds = workoutWithCommands.workout?.numberOfRounds ?: 0
    var audioFileBaseDirectory = ""

    // Flags
    private var restartOnPrevious = false
    var workoutHasPreparation = false
    var workoutHasBegun = false
    var workoutInProgress = false
    var firstTick = true


    private var millisRemainingAtPause: Long = 0
    private var totalSecondsElapsed: Int = 0

//    var totalWorkoutSecs = getTotalWorkoutLengthSecs()

//    private val _totalSecondsElapsedLD = MutableLiveData<Int>()
//    val totalSecondsElapsedLD: LiveData<Int>
//        get() = _totalSecondsElapsedLD

    private val _countdownSecondsLD = MutableLiveData<Int>()
    val countdownSecondsLD: LiveData<Int>
        get() = _countdownSecondsLD

//    private val _roundProgressLD = MutableLiveData<Int>()
//    val roundProgressLD: LiveData<Int>
//        get() = _roundProgressLD

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
        workoutHasPreparation = preparationTimeSecs > 0
        initWorkout()
    }

    private fun initWorkout() {
        if (workoutHasPreparation) {
            initWorkoutState(RandomWorkoutState.PREPARE)
        } else {
            initWorkoutState(RandomWorkoutState.WORK)
        }
        totalSecondsElapsed = getTotalSecondsElapsed()
    }

    private fun initWorkoutState(state: RandomWorkoutState) {
        serviceWorkoutStateLD.value = state

        // If is last round
        when (state) {
            RandomWorkoutState.PREPARE -> {
                _countdownSecondsLD.value = preparationTimeSecs
//                millisRemainingAtPause = preparationTimeSecs * 1000L
            }
            RandomWorkoutState.WORK -> {
//                _roundProgressLD.value = 0
                _countdownSecondsLD.value = currentCommandCrossRef.time_allocated_secs
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
                RandomWorkoutState.WORK -> initNextCommand()
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

                    restartOnPrevious = countdownMillis - millisUntilFinished > 1000

                    if (!firstTick) {
//                        onSecondElapsed()
                    }
                    firstTick = false
                }

                override fun onFinish() {
//                    onSecondElapsed()

                    when (workoutStateLD.value) {
                        RandomWorkoutState.PREPARE -> {
                            initWorkoutState(RandomWorkoutState.WORK)
                        }

                        RandomWorkoutState.WORK -> {
                            initNextCommand()
                        }

                        RandomWorkoutState.REST -> {
                            var currentRound = _currentRoundLD.value!!
                            _currentRoundLD.value = currentRound + 1
                            initWorkoutState(RandomWorkoutState.WORK)
                        }
                    }

                    serviceCountdownSecondsLD.value = DateTimeUtils.toMinuteSeconds(0)
                }
            }.start()
    }

    private fun initNextCommand() {
        when {
            structuredCommandCrossRefs.size == currentCommandCrossRefIndex + 1 -> {
                // If workout complete
                initWorkoutState(RandomWorkoutState.COMPLETE)
            }
            _currentRoundLD.value == structuredCommandCrossRefs[currentCommandCrossRefIndex + 1].round - 1 -> {
                // If round complete
                initWorkoutState(RandomWorkoutState.REST)
            }
            else -> {
                // If another command in round
                currentCommandCrossRefIndex++
                currentCommandCrossRef = structuredCommandCrossRefs[currentCommandCrossRefIndex]
                val nextCommand =
                    commands.firstOrNull { it.id == currentCommandCrossRef.command_id }
                _currentCommandLD.value = nextCommand
                serviceCommandAudioLD.value =
                    nextCommand?.file_name?.let { ServiceAudioCommand(it, audioFileBaseDirectory) }
                _workoutStateLD.value = RandomWorkoutState.WORK
                initCountdown((currentCommandCrossRef.time_allocated_secs * 1000).toLong())
            }
        }
    }

//    private fun onSecondElapsed() {
//        if (workoutStateLD.value == RandomWorkoutState.WORK) {
//            val progress = _roundProgressLD.value
//            _roundProgressLD.value = progress?.plus(1)
//        }
//
//        if (workoutStateLD.value != RandomWorkoutState.PREPARE) {
//            totalSecondsElapsed++
//            _totalSecondsElapsedLD.value = totalSecondsElapsed
//        }

//    }

//    fun onNext() {
//        if (workoutHasBegun) {
//            countDownTimer?.cancel()
//        }
//
//        when (randomWorkoutStateLD.value) {
//            RandomWorkoutState.PREPARE -> {
//                setCurrentRound(currentRound + 1)
//                initWorkoutState(RandomWorkoutState.WORK)
//            }
//
//            RandomWorkoutState.WORK -> {
//                roundProgress = getCountdownProgressBarMax(RandomWorkoutState.WORK)
//                _roundProgressLD.value = roundProgress
//                initWorkoutState(RandomWorkoutState.REST)
//            }
//
//            RandomWorkoutState.REST -> {
//                setCurrentRound(currentRound + 1)
//                initWorkoutState(RandomWorkoutState.WORK)
//            }
//        }
//
//        totalSecondsElapsed = getTotalSecondsElapsed()
//        _totalSecondsElapsedLD.value = totalSecondsElapsed
//    }

//    fun onRestart() {
//        workoutHasBegun = false
//        workoutInProgress = false
//        initWorkout()
//    }

//    fun onPrevious() {
//        millisRemainingAtPause = preparationTimeSecs * 1000L
//
//        if (workoutHasBegun) {
//            countDownTimer?.cancel()
//        }
//
//        when (randomWorkoutStateLD.value) {
//            RandomWorkoutState.PREPARE -> {
//                initWorkoutState(RandomWorkoutState.PREPARE)
//            }
//            RandomWorkoutState.WORK -> {
//                roundProgress = 0
//                _roundProgressLD.value = roundProgress
//                if (restartOnPrevious) {
//                    initWorkoutState(RandomWorkoutState.WORK)
//                    restartOnPrevious = false
//                } else {
//                    setCurrentRound(currentRound - 1)
//                    if (currentRound < 1) {
//                        initWorkoutState(RandomWorkoutState.PREPARE)
//                    } else {
//                        initWorkoutState(RandomWorkoutState.REST)
//                    }
//                }
//            }
//            RandomWorkoutState.REST -> {
//                if (restartOnPrevious) {
//                    initWorkoutState(RandomWorkoutState.REST)
//                    restartOnPrevious = false
//                } else {
//                    initWorkoutState(RandomWorkoutState.WORK)
//                }
//            }
//        }
//
//        totalSecondsElapsed = getTotalSecondsElapsed()
//        _totalSecondsElapsedLD.value = totalSecondsElapsed
//    }

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

    private fun getTotalWorkoutLengthSecs(): Int {
//        return (workTimeSecs * numberOfRounds) + (restTimeSecs * numberOfRounds) - restTimeSecs
        return 1000
    }


    private fun getTotalSecondsElapsed(): Int {
//        var totalSecondsElapsed = 0
//        when (randomWorkoutStateLD.value) {
//            RandomWorkoutState.PREPARE -> {
//                totalSecondsElapsed = 0
//            }
//            RandomWorkoutState.WORK -> {
//                totalSecondsElapsed = (currentRound - 1) * (workTimeSecs + restTimeSecs)
//            }
//
//            RandomWorkoutState.REST -> {
//                totalSecondsElapsed =
//                    ((currentRound) * workTimeSecs) + ((currentRound - 1) * restTimeSecs)
//            }
//        }
//        return totalSecondsElapsed
//        _totalSecondsElapsedLD.value = totalSecondsElapsed
        return 1000
    }

//    fun getTotalRounds(): Int {
//        return numberOfRounds
//    }

//    fun getCurrentRound(): Int {
//        return currentCommandCrossRef.round
//    }

}