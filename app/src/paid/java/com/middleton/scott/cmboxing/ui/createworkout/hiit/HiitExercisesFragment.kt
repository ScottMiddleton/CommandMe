package com.middleton.scott.cmboxing.ui.createworkout.hiit

import SaveCombinationDialog
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ColorFilter
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Combination
import com.middleton.scott.cmboxing.other.Constants
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.ui.combinations.CommandsAdapter
import com.middleton.scott.cmboxing.utils.MediaRecorderManager
import kotlinx.android.synthetic.main.fragment_combinations.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.File

class HiitExercisesFragment : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateHiitWorkoutSharedViewModel>() }
    private var mediaRecorder = MediaRecorder()
    var combinationsEmpty = true
    var undoSnackbarVisible = false
    var recordingEnabled = false

    private val handler = Handler()
    private lateinit var adapter: CommandsAdapter

    companion object {
        fun newInstance() =
            HiitExercisesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recordingEnabled = checkPermissions()
//        adapter = CombinationsAdapter(
//            viewModel.audioFileBaseDirectory,
//            parentFragmentManager,
//            { selectedCombinationCrossRef, isChecked ->
//                if (isChecked) {
//                    viewModel.addHiitExercises(selectedCombinationCrossRef)
//                } else {
//                    viewModel.removeCombination(selectedCombinationCrossRef)
//                }
//            },
//            {
//                viewModel.upsertCombination(it)
//            }, {
//                val file = File(viewModel.audioFileCompleteDirectory)
//                file.delete()
//            })
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

//        val itemTouchHelperCallback =
//            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
//                override fun onMove(
//                    recyclerView: RecyclerView,
//                    viewHolder: RecyclerView.ViewHolder,
//                    target: RecyclerView.ViewHolder
//                ): Boolean {
//                    return true
//                }
//
//                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                    var swipedComboIsChecked = false
//                    adapter.selectedCombinations.forEach {
//                        if (it == viewModel.allCombinations[viewHolder.adapterPosition]) {
//                            swipedComboIsChecked = true
//                        }
//                    }
//                    if (swipedComboIsChecked) {
//                        adapter.notifyDataSetChanged()
//                        Toast.makeText(requireContext(), getString(R.string.checked_combos_cannot_be_deleted), LENGTH_LONG).show()
//                    } else {
//                        undo_btn.visibility = View.VISIBLE
//                        undo_tv.visibility = View.VISIBLE
//                        undoSnackbarVisible = true
//
//                        val position = viewHolder.adapterPosition
//                        val combination = viewModel.deleteCombination(position)
//                        viewModel.addDeletedCombinationID(combination.id)
//
//                        undo_tv.text = getString(R.string.deleted_snackbar, combination.name)
//
//                        handler.removeCallbacksAndMessages(null)
//                        handler.postDelayed({
//                            val file = File(viewModel.audioFileBaseDirectory + combination.file_name)
//                            file.delete()
//                            undoSnackbarVisible = false
//                            undo_btn?.visibility = View.GONE
//                            undo_tv?.visibility = View.GONE
//                            if (combinationsEmpty) {
//                                empty_list_layout?.visibility = View.VISIBLE
//                            }
//                        }, 3000)
//                    }
//                }
//
//                override fun onChildDraw(
//                    c: Canvas,
//                    recyclerView: RecyclerView,
//                    viewHolder: RecyclerView.ViewHolder,
//                    dX: Float,
//                    dY: Float,
//                    actionState: Int,
//                    isCurrentlyActive: Boolean
//                ) {
//                    super.onChildDraw(
//                        c,
//                        recyclerView,
//                        viewHolder,
//                        dX,
//                        dY,
//                        actionState,
//                        isCurrentlyActive
//                    )
//
//                    RecyclerViewSwipeDecorator.Builder(
//                        c,
//                        recyclerView,
//                        viewHolder,
//                        dX,
//                        dY,
//                        actionState,
//                        isCurrentlyActive
//                    )
//                        .addBackgroundColor(
//                            ContextCompat.getColor(
//                                requireContext(), R.color.red
//                            )
//                        )
//                        .addActionIcon(R.drawable.ic_delete_sweep)
//                        .create()
//                        .decorate()
//                }
//            }
//
//        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
//        itemTouchHelper.attachToRecyclerView(combinations_RV)

        subscribeUI()

        viewModel.audioFileBaseDirectory =
            context?.getExternalFilesDir(null)?.absolutePath + "/"

        setClickListeners()
    }

    private fun subscribeUI() {
        viewModel.getAllCombinationsLD().observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                combinationsEmpty = true
                combinations_RV.visibility = View.GONE
                if (!undoSnackbarVisible) {
                    empty_list_layout.visibility = View.VISIBLE
                }
            } else {
                combinationsEmpty = false
                empty_list_layout.visibility = View.GONE
                combinations_RV.visibility = View.VISIBLE
                if (!viewModel.listAnimationShownOnce) {
                    val controller =
                        AnimationUtils.loadLayoutAnimation(
                            context,
                            R.anim.layout_animation_fall_down
                        )
                    combinations_RV.layoutAnimation = controller
                    viewModel.listAnimationShownOnce = true
                }
            }
