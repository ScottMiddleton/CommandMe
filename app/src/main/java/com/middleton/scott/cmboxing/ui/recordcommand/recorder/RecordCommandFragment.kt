package com.middleton.scott.cmboxing.ui.recordcommand.recorder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.createworkout.NumberPickerMinutesSecondsDialog
import com.middleton.scott.cmboxing.utils.DateTimeUtils
import com.middleton.scott.cmboxing.utils.DateTimeUtils.formatAsTime
import com.middleton.scott.cmboxing.utils.checkAudioPermission
import com.middleton.scott.cmboxing.utils.getDrawableCompat
import com.middleton.scott.cmboxing.utils.getRecordFile
import kotlinx.android.synthetic.main.fragment_record_command.*
import kotlinx.android.synthetic.main.include_play_recording.*
import org.koin.android.ext.android.inject
import java.io.File
import kotlin.math.sqrt

class RecordCommandFragment : Fragment() {
    private val viewModel: RecordCommandViewModel by inject()
    private lateinit var recorder: Recorder
    private lateinit var player: AudioPlayer
    private var timeToCompleteSecs: Int = 0
    private var saveAttempted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    deleteRecording()
                    findNavController().navigateUp()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_command, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
        checkAudioPermission(AUDIO_PERMISSION_REQUEST_CODE)
        initAudioRecorder()
        initRecorderUI()
    }

    private fun setClickListeners() {
        time_to_complete_et.setOnClickListener {
            val imm: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)

            var secs = 20
            if (!time_to_complete_et.text.isNullOrBlank()) {
                secs = timeToCompleteSecs
            }

            NumberPickerMinutesSecondsDialog(
                getString(R.string.time_to_complete),
                secs,
                { newSecs ->
                    timeToCompleteSecs = newSecs
                    time_to_complete_et.setText(DateTimeUtils.toMinuteSeconds(newSecs))
                    name_et.clearFocus()
                    if (saveAttempted) {
                        if (timeToCompleteSecs <= 0) {
                            time_to_complete_til.error = getString(R.string.greater_than_zero)
                        } else {
                            time_to_complete_til.isErrorEnabled = false
                        }
                    }
                },
                {

                }).show(childFragmentManager, "")
        }

        delete_recording_btn.setOnClickListener {
            deleteRecording()
            initAudioRecorder()
        }

        close_btn.setOnClickListener {
            deleteRecording()
            findNavController().navigateUp()
        }
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

    private fun initAudioRecorder() {
        timeline_tv.setText(getString(R.string.zero_seconds))
        viewModel.recordTimeMillis = System.currentTimeMillis()
        recorder = Recorder.getInstance(requireContext())
            .init(getRecordFile(viewModel.recordTimeMillis).toString())
        player_visualizer.visibility = GONE
        record_audio_button.visibility = VISIBLE
        recorder_visualizer.visibility = VISIBLE
        include_play_recording.visibility = GONE
        delete_recording_btn.visibility = INVISIBLE

        recorder.onStart = { handleRecordAudioAnimations(true) }
        recorder.onStop = {
            handleRecordAudioAnimations(false)
            recorder_visualizer.clear()
            timeline_tv.text = 0L.formatAsTime()
            initAudioPlayer()
            initPlayerUI()
            player_visualizer.visibility = VISIBLE
            record_audio_button.visibility = INVISIBLE
            recorder_visualizer.visibility = GONE
            include_play_recording.visibility = VISIBLE
            delete_recording_btn.visibility = VISIBLE
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

    private fun initAudioPlayer() {
        player = AudioPlayer.getInstance(requireContext())
            .init(getRecordFile(viewModel.recordTimeMillis)).apply {
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

    private fun handleRecordAudioAnimations(recording: Boolean) {
        if (recording) {
            record_audio_button.playAnimation()
            stop_button.visibility = VISIBLE
        } else {
            stop_button.visibility = GONE
            record_audio_button.cancelAnimation()
            record_audio_button.progress = 0.08f
        }
    }

    private fun updateTime(time: Long, isPlaying: Boolean) {
        timeline_tv.text = time.formatAsTime()
        player_visualizer.updateTime(time, isPlaying)
    }

    private fun deleteRecording() {
        getRecordFile(viewModel.recordTimeMillis).delete()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.release()
        player.release()
    }

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1
        const val SEEK_OVER_AMOUNT = 5000
    }
}
