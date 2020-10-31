package com.middleton.scott.customboxingworkout.ui.combinations

import SaveCombinationDialog
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var recordButtonAnimation: Animation
    private lateinit var recordButtonAnimationReverse: Animation

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

        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(combinations_RV)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recordButtonAnimation = AnimationUtils.loadAnimation(this.context, R.anim.button_scale)
        recordButtonAnimationReverse = AnimationUtils.loadAnimation(this.context, R.anim.button_scale_reverse)
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
        record_audio_button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    viewModel.audioFileBaseDirectory =
                        context?.getExternalFilesDir(null)?.absolutePath + "/"
                    context?.let { context ->
                        activity?.let { activity ->
                            PermissionsDialogManager.handlePermissionsDialog(
                                context,
                                activity
                            ) { permissionsGranted ->
                                if (permissionsGranted) {
                                    record_audio_button.startAnimation(recordButtonAnimation)
                                    Handler().postDelayed({
                                        handleRecordAudioAnimations(true)
                                    }, 250)
                                    startRecording()
                                }
                            }
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    handleRecordAudioAnimations(false)
                    record_audio_button.startAnimation(recordButtonAnimationReverse)
                    stopRecording()
                }
            }
            true
        }
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