package com.middleton.scott.customboxingworkout.ui.workout

import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
    private lateinit var soundPool: SoundPool
    private var workStartAudioId: Int = 0
    private var workEndAudioId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSoundPool()
        (activity as MainActivity).supportActionBar?.title = viewModel.workoutName
        viewModel.audioFileBaseDirectory =
            view.context.getExternalFilesDir(null)?.absolutePath + "/"
        total_rounds_count_tv.text = viewModel.getTotalRounds()
        remaining_tv.text = DateTimeUtils.toMinuteSeconds(viewModel.totalWorkoutSecs)
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

        viewModel.totalSecondsElapsedLD.observe(viewLifecycleOwner, Observer {
            elapsed_tv.text = DateTimeUtils.toMinuteSeconds(it)
            remaining_tv.text = DateTimeUtils.toMinuteSeconds(viewModel.totalWorkoutSecs - it)
        })

        viewModel.workoutStateLD.observe(viewLifecycleOwner, Observer {
            workout_state_tv.text = it.toString()
            countdown_pb.max = viewModel.getCountdownProgressBarMax()

            when (it) {
                WorkoutState.PREPARE -> {
                    round_count_ll.visibility = INVISIBLE
                    play_command_lottie.visibility = GONE
                    countdown_pb.progressTintList = ColorStateList.valueOf(Color.YELLOW)
                }
                WorkoutState.WORK -> {
                    round_count_ll.visibility = VISIBLE
                    combination_name_tv.visibility = VISIBLE
                    play_command_lottie.visibility = VISIBLE
                    countdown_pb.progressTintList = ColorStateList.valueOf(Color.RED)
                }
                WorkoutState.REST -> {
                    combination_name_tv.visibility = INVISIBLE
                    round_count_ll.visibility = VISIBLE
                    play_command_lottie.visibility = VISIBLE
                    countdown_pb.progressTintList = ColorStateList.valueOf(Color.GREEN)
                }
                WorkoutState.COMPLETE -> {
                    handlePlayAnimationLottie(true)
                    mediaPlayer.stop()
                    WorkoutCompleteDialog(
                        viewModel.totalWorkoutSecs,
                        viewModel.combinationsThrown,
                        {viewModel.onRestart()},
                        {findNavController().popBackStack()}).show(childFragmentManager, null)
                }
            }
        })

        viewModel.currentCombinationLD.observe(viewLifecycleOwner, Observer {
            combination_name_tv.text = it.name
            startPlayingCombinationAudio(it.file_name)
        })

        viewModel.playEndBellLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                mediaPlayer.stop()
                soundPool.play(
                    workEndAudioId,
                    1.0f,
                    1.0f,
                    0,
                    0,
                    1.0f
                )
            }
        })

        viewModel.playStartBellLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                soundPool.play(
                    workStartAudioId,
                    1.0f,
                    1.0f,
                    0,
                    0,
                    1.0f
                )
            }
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
        play_command_lottie.playAnimation()
    }

    private fun setClickListeners() {
        start_workout_lottie.speed = 3f
        start_workout_lottie.setMinAndMaxFrame(30, 60)
        start_workout_lottie.setOnClickListener {
            if (!viewModel.workoutInProgress) {
                handlePlayAnimationLottie(true)
                handlePlayAnimationLottie(false)
                viewModel.onPlay()
            } else {
                handlePlayAnimationLottie(true)
                mediaPlayer.stop()
                viewModel.onPause()
            }
        }
        next_btn.setOnClickListener {
            viewModel.onNext()
        }

        previous_btn.setOnClickListener {
            viewModel.onPrevious()
        }
    }

    private fun handlePlayAnimationLottie(
        playInReverse: Boolean
    ) {
        if (playInReverse) {
            start_workout_lottie.setMinAndMaxFrame(0, 30)
            start_workout_lottie.playAnimation()
        } else {
            start_workout_lottie.setMinAndMaxFrame(30, 60)
            start_workout_lottie.playAnimation()
        }
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

        workStartAudioId = soundPool.load(context, R.raw.work_start, 1)
        workEndAudioId = soundPool.load(context, R.raw.work_end, 1)

    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

}