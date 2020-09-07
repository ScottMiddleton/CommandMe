package com.middleton.scott.customboxingworkout.ui.createworkout

import SaveCombinationDialog
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsAdapter
import kotlinx.android.synthetic.main.fragment_combinations_screen.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.File
import java.io.IOException

class CombinationsTabFragment : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }
    private var mediaRecorder = MediaRecorder()
    private lateinit var adapter: CombinationsAdapter

    companion object {
        fun newInstance() =
            CombinationsTabFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CombinationsAdapter(viewModel.audioFileDirectory) { combination, checked ->
            viewModel.setCombination(combination, checked)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_combinations_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.audioFileDirectory = context?.getExternalFilesDir(null)?.absolutePath + "/"
        combinations_RV.adapter = adapter
        subscribeUI()
        setClickListeners()
    }

    private fun subscribeUI() {
        viewModel.workoutWithCombinationsLD.observe(viewLifecycleOwner, Observer {
            it?.combinations?.let { it1 -> adapter.setCheckedCombinations(it1) }
        })

        viewModel.getAllCombinations().observe(viewLifecycleOwner, Observer {
            adapter.setAdapter(it)
            val controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
            combinations_RV.layoutAnimation = controller
            combinations_RV.adapter?.notifyDataSetChanged()
            combinations_RV.scheduleLayoutAnimation()
        })
    }

    private fun setClickListeners() {
        record_audio_button.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        context?.let {
                            if (ContextCompat.checkSelfPermission(
                                    it,
                                    Manifest.permission.RECORD_AUDIO
                                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                    it,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                val permissions = arrayOf(
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                )
                                activity?.let { it1 ->
                                    ActivityCompat.requestPermissions(
                                        it1,
                                        permissions,
                                        0
                                    )
                                }
                            } else {
                                lottie_anim_left.playAnimation()
                                lottie_anim_right.playAnimation()
                                lottie_anim_left.visibility = View.VISIBLE
                                lottie_anim_right.visibility = View.VISIBLE
                                startRecording()
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        stopRecording()
                        lottie_anim_left.pauseAnimation()
                        lottie_anim_right.pauseAnimation()
                        lottie_anim_left.visibility = View.GONE
                        lottie_anim_right.visibility = View.GONE
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })
    }

    private fun startRecording() {
        try {
            viewModel.recording = true
            val filename = "audio_" + System.currentTimeMillis().toString() + ".mp3"
            viewModel.filename = filename
            viewModel.audioFileOutput = viewModel.audioFileDirectory + filename
            mediaRecorder = MediaRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            mediaRecorder.setOutputFile(viewModel.audioFileOutput)
            mediaRecorder.prepare()
            mediaRecorder.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (viewModel.recording) {
            try {
                mediaRecorder.stop()
                mediaRecorder.release()
                showSaveCombinationDialog()
            } catch (stopException: RuntimeException) {
                Toast.makeText(context, "Recording too short", Toast.LENGTH_SHORT).show()
            }
            viewModel.recording = false
        }
    }

    private fun showSaveCombinationDialog() {
        SaveCombinationDialog({ name ->
            viewModel.upsertCombination(name)
        }, {
            viewModel.filename = ""
            val file = File(viewModel.audioFileOutput)
            file.delete()
        }).show(childFragmentManager, null)
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }
}