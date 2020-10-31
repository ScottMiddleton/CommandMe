package com.middleton.scott.customboxingworkout.ui.combinations

import SaveCombinationDialog
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import com.middleton.scott.customboxingworkout.utils.MediaRecorderManager
import com.middleton.scott.customboxingworkout.utils.PermissionsDialogManager
import kotlinx.android.synthetic.main.fragment_combinations.*
import org.koin.android.ext.android.inject
import java.io.File

class CombinationsScreen : BaseFragment() {
    private val viewModel: CombinationsViewModel by inject()
    private var mediaRecorder = MediaRecorder()

    private lateinit var adapter: CombinationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_combinations, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.audioFileBaseDirectory = context?.getExternalFilesDir(null)?.absolutePath + "/"
        adapter = CombinationsAdapter(
            viewModel.audioFileBaseDirectory,
            parentFragmentManager,
            onEditCombination = {
                viewModel.upsertCombination(it)
            },
            onDeleteCombination = {
                val file = File(viewModel.audioFileCompleteDirectory)
                file.delete()
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        combinations_RV.adapter = adapter
        subscribeUI()
        setClickListeners()
    }

    private fun subscribeUI() {
        viewModel.getAllCombinationsLD().observe(viewLifecycleOwner, Observer {
            if (!viewModel.listAnimationShownOnce) {
                val controller =
                    AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
                combinations_RV.layoutAnimation = controller
                viewModel.listAnimationShownOnce = true
            }
            adapter.setAdapter(it, null)
        })
    }

    private fun setClickListeners() {
        record_audio_button.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        context?.let { context ->
                            activity?.let { activity ->
                                PermissionsDialogManager.handlePermissionsDialog(
                                    context,
                                    activity
                                ) { permissionsGranted ->
                                    if (permissionsGranted) {
                                        handleRecordAudioAnimations(true)
                                        viewModel.audioFileBaseDirectory =
                                            context.getExternalFilesDir(null)?.absolutePath + "/"
                                        startRecording()
                                    }
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        stopRecording()
                        handleRecordAudioAnimations(false)
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })
    }

    private fun startRecording() {
        viewModel.recording = true
        viewModel.setAudioFileOutput(System.currentTimeMillis())
        MediaRecorderManager.startRecording(
            mediaRecorder,
            viewModel.audioFileCompleteDirectory
        )
    }

    private fun stopRecording() {
        if (viewModel.recording) {
            MediaRecorderManager.stopRecording(mediaRecorder) { recordingComplete ->
                if (recordingComplete) {
                    showSaveCombinationDialog()
                } else {
                    Toast.makeText(context, "Recording too short", Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.recording = false
        }
    }

    private fun showSaveCombinationDialog() {
        SaveCombinationDialog(
            false,
            Combination("", 0, viewModel.audioFileName),
            { combination ->
                viewModel.upsertCombination(combination)
            }, {
                val file = File(viewModel.audioFileCompleteDirectory)
                file.delete()
            }).show(childFragmentManager, "")
    }

    private fun handleRecordAudioAnimations(recording: Boolean) {
        if (recording) {
            lottie_anim_left.playAnimation()
            lottie_anim_right.playAnimation()
            lottie_anim_left.visibility = View.VISIBLE
            lottie_anim_right.visibility = View.VISIBLE
        } else {
            lottie_anim_left.pauseAnimation()
            lottie_anim_right.pauseAnimation()
            lottie_anim_left.visibility = GONE
            lottie_anim_right.visibility = GONE
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }
}