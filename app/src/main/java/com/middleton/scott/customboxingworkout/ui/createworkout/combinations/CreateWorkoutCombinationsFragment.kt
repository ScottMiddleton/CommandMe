package com.middleton.scott.customboxingworkout.ui.createworkout.combinations

import SaveCombinationDialog
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsAdapter
import com.middleton.scott.customboxingworkout.ui.createworkout.CreateWorkoutSharedViewModel
import com.middleton.scott.customboxingworkout.utils.MediaRecorderManager
import com.middleton.scott.customboxingworkout.utils.PermissionsDialogManager
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_combinations.*
import kotlinx.android.synthetic.main.fragment_combinations.empty_list_layout
import kotlinx.android.synthetic.main.fragment_combinations.undo_btn
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.File

class CreateWorkoutCombinationsFragment : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }
    private var mediaRecorder = MediaRecorder()
    var combinationsEmpty = true
    var undoSnackbarVisible = false

    private val handler = Handler()

    private lateinit var adapter: CombinationsAdapter

    companion object {
        fun newInstance() =
            CreateWorkoutCombinationsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CombinationsAdapter(
            viewModel.audioFileBaseDirectory,
            parentFragmentManager,
            { selectedCombinationCrossRef, isChecked ->
                if (isChecked) {
                    viewModel.addCombination(selectedCombinationCrossRef)
                } else {
                    viewModel.removeCombination(selectedCombinationCrossRef)
                }
            },
            {
                viewModel.upsertCombination(it)
            }, {
                val file = File(viewModel.audioFileCompleteDirectory)
                file.delete()
            })
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

        undo_btn.setOnClickListener {
            undo_btn?.visibility = View.GONE
            undo_tv.visibility = View.GONE
            viewModel.undoPreviouslyDeletedCombination()
        }


        combinations_RV.adapter = adapter

        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    undo_btn.visibility = View.VISIBLE
                    undo_tv.visibility = View.VISIBLE
                    undoSnackbarVisible = true

                    val position = viewHolder.adapterPosition
                    val combination = viewModel.deleteCombination(position)

                    undo_tv.text = getString(R.string.deleted_snackbar, combination.name)

                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        undoSnackbarVisible = false
                        undo_btn?.visibility = View.GONE
                        undo_tv?.visibility = View.GONE
                        if (combinationsEmpty) {
                            empty_list_layout?.visibility = View.VISIBLE
                        }
                    }, 3000)
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )

                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(), R.color.red
                            )
                        )
                        .addActionIcon(R.drawable.ic_delete_sweep)
                        .create()
                        .decorate()
                }
            }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(combinations_RV)

        subscribeUI()

        viewModel.audioFileBaseDirectory =
            context?.getExternalFilesDir(null)?.absolutePath + "/"

        context?.let { context ->
            activity?.let { activity ->
                PermissionsDialogManager.handlePermissionsDialog(
                    context,
                    activity
                ) { permissionsGranted ->
                    if (permissionsGranted) {
                        viewModel.permissionsGranted = true
                    }
                }
            }
        }

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
            adapter.setAdapter(it, viewModel.selectedCombinations)
        })
    }

    private fun setClickListeners() {
        record_audio_button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (viewModel.permissionsGranted) {
                        val yourColor = ContextCompat.getColor(requireContext(), R.color.red)
                        val filter = SimpleColorFilter(yourColor)
                        val keyPath = KeyPath("**")
                        val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(filter)
                        record_audio_button.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
                        startRecording()
                        handleRecordAudioAnimations(true)

                    }
                }

                MotionEvent.ACTION_UP -> {
                    stopRecording()
                    val yourColor = ContextCompat.getColor(requireContext(), R.color.transparent)
                    val filter = SimpleColorFilter(yourColor)
                    val keyPath = KeyPath("**")
                    val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(filter)
                    record_audio_button.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
                    handleRecordAudioAnimations(false)
                }
            }
            true
        }
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
                    mediaRecorder = MediaRecorder()
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