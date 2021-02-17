package com.middleton.scott.cmboxing.ui.workout

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.middleton.scott.cmboxing.other.Constants.ACTION_STOP_SERVICE
import com.middleton.scott.cmboxing.service.WorkoutService
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.utils.DateTimeUtils
import kotlinx.android.synthetic.main.fragment_workout_screen.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class StructuredWorkoutScreen : BaseFragment() {
    private val args: StructuredWorkoutScreenArgs by navArgs()
    private val viewModel: StructuredWorkoutScreenViewModel by viewModel { parametersOf(args.workoutId) }
    private var mediaPlayer = MediaPlayer()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
//        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
//        initRoundProgressView()
        (activity as MainActivity).supportActionBar?.title = viewModel.workoutName
        viewModel.audioFileBaseDirectory = view.context.getExternalFilesDir(null)?.absolutePath + "/"
//        total_rounds_count_tv.text = viewModel.getTotalRounds().toString()
//        remaining_tv.text = DateTimeUtils.toMinuteSeconds(viewModel.totalWorkoutSecs)
        subscribeUI()
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
//        // This is to ensure that the round progress bars update after the app has been backgrounded
//        if (viewModel.workoutStateLD.value == RandomWorkoutState.REST) {
//            repeat(viewModel.getCurrentRound()) { index ->
//                val seekbar = round_progress_ll.getChildAt(index) as SeekBar?
//                seekbar?.thumb?.mutate()?.alpha = 255
//                seekbar?.progress = viewModel.getCountdownProgressBarMax(RandomWorkoutState.WORK)
//            }
//        } else if (viewModel.workoutStateLD.value == RandomWorkoutState.WORK) {
//            repeat(viewModel.getCurrentRound()) { index ->
//                val seekbar = round_progress_ll.getChildAt(index - 1) as SeekBar?
//                seekbar?.thumb?.mutate()?.alpha = 255
//                seekbar?.progress = viewModel.getCountdownProgressBarMax(RandomWorkoutState.WORK)
//            }
//        }
    }

    private fun subscribeUI() {
        viewModel.currentRoundLD.observe(viewLifecycleOwner, { it ->
            if (it == 0) {
                current_round_count_tv.text = "1"
            } else {
                current_round_count_tv.text = it.toString()
            }
        })

        viewModel.countdownSecondsLD.observe(viewLifecycleOwner, Observer {
            countdown_seconds_tv.text = DateTimeUtils.toMinuteSeconds(it)
            countdown_pb.progress = it
        })

//        viewModel.roundProgressLD.observe(viewLifecycleOwner, Observer {
//            val seekbar = round_progress_ll.getChildAt(viewModel.getCurrentRound() - 1) as SeekBar
//            seekbar.thumb.mutate().alpha = 255
//            seekbar.progress = it
//        })

//        viewModel.totalSecondsElapsedLD.observe(viewLifecycleOwner, Observer {
//            elapsed_tv.text = DateTimeUtils.toMinuteSeconds(it)
//            remaining_tv.text = DateTimeUtils.toMinuteSeconds(viewModel.totalWorkoutSecs - it)
//        })

        viewModel.workoutStateLD.observe(viewLifecycleOwner, Observer {

            countdown_pb.max = viewModel.getCountdownProgressBarMax(it)

            when (it) {
                RandomWorkoutState.PREPARE -> {
                    workout_state_tv.text = it.toString()
                    play_command_lottie.visibility = GONE
                    countdown_pb.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.yellow
                        )
                    )
                }
                RandomWorkoutState.WORK -> {
                    workout_state_tv.text = ""
                    command_name_tv.visibility = VISIBLE
                    play_command_lottie.visibility = VISIBLE
                    countdown_pb.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                }
                RandomWorkoutState.REST -> {
                    workout_state_tv.text = it.toString()
                    command_name_tv.visibility = INVISIBLE
                    play_command_lottie.visibility = VISIBLE
                    countdown_pb.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorPrimary
                        )
                    )
                }

                RandomWorkoutState.COMPLETE -> {
                    workout_state_tv.text = it.toString()
                    handlePlayAnimationLottie(true)
                    mediaPlayer.stop()
//                    WorkoutCompleteDialog(
//                        viewModel.totalWorkoutSecs,
//                        0,
//                        {
//                            initRoundProgressView()
//                            viewModel.onRestart()
//                        },
//                        { findNavController().popBackStack() }).show(childFragmentManager, null)
                }
            }
        })

        viewModel.currentCommandLD.observe(viewLifecycleOwner, Observer {
            command_name_tv.text = it.name
            play_command_lottie.playAnimation()
        })
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
//            viewModel.onNext()
        }

        previous_btn.setOnClickListener {
//            viewModel.onPrevious()
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



//    private fun initRoundProgressView() {
//        round_progress_ll.removeAllViews()
//
//        val params = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            1.0f
//        )
//        params.marginEnd = 10
//
//        repeat(viewModel.getTotalRounds()) {
//            val seekBar = SeekBar(requireContext())
//            val thumb = ShapeDrawable(OvalShape())
//            thumb.setTint(ContextCompat.getColor(requireContext(), R.color.white))
//            thumb.intrinsicHeight = 20
//            thumb.intrinsicWidth = 8
//            seekBar.setPadding(0, 0, 0, 0)
//            seekBar.thumb = thumb
//            seekBar.layoutParams = params
//            seekBar.max = viewModel.getCountdownProgressBarMax(RandomWorkoutState.WORK)
//            seekBar.scaleY = 12f
//            seekBar.progress = 0
//            seekBar.progressTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.red
//                )
//            )
//            round_progress_ll.addView(seekBar)
//            seekBar.thumb.mutate().alpha = 0
//        }
//    }

//    private fun sendCommandToService(action: String) {
//        Intent(requireContext(), WorkoutService::class.java).also {
//            it.action = action
//            requireContext().startService(it)
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        viewModel.onPause()
//        sendCommandToService(ACTION_STOP_SERVICE)
//    }

}