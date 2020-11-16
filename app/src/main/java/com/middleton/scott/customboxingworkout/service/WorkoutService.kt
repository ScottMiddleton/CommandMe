package com.middleton.scott.customboxingworkout.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.MainActivity
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.other.Constants.ACTION_PAUSE_SERVICE
import com.middleton.scott.customboxingworkout.other.Constants.ACTION_SHOW_WORKOUT_SCREEN
import com.middleton.scott.customboxingworkout.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.middleton.scott.customboxingworkout.other.Constants.ACTION_STOP_SERVICE
import com.middleton.scott.customboxingworkout.other.Constants.NOTIFICATION_CHANNEL_ID
import com.middleton.scott.customboxingworkout.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.middleton.scott.customboxingworkout.other.Constants.NOTIFICATION_ID
import com.middleton.scott.customboxingworkout.ui.workout.WorkoutState
import com.middleton.scott.customboxingworkout.utils.DateTimeUtils
import kotlinx.android.synthetic.main.fragment_workout_screen.*
import java.io.IOException

class WorkoutService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    private var mediaPlayer = MediaPlayer()

    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManager

    companion object {
        val serviceWorkoutStateLD = MutableLiveData<WorkoutState>()
        val serviceCountdownSecondsLD = MutableLiveData<String>()
        val serviceCommandAudioLD = MutableLiveData<ServiceAudioCommand>()
    }

    private fun postInitialValues(){
        serviceCountdownSecondsLD.value = ""
        serviceCommandAudioLD.value = ServiceAudioCommand("","")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {

                    }
                }
                ACTION_PAUSE_SERVICE -> {

                }
                ACTION_STOP_SERVICE -> {
                    killService()
                }

            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getMainActivityPendingIntent() =
        PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_WORKOUT_SCREEN
        }, FLAG_UPDATE_CURRENT)

    private fun startForegroundService() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_boxer_24dp)
            .setContentTitle("")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        serviceWorkoutStateLD.observe(this, Observer {
            if(!serviceKilled){
            notificationBuilder.setContentTitle(it.name)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())}
        })

        serviceCountdownSecondsLD.observe(this, Observer {
            if(!serviceKilled){
            notificationBuilder.setContentText(it)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())}
        })

        serviceCommandAudioLD.observe(this, Observer {
            startPlayingCombinationAudio(it.fileName, it.audioBaseFileDirectory)
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }

    private fun startPlayingCombinationAudio(fileName: String, audioBaseFileDirectory: String) {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioBaseFileDirectory + fileName)
                prepare()
                this.setOnCompletionListener {
                }
                start()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }
    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

}

data class ServiceAudioCommand(val fileName: String, val audioBaseFileDirectory: String)
