package com.middleton.scott.cmboxing.ui.workout

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.datasource.DataRepository
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.datasource.local.model.WorkoutWithCommands
import com.middleton.scott.cmboxing.service.ServiceAudioCommand
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.playEndBellLD
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.playStartBellLD
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.serviceCommandAudioLD
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.serviceCountdownSecondsLD
import com.middleton.scott.cmboxing.service.WorkoutService.Companion.serviceWorkoutStateLD
import com.middleton.scott.cmboxing.utils.DateTimeUtils
import kotlinx.coroutines.launch
import kotlin.math.ceil

class RandomWorkoutScreenViewModel(
    private val dataRepository: DataRepository,
    val workoutId: Long
) : ViewModel() {

    private var restartOnPrevious = false
    var workoutHasBegun = false
    var workoutInProgress = false
    var firstTick = true

    var audioFileBaseDirectory = ""
    private val workoutWithCommands: WorkoutWithCommands =
        dataRepository.getLocalDataSource().getWorkoutWithCommands(workoutId)
    val workoutName = workoutWithCommands.workout?.name
    private var commands: List<Command>? = null
    private val preparationTimeSecs = workoutWithCommands.workout?.preparation_time_secs ?: 0
    private val workTimeSecs = workoutWithCommands.workout?.work_time_secs ?: 0
    private val restTimeSecs = workoutWithCommands.workout?.default_rest_time_secs ?: 0
    private val numberOfRounds = workoutWithCommands.workout?.numberOfRounds ?: 0
    private val intensity = workoutWithCommands.workout?.intensity

    var workoutHasPreparation = preparationTimeSecs > 0

    private var millisRemainingAtPause: Long = 0
    private var millisUntilNextCombination: Int = 0
    private var totalSecondsElapsed: Int = 0
    private var roundProgress: Int = -1
    var totalWorkoutSecs = getTotalWorkoutLengthSecs()

    private val _totalSecondsElapsedLD = MutableLiveData<Int>()
    val totalSecondsElapsedLD: LiveData<Int>
        get() = _totalSecondsElapsedLD

    private val _countdownSecondsLD = MutableLiveData<Int>()
    val countdownSecondsLD: LiveData<Int>
        get() = _countdownSecondsLD

    private val _roundProgressLD = MutableLiveData<Int>()
    val roundProgressLD: LiveData<Int>
        get() = _roundProgressLD

    private var currentRound = 0
    private val _currentRoundLD = MutableLiveData<Int>()
    val currentRoundLD: LiveData<Int>
        get() = _currentRoundLD

    private val _workoutStateLD = MutableLiveData<WorkoutState>()
    val workoutStateLD: LiveData<WorkoutState>
        get() = _workoutStateLD

    private val _currentCombinationLD = MutableLiveData<Command>()
    val currentCommandLD: LiveData<Command>
        get() = _currentCombinationLD

    private var countDownTimer: CountDownTimer? = null

    init {
        MainActivity.currentWorkoutId = workoutId
        commands = workoutWithCommands?.commands
        handleCombinationFrequencies()
        initWorkout()
    }

    fun getTotalRounds(): Int {
        return numberOfRounds
    }

    private fun initWorkout() {
        if (workoutHasPreparation) {
            setCurrentRound(0)
            initWorkoutState(WorkoutState.PREPARE)
        } else {
            setCurrentRound(1)
            initWorkoutState(WorkoutState.WORK)
        }
        totalSecondsElapsed = getTotalSecondsElapsed()
        _totalSecondsElapsedLD.value = totalSecondsElapsed
    }

    private fun initWorkoutState(state: WorkoutState) {
        _workoutStateLD.value = state
        serviceWorkoutStateLD.value = state

        // If is last round
        when (state) {
            WorkoutState.PREPARE -> {
                _countdownSecondsLD.value = preparationTimeSecs
                millisRemainingAtPause = preparationTimeSecs * 1000L
            }
            WorkoutState.WORK -> {
                roundProgress = 0
                _roundProgressLD.value = roundProgress
                _countdownSecondsLD.value = workTimeSecs
                millisRemainingAtPause = workTimeSecs * 1000L
            }
            WorkoutState.REST -> {
                if (currentRound >= numberOfRounds) {
                    onComplete()
                } else {
                    _countdownSecondsLD.value = restTimeSecs
                    millisRemainingAtPause = restTimeSecs * 1000L
                }
            }
        }

        if (workoutInProgress) {
            when (state) {
                WorkoutState.PREPARE -> initCountdown(preparationTimeSecs * 1000L)
                WorkoutState.WORK -> initCountdown(workTimeSecs * 1000L)
                WorkoutState.REST -> initCountdown(restTimeSecs * 1000L)
            }
        }
    }

    private fun setCurrentRound(round: Int) {
        currentRound = round
        _currentRoundLD.value = round
    }

    fun getCurrentRound(): Int {
        return currentRound
    }

    private fun initCountdown(countdownMillis: Long) {
        workoutInProgress = true
        firstTick = true

        if (workoutStateLD.value == WorkoutState.WORK) {
            playStartBellLD.value = true
        }

        millisRemainingAtPause = countdownMillis

        countDownTimer =
            object : CountDownTimer(countdownMillis, 1000) {
                override fun onFinish() {
                    onSecondElapsed()

                    millisUntilNextCombination = 0

                    when (workoutStateLD.value) {
                        WorkoutState.PREPARE -> {
                            setCurrentRound(currentRound + 1)
                            initWorkoutState(WorkoutState.WORK)
                        }

                        WorkoutState.WORK -> {
                            initWorkoutState(WorkoutState.REST)
                            playEndBellLD.value = true
                        }

                        WorkoutState.REST -> {
                            setCurrentRound(currentRound + 1)
                            initWorkoutState(WorkoutState.WORK)
                        }
                    }

                    serviceCountdownSecondsLD.value = DateTimeUtils.toMinuteSeconds(0)
                }

                override fun onTick(millisUntilFinished: Long) {
                    val countdownStr =
                        DateTimeUtils.toMinuteSeconds(ceil(millisUntilFinished.toDouble() / 1000).toInt())
                    serviceCountdownSecondsLD.value = countdownStr

                    _countdownSecondsLD.value =
                        (ceil(millisUntilFinished.toDouble() / 1000).toInt())

                    millisRemainingAtPause = millisUntilFinished

                    restartOnPrevious = countdownMillis - millisUntilFinished > 1000

                    if (!firstTick) {
                        onSecondElapsed()
                    }
                    firstTick = false

                    if (workoutStateLD.value == WorkoutState.WORK) {
                        if (millisUntilNextCombination <= 0L) {
                            initNextCommand()
                        } else {
                            millisUntilNextCombination -= 1000
                        }
                    }

                }
            }.start()
    }

    private fun initNextCommand() {
        val nextCommand: Command? = getRandomCombination()
        _currentCombinationLD.value = nextCommand
        serviceCommandAudioLD.value =
            nextCommand?.file_name?.let { ServiceAudioCommand(nextCommand.name, it, audioFileBaseDirectory) }
        val timeToCompleteCombination = nextCommand?.timeToCompleteSecs ?: 2
        millisUntilNextCombination = getTimeUntilNextCombination(timeToCompleteCombination)
    }

    private fun onSecondElapsed() {
        if (workoutStateLD.value == WorkoutState.WORK) {
            roundProgress++
            _roundProgressLD.value = roundProgress
        }

        if (workoutStateLD.value != WorkoutState.PREPARE) {
            totalSecondsElapsed++
            _totalSecondsElapsedLD.value = totalSecondsElapsed
        }

    }

    fun onNext() {
        if (workoutHasBegun) {
            countDownTimer?.cancel()
        }

        when (workoutStateLD.value) {
            WorkoutState.PREPARE -> {
                setCurrentRound(currentRound + 1)
                initWorkoutState(WorkoutState.WORK)
            }

            WorkoutState.WORK -> {
                roundProgress = getCountdownProgressBarMax(WorkoutState.WORK)
                _roundProgressLD.value = roundProgress
                initWorkoutState(WorkoutState.REST)
            }

            WorkoutState.REST -> {
                setCurrentRound(currentRound + 1)
                initWorkoutState(WorkoutState.WORK)
            }
        }

        totalSecondsElapsed = getTotalSecondsElapsed()
        _totalSecondsElapsedLD.value = totalSecondsElapsed
    }

    fun onRestart() {
        workoutHasBegun = false
        workoutInProgress = false
        initWorkout()
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
                roundProgress = 0
                _roundProgressLD.value = roundProgress
                if (restartOnPrevious) {
                    initWorkoutState(WorkoutState.WORK)
                    restartOnPrevious = false
                } else {
                    setCurrentRound(currentRound - 1)
                    if (currentRound < 1 && workoutHasPreparation) {
                        initWorkoutState(WorkoutState.PREPARE)
                    } else {
                        initWorkoutState(WorkoutState.REST)
                    }
                }
            }
            WorkoutState.REST -> {
                if (restartOnPrevious) {
                    initWorkoutState(WorkoutState.REST)
                    restartOnPrevious = false
                } else {
                    initWorkoutState(WorkoutState.WORK)
                }
            }
        }

        totalSecondsElapsed = getTotalSecondsElapsed()
        _totalSecondsElapsedLD.value = totalSecondsElapsed
    }

    fun onPlay() {
        if (workoutHasBegun) {
            initCountdown(millisRemainingAtPause)
        } else {
            var timeSecs = 0
            when (workoutStateLD.value) {
                WorkoutState.PREPARE -> timeSecs = preparationTimeSecs
                WorkoutState.WORK -> timeSecs = workTimeSecs
                WorkoutState.REST -> timeSecs = restTimeSecs
            }
            if (workoutStateLD.value != WorkoutState.PREPARE) {
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
        _workoutStateLD.value = WorkoutState.COMPLETE
        serviceWorkoutStateLD.value = WorkoutState.COMPLETE
    }

    fun getCountdownProgressBarMax(workoutState: WorkoutState): Int {
        return when (workoutState) {
            WorkoutState.PREPARE -> preparationTimeSecs
            WorkoutState.WORK -> workTimeSecs
            WorkoutState.REST -> restTimeSecs
            else -> 0
        }
    }

    private fun handleCombinationFrequencies() {
        viewModelScope.launch {
            val multipliedCombinationsList = mutableListOf<Command>()
            val selectedCombinationsCrossRefs =
                dataRepository.getLocalDataSource().getSelectedCommandCrossRefs(workoutId)

            commands?.forEach { combination ->
                val frequencyType =
                    selectedCombinationsCrossRefs.firstOrNull { combination.id == it.command_id }?.frequency
                frequencyType?.multiplicationValue?.let {
                    repeat(it) {
                        multipliedCombinationsList.add(combination)
                    }
                }
            }
            commands = multipliedCombinationsList
        }
    }

    private fun getRandomCombination(): Command? {
        var randomCommand: Command? = null
        commands?.let { randomCommand = it.shuffled().take(1)[0] }
        return randomCommand
    }

    private fun getTimeUntilNextCombination(timeToCompleteCombinationSecs: Int): Int {
        var amountToAdjustMillis = 0
        val millis = timeToCompleteCombinationSecs * 1000

        when (intensity) {
            10 -> amountToAdjustMillis = -(millis / 100) * 100
            9 -> amountToAdjustMillis = -(millis / 100) * 80
            8 -> amountToAdjustMillis = -(millis / 100) * 60
            7 -> amountToAdjustMillis = -(millis / 100) * 25
            6 -> amountToAdjustMillis = -(millis / 100) * 10
            5 -> amountToAdjustMillis = 0
            4 -> amountToAdjustMillis = (millis / 100) * 10
            3 -> amountToAdjustMillis = (millis / 100) * 20
            2 -> amountToAdjustMillis = (millis / 100) * 35
            1 -> amountToAdjustMillis = (millis / 100) * 50
        }

        return millis + amountToAdjustMillis
    }

//    private fun calculateCommandTimeBufferMillis(): Long {
//        var timeBuffer = 3000L
//        when (intensity) {
//            10 -> timeBuffer = 1.times(500).minus(500).toLong()
//            9 -> timeBuffer = 2.times(500).minus(500).toLong()
//            8 -> timeBuffer = 3.times(500).minus(500).toLong()
//            7 -> timeBuffer = 4.times(500).minus(500).toLong()
//            6 -> timeBuffer = 5.times(500).minus(500).toLong()
//            5 -> timeBuffer = 6.times(500).minus(500).toLong()
//            4 -> timeBuffer = 7.times(500).minus(500).toLong()
//            3 -> timeBuffer = 8.times(500).minus(500).toLong()
//            2 -> timeBuffer = 9.times(500).minus(500).toLong()
//            1 -> timeBuffer = 10.times(500).minus(500).toLong()
//        }
//
//        return timeBuffer
//    }

    private fun getTotalWorkoutLengthSecs(): Int {
        return (workTimeSecs * numberOfRounds) + (restTimeSecs * numberOfRounds) - restTimeSecs
    }

    private fun getTotalSecondsElapsed(): Int {
        var totalSecondsElapsed = 0
        when (workoutStateLD.value) {
            WorkoutState.PREPARE -> {
                totalSecondsElapsed = 0
            }
            WorkoutState.WORK -> {
                totalSecondsElapsed = (currentRound - 1) * (workTimeSecs + restTimeSecs)
            }

            WorkoutState.REST -> {
                totalSecondsElapsed =
                    ((currentRound) * workTimeSecs) + ((currentRound - 1) * restTimeSecs)
            }
        }
        return totalSecondsElapsed
    }

}