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

    var audioFileBaseDirectory = ""
    private val workoutWithCombinations: WorkoutWithCombinations? =
        localDataSource.getWorkoutWithCombinations(workoutId)
    val workoutName = workoutWithCombinations?.workout?.name
    private var combinations: List<Combination>?
    private val preparationTimeSecs = workoutWithCombinations?.workout?.preparation_time_secs ?: 0
    private val workTimeSecs = workoutWithCombinations?.workout?.work_time_secs ?: 0
    private val restTimeSecs = workoutWithCombinations?.workout?.rest_time_secs ?: 0
    private val numberOfRounds = workoutWithCombinations?.workout?.numberOfRounds ?: 0
    private val intensity = workoutWithCombinations?.workout?.intensity

    private var millisRemaining: Long = 0
    private var secondsUntilNextCombination = 0

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

    private lateinit var countDownTimer: CountDownTimer

    init {
        combinations = workoutWithCombinations?.combinations
        handleCombinationFrequencies()
        _currentRoundLD.value = 0
        if (preparationTimeSecs > 0) {
            _workoutStateLD.value = WorkoutState.PREPARE
            _countdownSecondsLD.value = preparationTimeSecs
        } else {
            _workoutStateLD.value = WorkoutState.WORK
            _countdownSecondsLD.value = workTimeSecs
            _currentRoundLD.value = 1
        }
    }

    private var workoutHasBegun = false

    fun getTotalRounds(): String {
        return numberOfRounds.toString()
    }

    private fun initCountdown(timeRemainingMillis: Long) {
        millisRemaining = timeRemainingMillis
        countDownTimer =
            object : CountDownTimer(timeRemainingMillis, 1_000) {
                override fun onFinish() {
                    when (workoutStateLD.value) {
                        WorkoutState.PREPARE -> startNextRound()
                        WorkoutState.WORK -> startRest()
                        WorkoutState.REST -> startNextRound()
                    }
                }

                override fun onTick(millisUntilFinished: Long) {
                    _countdownSecondsLD.value = (millisUntilFinished / 1000 + 1).toInt()
                    millisRemaining = millisUntilFinished

                    if (workoutStateLD.value == WorkoutState.WORK) {
                        if (secondsUntilNextCombination == 0) {
                            _currentCombinationLD.value = getRandomCombination()
                            secondsUntilNextCombination = 2
                        } else {
                            secondsUntilNextCombination -= 1
                        }
                    }

                }
            }.start()
    }

    private fun startNextRound() {
        _workoutStateLD.value = WorkoutState.WORK
        currentRound++
        _currentRoundLD.value = currentRound
        initCountdown(workTimeSecs * 1000L)
    }

    private fun startRest() {
        if (currentRound >= numberOfRounds) {
            countDownTimer.cancel()
        } else {
            _workoutStateLD.value = WorkoutState.REST
            initCountdown(restTimeSecs * 1000L)
        }
    }

    fun onStart() {
        if (workoutHasBegun) {
            initCountdown(millisRemaining)
        } else {
            var timeSecs = 0
            when (workoutStateLD.value) {
                WorkoutState.PREPARE -> timeSecs = preparationTimeSecs
                WorkoutState.WORK -> timeSecs = workTimeSecs
                WorkoutState.REST -> timeSecs = restTimeSecs
            }
            initCountdown(timeSecs * 1000L)
            workoutHasBegun = true
        }
    }

    fun onPause() {
        countDownTimer.cancel()
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
                val frequencyType = selectedCombinationsCrossRefs.firstOrNull { combination.id == it.combination_id }?.frequency
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

}