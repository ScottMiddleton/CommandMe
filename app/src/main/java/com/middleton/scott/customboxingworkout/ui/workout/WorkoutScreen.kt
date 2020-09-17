package com.middleton.scott.customboxingworkout.ui.workout

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.MainActivity
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import com.middleton.scott.customboxingworkout.utils.DateTimeUtils
import kotlinx.android.synthetic.main.fragment_workout_screen.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.IOException

class WorkoutScreen : BaseFragment() {
    private val args: WorkoutScreenArgs by navArgs()
    private val viewModel: WorkoutScreenViewModel by viewModel { parametersOf(args.workoutId) }

    private var mediaPlayer = MediaPlayer()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = viewModel.workoutName
        viewModel.audioFileBaseDirectory =
            view.context.getExternalFilesDir(null)?.absolutePath + "/"
        total_rounds_count_tv.text = viewModel.getTotalRounds()
        subscribeUI()
        setClickListeners()
    }

    private fun subscribeUI() {
        viewModel.currentRoundLD.observe(viewLifecycleOwner, Observer {
            current_round_count_tv.text = it.toString()
        })

        viewModel.countdownSecondsLD.observe(viewLifecycleOwner, Observer {
            countdown_seconds_tv.text = DateTimeUtils.toMinuteSeconds(it)
            countdown_pb.progress = it
        })

        viewModel.workoutStateLD.observe(viewLifecycleOwner, Observer {
            workout_state_tv.text = it.toString()
            countdown_pb.max = viewModel.getCountdownProgressBarMax()

            when (it) {
                WorkoutState.PREPARE -> {
                    round_count_ll.visibility = INVISIBLE
                }
                WorkoutState.WORK -> {
                    round_count_ll.visibility = VISIBLE
                    combination_name_tv.visibility = VISIBLE
                }
                WorkoutState.REST -> {
                    combination_name_tv.visibility = INVISIBLE
                    round_count_ll.visibility = VISIBLE
                }
            }
        })

        viewModel.currentCombinationLD.observe(viewLifecycleOwner, Observer {
            combination_name_tv.text = it.name
            startPlayingCombinationAudio(it.file_name)
        })
    }

    private fun startPlayingCombinationAudio(fileName: String) {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(viewModel.audioFileBaseDirectory + fileName)
                prepare()
                this.setOnCompletionListener {
                }
                start()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }
    }

    private fun setClickListeners() {
        start_toggle_btn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.onStart()
            } else {
                viewModel.onPause()
            }
        }
    }

}