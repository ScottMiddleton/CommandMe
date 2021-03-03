package com.middleton.scott.cmboxing.utils

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

const val WAVE_HEADER_SIZE = 44

fun getRecordFile(timeInMillis: Long): File {
    return File(MainActivity.baseFilePath, "audio_$timeInMillis.wav")
}

fun File.toMediaSource(): MediaSource =
    DataSpec(this.toUri())
        .let { FileDataSource().apply { open(it) } }
        .let { DataSource.Factory { it } }
        .let { ProgressiveMediaSource.Factory(it, DefaultExtractorsFactory()) }
        .createMediaSource(MediaItem.fromUri(this.toUri()))