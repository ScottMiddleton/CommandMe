package com.middleton.scott.customboxingworkout.ui.createworkout.combinations

import SaveCombinationDialog
import android.graphics.Canvas
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import com.middleton.scott.customboxingworkout.ui.combinations.CombinationsAdapter
import com.middleton.scott.customboxingworkout.ui.createworkout.CreateWorkoutSharedViewModel
import com.middleton.scott.customboxingworkout.utils.MediaRecorderManager
import com.middleton.scott.customboxingworkout.utils.PermissionsDialogManager
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_combinations.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.File

class CreateWorkoutCombinationsFragment : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }
    private var mediaRecorder = MediaRecorder()
    private lateinit var recordButtonAnimation: Animation
    private lateinit var recordButtonAnimationReverse: Animation

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

        combinations_RV.adapter = adapter

        recordButtonAnimation = AnimationUtils.loadAnimation(this.context, R.anim.button_scale)
        recordButtonAnimationReverse =
            AnimationUtils.loadAnimation(this.context, R.anim.button_scale_reverse)
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
                    val position = viewHolder.adapterPosition
                    val combination = viewModel.deleteCombination(position)

                    Snackbar.make(
                        combinations_RV,
                        getString(R.string.deleted_snackbar, combination.name),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(getString(R.string.undo)) {
                            viewModel.undoPreviouslyDeletedCombination()
                        }.addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                if (event != DISMISS_EVENT_ACTION) {
                                    viewModel.deleteWorkoutCombinations()
                                }
                            }
                        }).show()
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
                empty_list_layout.visibility = View.VISIBLE
                combinations_RV.visibility = View.GONE
            } else {
                empty_list_layout.visibility = View.GONE
                combinations_RV.visibility = View.VISIBLE
                if (!viewModel.listAnimationShownOnce) {
                    val controller =
                        AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
                    combinations_RV.layoutAnimation = controller
                    viewModel.listAnimationShownOnce = true
                }
            }
            adapter.setAdapter(it, null)
        })
    }

    private fun setClickListeners() {
        record_audio_button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (viewModel.permissionsGranted) {
                        record_audio_button.startAnimation(recordButtonAnimation)
                        handleRecordAudioAnimations(true)
                        startRecording()
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
            lottie_anim_left.visibility = View.GONE
            lottie_anim_right.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }
}