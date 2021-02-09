package com.middleton.scott.cmboxing.utils

import android.media.MediaRecorder
import android.util.Log
import java.io.IOException

object MediaRecorderManager {

    fun startRecording(mediaRecorder: MediaRecorder, audioFileOutput: String) {
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setOutputFile(audioFileOutput)
            mediaRecorder.prepare()
            mediaRecorder.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: java.lang.RuntimeException) {
            e.printStackTrace()
        }
    }

    fun stopRecording(
        mediaRecorder: MediaRecorder,
        onComplete: ((recordingComplete: Boolean) -> Unit)
    ) {
        try {
            mediaRecorder.stop()
            onComplete(true)
        } catch (stopException: RuntimeException) {
            Log.e("STOP ERROR", stopException.toString())
            onComplete(false)
        }
    }

}