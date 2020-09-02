package com.middleton.scott.customboxingworkout.ui.createworkout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.middleton.scott.customboxingworkout.datasource.local.LocalDataSource
import com.middleton.scott.customboxingworkout.datasource.local.model.Workout
import kotlinx.coroutines.flow.map

class CreateWorkoutViewModel(private val localDataSource: LocalDataSource, workoutId: Long) : ViewModel() {

    var workout = Workout()

    val workoutLD = localDataSource.getWorkoutById(workoutId).map {
        it?.let { workout = it }
        it
    }.asLiveData()

    val preparationDurationSecsLD = MutableLiveData<Int>()
    val numberOfRoundsLD = MutableLiveData<Int>()
    val roundDurationSecsLD = MutableLiveData<Int>()
    val restDurationSecsLD = MutableLiveData<Int>()
    val intensityLD = MutableLiveData<Int>()

    val dbUpdateLD = MutableLiveData<Boolean>()

    fun upsertWorkout() {
//        val id = localDataSource.upsertExercise(Exercise("Exercise 111", 10, "test"))
//        val id1 = localDataSource.upsertExercise(Exercise("Exercise 200", 10, "test"))
//        val id2 = localDataSource.upsertExercise(Exercise("Exercise 300", 10, "test"))
//        val id3 = localDataSource.upsertExercise(Exercise("Exercise 400", 10, "test"))
//        val id4 = localDataSource.upsertExercise(Exercise("Exercise 500", 10, "test"))

        localDataSource.upsertWorkout(workout)
        dbUpdateLD.value = true
    }

    fun setWorkoutName(name: String){
        if(name.isNotEmpty()){
            workout.name = name
        }
    }

    fun setPreparationTime(preparationDurationSecs: Int) {
        if (preparationDurationSecs >= 0) {
            workout.preparation_time_secs = preparationDurationSecs
            preparationDurationSecsLD.value = preparationDurationSecs
        }
    }

    fun setNumberOfRounds(numberOfRounds: Int) {
        workout.numberOfRounds = numberOfRounds
        numberOfRoundsLD.value = numberOfRounds
    }

    fun setRoundDuration(roundDurationSecs: Int) {
        if (roundDurationSecs >= 0) {
            workout.round_duration_secs = roundDurationSecs
            roundDurationSecsLD.value = roundDurationSecs
        }
    }

    fun setRestDuration(restDurationSecs: Int) {
        if (restDurationSecs >= 0) {
            workout.rest_duration_secs = restDurationSecs
            restDurationSecsLD.value = restDurationSecs
        }
    }

    fun setIntensity(intensity: Int) {
        workout.intensity = intensity
        intensityLD.value = intensity
    }
}