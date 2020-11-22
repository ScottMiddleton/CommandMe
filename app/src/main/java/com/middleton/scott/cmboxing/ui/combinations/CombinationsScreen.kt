package com.middleton.scott.cmboxing.ui.combinations

import SaveCombinationDialog
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Combination
import com.middleton.scott.cmboxing.other.Constants.REQUEST_AUDIO_PERMISSION_CODE
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.utils.MediaRecorderManager
import com.middleton.scott.cmboxing.utils.PermissionsDialogManager
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_combinations.*
import org.koin.android.ext.android.inject
import java.io.File

class CombinationsScreen : BaseFragment() {
    private val viewModel: CombinationsViewModel by inject()
    private var mediaRecorder = MediaRecorder()
    var combinationsEmpty = true
    var undoSnackbarVisible = false

    private val handler = Handler()

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

        setClickListeners()

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
                        undo_btn?.visibility = GONE
                        undo_tv?.visibility = GONE
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

    }

    private fun subscribeUI() {
        viewModel.getAllCombinationsLD().observe(viewLifecycleOwner, {
            if (it.isNullOrEmpty()) {
                combinationsEmpty = true
                combinations_RV.visibility = GONE
                if (!undoSnackbarVisible) {
                    empty_list_layout.visibility = View.VISIBLE
                }
            } else {
                combinationsEmpty = false
                empty_list_layout.visibility = GONE
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
            adapter.setAdapter(it, null)
        })
    }

    private fun setClickListeners() {

        undo_btn.setOnClickListener {
            undo_btn.visibility = GONE
            undo_tv.visibility = GONE
            viewModel.undoPreviouslyDeletedCombination()
        }

        record_audio_button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (checkPermissions()) {
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
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {
                    Toast.makeText(requireContext(), "Recording enabled.", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Recording and saving audio permissions have been denied. These both must be granted to record audio.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            WRITE_EXTERNAL_STORAGE
        )
        val result1 = ContextCompat.checkSelfPermission(requireContext(), RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE
        )
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