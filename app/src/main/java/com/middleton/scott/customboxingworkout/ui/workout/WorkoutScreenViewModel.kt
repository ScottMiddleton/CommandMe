package com.middleton.scott.customboxingworkout.ui.workout

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations
import kotlinx.coroutines.launch

class WorkoutScreenViewModel(
    private val localDataSource: LocalDataSource,
    val workoutId: Long
) : ViewModel() {

    private var restartOnPrevious = false
    private var workoutHasPreparation = false
    private var workoutHasBegun = false
    var workoutInProgress = false
    var combinationsThrown = 0

    var audioFileBaseDirectory = ""
    private val workoutWithCombinations: WorkoutWithCombinations? =
        localDataSource.getWorkoutWithCombinations(workoutId)
    val workoutName = workoutWithCombinations?.workout?.name
    private var combinations: List<Combination>? = null
    private val preparationTimeSecs = workoutWithCombinations?.workout?.preparation_time_secs ?: 0
    private val workTimeSecs = workoutWithCombinations?.workout?.work_time_secs ?: 0
    private val restTimeSecs = workoutWithCombinations?.workout?.rest_time_secs ?: 0
    private val numberOfRounds = workoutWithCombinations?.workout?.numberOfRounds ?: 0
    private val intensity = workoutWithCombinations?.workout?.intensity

    private var millisRemainingAtPause: Long = 0
    private var millisUntilNextCombination: Long = 0
    private var totalSecondsElapsed: Int = 0
    var totalWorkoutSecs = getTotalWorkoutLengthSecs()

    private val _totalSecondsElapsedLD = MutableLiveData<Int>()
    val totalSecondsElapsedLD: LiveData<Int>
        get() = _totalSecondsElapsedLD

    private val _countdownSecondsLD = MutableLiveData<Int>()
    val countdownSecondsLD: LiveData<Int>
        get() = _countdownSecondsLD

    private var currentRound = 0
    private val _currentRoundLD = MutableLiveData<Int>()
    val currentRoundLD: LiveData<Int>
        get() = _currentRoundLD

    private val _workoutStateLD = MutableLiveData<WorkoutState>()
    val workoutStateLD: LiveData<WorkoutState>
        get() = _workoutStateLD

    private val _currentCombinationLD = MutableLiveData<Combination>()
    val currentCombinationLD: LiveData<Combination>
        get() = _currentCombinationLD

    private val _playStartBellLD = MutableLiveData<Boolean>()
    val playStartBellLD: LiveData<Boolean>
        get() = _playStartBellLD

    private val _playEndBellLD = MutableLiveData<Boolean>()
    val playEndBellLD: LiveData<Boolean>
        get() = _playEndBellLD

    private lateinit var countDownTimer: CountDownTimer

    init {
        workoutHasPreparation = preparationTimeSecs > 0
        combinations = workoutWithCombinations?.combinations
        handleCombinationFrequencies()
        initWorkout()
    }

    fun getTotalRounds(): String {
        return numberOfRounds.toString()
    }

    private fun initWorkout() {
        if (workoutHasPreparation) {
            setCurrentRound(0)
            initWorkoutState(WorkoutState.PREPARE)
        } else {
            setCurrentRound(1)
            initWorkoutState(WorkoutState.WORK)
        }
    }

    private fun initWorkoutState(state: WorkoutState) {
        _workoutStateLD.value = state

        // If is last round
        if (currentRound >= numberOfRounds) {
            onComplete()
        } else {
            when (state) {
                WorkoutState.PREPARE -> {
                    _countdownSecondsLD.value = preparationTimeSecs
                    millisRemainingAtPause = preparationTimeSecs * 1000L
                }
                WorkoutState.WORK -> {
                    _countdownSecondsLD.value = workTimeSecs
                    millisRemainingAtPause = workTimeSecs * 1000L
                }
                WorkoutState.REST -> {
                    _countdownSecondsLD.value = restTimeSecs
                    millisRemainingAtPause = restTimeSecs * 1000L
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
    }

    private fun setCurrentRound(round: Int) {
        currentRound = round
        _currentRoundLD.value = round
    }

    private fun initCountdown(countdownMillis: Long) {
        workoutInProgress = true

        if (workoutStateLD.value == WorkoutState.WORK) {
            _playStartBellLD.value = true
        }

        millisRemainingAtPause = countdownMillis

        countDownTimer =
            object : CountDownTimer(countdownMillis, 1000) {
                override fun onFinish() {
                    if (workoutStateLD.value != WorkoutState.PREPARE) {
                        onSecondElapsed()
                    }

                    millisUntilNextCombination = 0L

                    when (workoutStateLD.value) {
                        WorkoutState.PREPARE -> {
                            setCurrentRound(currentRound + 1)
                            initWorkoutState(WorkoutState.WORK)
                        }

                        WorkoutState.WORK -> {
                            initWorkoutState(WorkoutState.REST)
                            _playEndBellLD.value = true
                        }

                        WorkoutState.REST -> {
                            setCurrentRound(currentRound + 1)
                            initWorkoutState(WorkoutState.WORK)
                        }
                    }
                }

                override fun onTick(millisUntilFinished: Long) {
                    _countdownSecondsLD.value = (millisUntilFinished / 1000 + 1).toInt()
                    millisRemainingAtPause = millisUntilFinished

                    restartOnPrevious = (countdownMillis - millisUntilFinished) > 1000

                    if (workoutStateLD.value != WorkoutState.PREPARE) {
                        onSecondElapsed()
                    }

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
        combinationsThrown++
        val nextCombination: Combination? = getRandomCombination()
        _currentCombinationLD.value = nextCombination
        val timeToCompleteCombination = nextCombination?.timeToCompleteMillis ?: 2000
        millisUntilNextCombination = getTimeUntilNextCombination(timeToCompleteCombination)
    }

    private fun onSecondElapsed() {
        totalSecondsElapsed++
        _totalSecondsElapsedLD.value = totalSecondsElapsed
    }

    fun onNext() {
        if (workoutHasBegun) {
            countDownTimer.cancel()
        }

        when (workoutStateLD.value) {
            WorkoutState.PREPARE -> {
                setCurrentRound(currentRound + 1)
                initWorkoutState(WorkoutState.WORK)
            }

            WorkoutState.WORK -> initWorkoutState(WorkoutState.REST)

            WorkoutState.REST -> {
                setCurrentRound(currentRound + 1)
                initWorkoutState(WorkoutState.WORK)
            }
        }
    }

    fun onRestart() {
        workoutHasBegun = false
        workoutInProgress = false
        combinationsThrown = 0
        initWorkout()
    }

    fun onPrevious() {
        millisRemainingAtPause = preparationTimeSecs * 1000L

        if (workoutHasBegun) {
            countDownTimer.cancel()
        }

        when (workoutStateLD.value) {
            WorkoutState.PREPARE -> {
                initWorkoutState(WorkoutState.PREPARE)
            }
            WorkoutState.WORK -> {
                if (restartOnPrevious) {
                    initWorkoutState(WorkoutState.WORK)
                    restartOnPrevious = false
                } else {
                    setCurrentRound(currentRound - 1)
                    if (currentRound < 1) {
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
            _playStartBellLD.value = true
            initCountdown(timeSecs * 1000L)
            workoutHasBegun = true
        }
    }

    fun onPause() {
        workoutInProgress = false
        countDownTimer.cancel()
    }

    private fun onComplete() {
        if (workoutInProgress) {
            countDownTimer.cancel()
        }

        workoutInProgress = false
        _workoutStateLD.value = WorkoutState.COMPLETE
    }

    fun getCountdownProgressBarMax(): Int {
        return when (workoutStateLD.value) {
            WorkoutState.PREPARE -> preparationTimeSecs
            WorkoutState.WORK -> workTimeSecs
            WorkoutState.REST -> restTimeSecs
            else -> 0
        }
    }

    private fun handleCombinationFrequencies() {
        viewModelScope.launch {
            val multipliedCombinationsList = mutableListOf<Combination>()
            val selectedCombinationsCrossRefs =
                localDataSource.getSelectedCombinationCrossRefs(workoutId)

            combinations?.forEach { combination ->
                val frequencyType =
                    selectedCombinationsCrossRefs.firstOrNull { combination.id == it.combination_id }?.frequency
                frequencyType?.multiplicationValue?.let {
                    repeat(it) {
                        multipliedCombinationsList.add(combination)
                    }
                }
            }
            combinations = multipliedCombinationsList
        }
    }

    private fun getRandomCombination(): Combination? {
        var randomCombination: Combination? = null
        combinations?.let { randomCombination = it.shuffled().take(1)[0] }
        return randomCombination
    }

    private fun getTimeUntilNextCombination(timeToCompleteCombinationMillis: Long): Long {
        var amountToAdjustMillis = 0L

        when (intensity) {
            10 -> amountToAdjustMillis = -(timeToCompleteCombinationMillis / 100) * 40
            9 -> amountToAdjustMillis = -(timeToCompleteCombinationMillis / 100) * 30
            8 -> amountToAdjustMillis = -(timeToCompleteCombinationMillis / 100) * 20
            7 -> amountToAdjustMillis = -(timeToCompleteCombinationMillis / 100) * 10
            6 -> amountToAdjustMillis = -(timeToCompleteCombinationMillis / 100) * 5
            5 -> amountToAdjustMillis = 0
            4 -> amountToAdjustMillis = (timeToCompleteCombinationMillis / 100) * 10
            3 -> amountToAdjustMillis = (timeToCompleteCombinationMillis / 100) * 20
            2 -> amountToAdjustMillis = (timeToCompleteCombinationMillis / 100) * 30
            1 -> amountToAdjustMillis = (timeToCompleteCombinationMillis / 100) * 40

        }

        val adjustedTimeToCompleteCombination =
            timeToCompleteCombinationMillis + amountToAdjustMillis
        return adjustedTimeToCompleteCombination + calculateCommandTimeBufferMillis()
    }

    private fun calculateCommandTimeBufferMillis(): Long {
        var timeBuffer = 3000L
        when (intensity) {
            10 -> timeBuffer = 1.times(500).minus(500).toLong()
            9 -> timeBuffer = 2.times(500).minus(500).toLong()
            8 -> timeBuffer = 3.times(500).minus(500).toLong()
            7 -> timeBuffer = 4.times(500).minus(500).toLong()
            6 -> timeBuffer = 5.times(500).minus(500).toLong()
            5 -> timeBuffer = 6.times(500).minus(500).toLong()
            4 -> timeBuffer = 7.times(500).minus(500).toLong()
            3 -> timeBuffer = 8.times(500).minus(500).toLong()
            2 -> timeBuffer = 9.times(500).minus(500).toLong()
            1 -> timeBuffer = 10.times(500).minus(500).toLong()
        }

        return timeBuffer
    }

    private fun getTotalWorkoutLengthSecs(): Int {
        return (workTimeSecs * numberOfRounds) + (restTimeSecs * numberOfRounds) - restTimeSecs
    }

}