//            adapter.setAdapter(it, viewModel.selectedExercises)
        })

        viewModel.selectedCombinationsLD.observe(viewLifecycleOwner, {})
    }

    private fun setClickListeners() {
        record_audio_button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (recordingEnabled) {
                        val yourColor = ContextCompat.getColor(requireContext(), R.color.red)
                        val filter = SimpleColorFilter(yourColor)
                        val keyPath = KeyPath("**")
                        val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(filter)
                        record_audio_button.addValueCallback(
                            keyPath,
                            LottieProperty.COLOR_FILTER,
                            callback
                        )
                        startRecording()
                        handleRecordAudioAnimations(true)
                    } else {
                        requestPermission()
                    }
                }

                MotionEvent.ACTION_UP -> {
                    stopRecording()
                    val yourColor = ContextCompat.getColor(requireContext(), R.color.transparent)
                    val filter = SimpleColorFilter(yourColor)
                    val keyPath = KeyPath("**")
                    val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(filter)
                    record_audio_button.addValueCallback(
                        keyPath,
                        LottieProperty.COLOR_FILTER,
                        callback
                    )
                    handleRecordAudioAnimations(false)
                }
            }
            true
        }

        undo_btn.setOnClickListener {
            undo_btn?.visibility = View.GONE
            undo_tv.visibility = View.GONE
            viewModel.undoPreviouslyDeletedCombination()
        }
    }

    private fun startRecording() {
        viewModel.resetRecordingTimer()
        viewModel.recording = true
        viewModel.setAudioFileOutput(System.currentTimeMillis())
        MediaRecorderManager.startRecording(
            mediaRecorder,
            viewModel.audioFileCompleteDirectory
        )
        viewModel.startHTime = SystemClock.uptimeMillis();
        viewModel.customHandler.postDelayed(viewModel.updateTimerThread, 0);
    }

    private fun stopRecording() {
        if (viewModel.recording) {
            MediaRecorderManager.stopRecording(mediaRecorder) { recordingComplete ->
                if (recordingComplete) {
                    viewModel.timeSwapBuff += viewModel.timeInMilliseconds
                    viewModel.customHandler.removeCallbacks(viewModel.updateTimerThread)
                    if(viewModel.timeSwapBuff > 500){
                        showSaveCombinationDialog()
                    } else {
                        val file = File(viewModel.audioFileCompleteDirectory)
                        file.delete()
                        Toast.makeText(context, "Recording too short. Hold the microphone to record a combination command.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Recording too short. Hold the microphone to record a combination command.", Toast.LENGTH_LONG).show()
                    mediaRecorder = MediaRecorder()
                }
            }
            viewModel.recording = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {
                    recordingEnabled = true
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.recording_enabled),
                        Toast.LENGTH_LONG
                    )
                        .show()
                } else {
                    recordingEnabled = false
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.recording_denied),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val result1 = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        )
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            Constants.REQUEST_AUDIO_PERMISSION_CODE
        )
    }

    private fun showSaveCombinationDialog() {
        SaveCombinationDialog(
            viewModel.audioFileCompleteDirectory,
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
            record_audio_button.playAnimation()
        } else {
            record_audio_button.cancelAnimation()
            record_audio_button.progress = 0.08f
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }
}