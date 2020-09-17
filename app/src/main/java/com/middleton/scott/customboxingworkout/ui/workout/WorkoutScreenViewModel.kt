package com.middleton.scott.customboxingworkout.ui.workout

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.WorkoutWithCombinations

class WorkoutScreenViewModel(
    private val localDataSource: LocalDataSource,
    private val workoutId: Long
) : ViewModel() {

    private val workoutWithCombinations: WorkoutWithCombinations? =
        localDataSource.getWorkoutWithCombinations(workoutId)
    private val preparationTimeSecs = workoutWithCombinations?.workout?.preparation_time_secs ?: 0
    private val workTimeSecs = workoutWithCombinations?.workout?.work_time_secs ?: 0
    private val restTimeSecs = workoutWithCombinations?.workout?.rest_time_secs ?: 0
    private val numberOfRounds = workoutWithCombinations?.workout?.numberOfRounds
    private val intensity = workoutWithCombinations?.workout?.intensity

    private val _countdownSecondsLD = MutableLiveData<String>()
    val countdownSecondsLD: LiveData<String>
        get() = _countdownSecondsLD

    private val _currentRoundLD = MutableLiveData<Int>()
    val currentRoundLD: LiveData<Int>
        get() = _currentRoundLD

    private val _workoutStateLD = MutableLiveData<WorkoutState>()
    val workoutStateLD: LiveData<WorkoutState>
        get() = _workoutStateLD

    private var prepareCountDownTimer =
        object : CountDownTimer(preparationTimeSecs * 1000L, 1_000) {
            override fun onFinish() {
                _countdownSecondsLD.value = "0"
                _workoutStateLD.value = WorkoutState.WORK
            }

            override fun onTick(millisUntilFinished: Long) {
                _countdownSecondsLD.value = (millisUntilFinished / 1000 + 1).toString()
            }
        }

    private var workCountDownTimer =
        object : CountDownTimer(workTimeSecs * 1000L, 1_000) {
            override fun onFinish() {
                _countdownSecondsLD.value = "0"
                startRest()
            }

            override fun onTick(millisUntilFinished: Long) {
                _countdownSecondsLD.value = (millisUntilFinished / 1000 + 1).toString()
            }
        }

    private var restCountDownTimer =
        object : CountDownTimer(restTimeSecs * 1000L, 1_000) {
            override fun onFinish() {
                _countdownSecondsLD.value = "0"
                startNextRound()
            }

            override fun onTick(millisUntilFinished: Long) {
                _countdownSecondsLD.value = (millisUntilFinished / 1000 + 1).toString()
            }
        }

    init {
        _currentRoundLD.value = 0
        if (preparationTimeSecs > 0) {
            _workoutStateLD.value = WorkoutState.PREPARE
            prepareCountDownTimer.start()
        } else {
            _workoutStateLD.value = WorkoutState.WORK
            workCountDownTimer.start()
        }
    }

    fun getTotalRounds(): String {
        return numberOfRounds.toString()
    }

    private fun startNextRound() {
        _workoutStateLD.value = WorkoutState.WORK
        _currentRoundLD.value = _currentRoundLD.value?.plus(1)
        workCountDownTimer.start()
    }

    private fun startRest() {
        _workoutStateLD.value = WorkoutState.REST
        restCountDownTimer.start()
    }

}