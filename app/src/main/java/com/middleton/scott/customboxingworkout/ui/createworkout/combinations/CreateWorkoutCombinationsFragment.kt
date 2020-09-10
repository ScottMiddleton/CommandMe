package com.middleton.scott.customboxingworkout.ui.createworkout.combinations

import SaveCombinationDialog
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsAdapter
import com.middleton.scott.customboxingworkout.ui.createworkout.CreateWorkoutSharedViewModel
import com.middleton.scott.customboxingworkout.utils.MediaRecorderManager
import com.middleton.scott.customboxingworkout.utils.PermissionsDialogManager
import kotlinx.android.synthetic.main.fragment_combinations.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.File

class CreateWorkoutCombinationsFragment : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }
    private var mediaRecorder = MediaRecorder()

    private lateinit var adapter: CombinationsAdapter

    companion object {
        fun newInstance() =
            CreateWorkoutCombinationsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CombinationsAdapter(viewModel.audioFileBaseDirectory, viewModel.workoutId) { workoutCombination, isChecked ->
            viewModel.setCombination(workoutCombination, isChecked)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_combinations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        combinations_RV.adapter = adapter
        subscribeUI()
        setClickListeners()
    }

    private fun subscribeUI() {
//        viewModel.workoutWithCombinationsLD.observe(viewLifecycleOwner, Observer {
//            it?.combinations?.let { it1 -> adapter.setCheckedCombinations(it1) }
//        })

        viewModel.getAllCombinationsLD().observe(viewLifecycleOwner, Observer {
            adapter.setAdapter(it, viewModel.workoutCombinations)
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
        MediaRecorderManager.startRecording(mediaRecorder, viewModel.audioFileCompleteDirectory)
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
        SaveCombinationDialog({ name ->
            viewModel.upsertCombination(name)
        }, {
            val file = File(viewModel.audioFileCompleteDirectory)
            file.delete()
        }).show(childFragmentManager, null)
    }

    private fun handleRecordAudioAnimations(recording: Boolean){
        if(recording){
            lottie_anim_left.playAnimation()
            lottie_anim_right.playAnimation()
            lottie_anim_left.visibility = View.VISIBLE
            lottie_anim_right.visibility = View.VISIBLE
        } else {
            lottie_anim_left.pauseAnimation()
            lottie_anim_right.pauseAnimation()
            lottie_anim_left.visibility = View.GONE
            lottie_anim_right.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }
}