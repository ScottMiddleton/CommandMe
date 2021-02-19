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

        // If is last round
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
                            initNextCommand()
                        }

                        RandomWorkoutState.REST -> {
                            _currentRoundLD.value = currentRoundLD.value!! + 1
                            _roundProgressLD.value = 0
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
            _currentRoundLD.value == structuredCommandCrossRefs[currentCommandCrossRefIndex + 1].round - 1 &&  workoutHasRest -> {
                // If round complete
                initWorkoutState(RandomWorkoutState.REST)
            }
            else -> {
                // If another command in round
                currentCommandCrossRefIndex++
                currentCommandCrossRef = structuredCommandCrossRefs[currentCommandCrossRefIndex]
                val nextCommand = commands.firstOrNull { it.id == currentCommandCrossRef.command_id }
                _currentCommandLD.value = nextCommand
                serviceCommandAudioLD.value = nextCommand?.file_name?.let { ServiceAudioCommand(it, audioFileBaseDirectory) }
                _workoutStateLD.value = RandomWorkoutState.WORK
                _currentRoundLD.value = currentCommandCrossRef.round
                if(currentCommandCrossRef.position_index == 0){
                    _roundProgressLD.value = 0
                }
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

    fun onNext() {
        if (workoutHasBegun) {
            countDownTimer?.cancel()
        }

        when (workoutStateLD.value) {
            RandomWorkoutState.PREPARE -> {
                initWorkoutState(RandomWorkoutState.WORK)
            }

            RandomWorkoutState.WORK -> {
                setCurrentRoundProgress()
                initNextCommand()
            }

            RandomWorkoutState.REST -> {
                _currentRoundLD.value = currentRoundLD.value!! + 1
                initWorkoutState(RandomWorkoutState.WORK)
            }
        }
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
            RandomWorkoutState.PREPARE -> {
                initWorkoutState(RandomWorkoutState.PREPARE)
            }
            RandomWorkoutState.WORK -> {
                _roundProgressLD.value = 0
                currentCommandCrossRefIndex --
                if (restartCommandOnPrevious) {
                    initNextCommand()
                    restartCommandOnPrevious = false
                } else {
//                    _currentRoundLD.value = currentRoundLD.value!! - 1
                    if (currentRoundLD.value!! == 1 && currentCommandCrossRef.position_index == 0) {
                        initWorkoutState(RandomWorkoutState.PREPARE)
                    } else if (currentCommandCrossRef.position_index == 0){
                        initWorkoutState(RandomWorkoutState.REST)
                    } else {
                        currentCommandCrossRefIndex --
                        setCurrentRoundProgress()
                        initNextCommand()
                    }
                }
            }
            RandomWorkoutState.REST -> {
                if (restartCommandOnPrevious) {
                    initWorkoutState(RandomWorkoutState.REST)
                    restartCommandOnPrevious = false
                } else {
                    initWorkoutState(RandomWorkoutState.WORK)
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

    private fun setCurrentRoundProgress() {
        val roundCrossRefs = structuredCommandCrossRefs.filter {it.round == currentRoundLD.value!!}

        var roundProgress = 0

        roundCrossRefs.forEach lit@{
            if (it.position_index > currentCommandCrossRef.position_index) return@lit

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