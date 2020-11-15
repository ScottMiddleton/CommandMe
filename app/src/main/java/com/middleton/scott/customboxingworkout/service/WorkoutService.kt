package com.middleton.scott.customboxingworkout.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.MainActivity
import com.middleton.scott.customboxingworkout.other.Constants.ACTION_PAUSE_SERVICE
import com.middleton.scott.customboxingworkout.other.Constants.ACTION_SHOW_WORKOUT_SCREEN
import com.middleton.scott.customboxingworkout.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.middleton.scott.customboxingworkout.other.Constants.ACTION_STOP_SERVICE
import com.middleton.scott.customboxingworkout.other.Constants.NOTIFICATION_CHANNEL_ID
import com.middleton.scott.customboxingworkout.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.middleton.scott.customboxingworkout.other.Constants.NOTIFICATION_ID

class WorkoutService : LifecycleService() {

    var isFirstRun = true

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
        val notificiationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificiationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_boxer_24dp)
            .setContentTitle("CommandMe")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }

}