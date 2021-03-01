package com.middleton.scott.cmboxing.ui.recordcommand.recorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.utils.DateTimeUtils.formatAsTime
import com.middleton.scott.cmboxing.utils.checkAudioPermission
import com.middleton.scott.cmboxing.utils.getDrawableCompat
import kotlinx.android.synthetic.main.fragment_record_command.*
import kotlinx.android.synthetic.main.include_play_recording.*
import kotlin.math.sqrt

class RecordCommandFragment : Fragment() {
    private lateinit var recorder: Recorder
    private lateinit var player: AudioPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_command, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAudioPermission(AUDIO_PERMISSION_REQUEST_CODE)
    }

    override fun onStart() {
        super.onStart()
        listenOnRecorderStates()
        initRecorderUI()
    }

    override fun onStop() {
        recorder.release()
        super.onStop()
    }

    private fun initRecorderUI() {
        record_audio_button.setOnClickListener { recorder.toggleRecording() }
        recorder_visualizer.ampNormalizer = { sqrt(it.toFloat()).toInt() }
    }

    private fun initPlayerUI() {
        player_visualizer.apply {
            ampNormalizer = { sqrt(it.toFloat()).toInt() }
            onStartSeeking = {
                player.pause()
            }
            onSeeking = { timeline_tv.text = it.formatAsTime() }
            onFinishedSeeking = { time, isPlayingBefore ->
                player.seekTo(time)
                if (isPlayingBefore) {
                    player.resume()
                }
            }
            onAnimateToPositionFinished = { time, isPlaying ->
                updateTime(time, isPlaying)
                player.seekTo(time)
            }
        }

        play_button.setOnClickListener { player.togglePlay() }
        seek_forward_button.setOnClickListener { player_visualizer.seekOver(SEEK_OVER_AMOUNT) }
        seek_backward_button.setOnClickListener { player_visualizer.seekOver(-SEEK_OVER_AMOUNT) }

        lifecycleScope.launchWhenCreated {
            val amps = player.loadAmps()
            player_visualizer.setWaveForm(amps, player.tickDuration)
        }
    }

    private fun listenOnRecorderStates() {
        recorder = Recorder.getInstance(requireContext()).init()

        recorder.onStart = {}
        recorder.onStop = {
            recorder_visualizer.clear()
            timeline_tv.text = 0L.formatAsTime()
            listenOnPlayerStates()
            initPlayerUI()
            player_visualizer.visibility = VISIBLE
            record_audio_button.visibility = GONE
            recorder_visualizer.visibility = GONE
            include_play_recording.visibility = VISIBLE
//            recordButton.icon = getDrawableCompat(R.drawable.ic_record_24)
        }

        recorder.onAmpListener = {
            requireActivity().runOnUiThread {
                if (recorder.isRecording) {
                    timeline_tv.text = recorder.getCurrentTime().formatAsTime()
                    recorder_visualizer.addAmp(it, recorder.tickDuration)
                }
            }
        }
    }

    private fun listenOnPlayerStates() {
        player = AudioPlayer.getInstance(requireContext()).init().apply {
            onStart =
                { play_button.icon = requireContext().getDrawableCompat(R.drawable.ic_pause_24) }
            onStop = {
                play_button.icon = requireContext().getDrawableCompat(R.drawable.ic_play_arrow_24)
            }
            onPause = {
                play_button.icon = requireContext().getDrawableCompat(R.drawable.ic_play_arrow_24)
            }
            onResume =
                { play_button.icon = requireContext().getDrawableCompat(R.drawable.ic_pause_24) }
            onProgress = { time, isPlaying -> updateTime(time, isPlaying) }
        }
    }

    private fun updateTime(time: Long, isPlaying: Boolean) {
        timeline_tv.text = time.formatAsTime()
        player_visualizer.updateTime(time, isPlaying)
    }

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1
        const val SEEK_OVER_AMOUNT = 5000
    }
}
