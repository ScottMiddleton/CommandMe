package com.middleton.scott.cmboxing.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.other.Constants.ACTION_PAUSE_SERVICE
import com.middleton.scott.cmboxing.other.Constants.ACTION_SHOW_WORKOUT_SCREEN
import com.middleton.scott.cmboxing.other.Constants.ACTION_START_OR_RESUME_RANDOM_SERVICE
import com.middleton.scott.cmboxing.other.Constants.ACTION_START_OR_RESUME_STRUCTURED_SERVICE
import com.middleton.scott.cmboxing.other.Constants.ACTION_STOP_SERVICE
import com.middleton.scott.cmboxing.other.Constants.NOTIFICATION_CHANNEL_ID
import com.middleton.scott.cmboxing.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.middleton.scott.cmboxing.other.Constants.NOTIFICATION_ID
import com.middleton.scott.cmboxing.ui.createworkout.WorkoutType
import com.middleton.scott.cmboxing.ui.workout.WorkoutState
import com.middleton.scott.cmboxing.utils.getBaseFilePath
import java.io.IOException

class WorkoutService : LifecycleService() {

    var workoutType = WorkoutType.STRUCTURED
    var isFirstRun = true
    var serviceKilled = false

    private var mediaPlayer = MediaPlayer()
    private lateinit var soundPool: SoundPool
    private var workStartAudioId: Int = 0
    private var workEndAudioId: Int = 0

    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManager

    companion object {
        val serviceWorkoutStateLD = MutableLiveData<WorkoutState>()
        val serviceCountdownSecondsLD = MutableLiveData<String>()
        val serviceCommandAudioLD = MutableLiveData<ServiceAudioCommand>()
        val playStartBellLD = MutableLiveData<Boolean>()
        val playEndBellLD = MutableLiveData<Boolean>()
    }

    private fun postInitialValues() {
        serviceCountdownSecondsLD.value = ""
        serviceCommandAudioLD.value = ServiceAudioCommand("", "")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_STRUCTURED_SERVICE -> {
                    if (isFirstRun) {
                        workoutType = WorkoutType.STRUCTURED
                        initSoundPool()
                        startForegroundService()
                        isFirstRun = false
                    }
                }
                ACTION_START_OR_RESUME_RANDOM_SERVICE -> {
                    if (isFirstRun) {
                        workoutType = WorkoutType.RANDOM
                        initSoundPool()
                        startForegroundService()
                        isFirstRun = false
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
            .setSmallIcon(R.drawable.ic_shout)
            .setContentTitle("")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        serviceWorkoutStateLD.observe(this, Observer {
            if (!serviceKilled) {
                if (workoutType == WorkoutType.STRUCTURED && it == WorkoutState.WORK) {

                } else {
                    notificationBuilder.setContentTitle(it.name)
                }

                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        })

        serviceCountdownSecondsLD.observe(this, Observer {
            if (!serviceKilled) {
                notificationBuilder.setContentText(it)
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        })

        serviceCommandAudioLD.observe(this, Observer {
            if (workoutType == WorkoutType.STRUCTURED) {
                notificationBuilder.setContentTitle(it.name)
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            }
            startPlayingCombinationAudio(it.fileName)
        })

        playEndBellLD.observe(this, Observer {
            if (it) {
                mediaPlayer.stop()
                soundPool.play(
                    workEndAudioId,
                    0.2f,
                    0.2f,
                    0,
                    0,
                    1.0f
                )
            }
        })

        playStartBellLD.observe(this, Observer {
            if (it) {
                soundPool.play(
                    workStartAudioId,
                    0.2f,
                    0.2f,
                    0,
                    0,
                    1.0f
                )
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }

    private fun startPlayingCombinationAudio(fileName: String) {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(getBaseFilePath() + fileName)
                prepare()
                this.setOnCompletionListener {
                }
                start()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        postInitialValues()
        try {
            soundPool.release()
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
        stopForeground(true)
        stopSelf()
    }

    private fun initSoundPool() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build()
        } else {
            SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0)
        }

        workStartAudioId = soundPool.load(this, R.raw.work_start, 1)
        workEndAudioId = soundPool.load(this, R.raw.work_end, 1)
    }

}

data class ServiceAudioCommand(
    val name: String,
    val fileName: String
)
