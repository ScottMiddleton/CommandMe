package com.middleton.scott.cmboxing.utils

import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.middleton.scott.cmboxing.MainActivity
import java.io.File
import java.text.DecimalFormat

const val WAVE_HEADER_SIZE = 44

fun getRecordFileByFileName(fileName: String): File {
    return File(getBaseFilePath(), fileName)
}

fun getRecordFileName(timeInMillis: Long): String {
    return "audio_$timeInMillis.wav"
}

fun getBaseFilePath(): String {
    return MainActivity.instance.getExternalFilesDir(null)?.absolutePath.toString() + "/"
}

fun File.toMediaSource(): MediaSource =
    DataSpec(this.toUri())
        .let { FileDataSource().apply { open(it) } }
        .let { DataSource.Factory { it } }
        .let { ProgressiveMediaSource.Factory(it, DefaultExtractorsFactory()) }
        .createMediaSource(MediaItem.fromUri(this.toUri()))

fun getAudioLengthToOneDP(recordFileName: String): Double? {
    val mmr = MediaMetadataRetriever()

    return try {
        mmr.setDataSource(getRecordFileByFileName(recordFileName).path)
        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toDouble()

        val df = DecimalFormat("#.#")

        if (duration != null) {
            df.format(duration / 1000).toDouble()
        } else {
            null
        }
    } catch (e: IllegalArgumentException) {
        null
    }
}