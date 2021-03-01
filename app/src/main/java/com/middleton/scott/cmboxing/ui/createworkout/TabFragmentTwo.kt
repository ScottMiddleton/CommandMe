package com.middleton.scott.cmboxing.ui.createworkout

import AddCommandDialog
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.other.Constants
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.ui.commands.CommandsAdapter
import com.middleton.scott.cmboxing.utils.DialogManager
import com.middleton.scott.cmboxing.utils.MediaRecorderManager
import kotlinx.android.synthetic.main.fragment_commands.*
import kotlinx.android.synthetic.main.fragment_commands.empty_list_layout
import kotlinx.android.synthetic.main.fragment_commands.fab_tv
import kotlinx.android.synthetic.main.fragment_commands.nested_scroll_view
import kotlinx.android.synthetic.main.fragment_commands.next_btn_include
import kotlinx.android.synthetic.main.fragment_commands.undo_btn
import kotlinx.android.synthetic.main.fragment_commands.undo_tv
import kotlinx.android.synthetic.main.fragment_my_workouts.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.File

class TabFragmentTwo : BaseFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CreateWorkoutSharedViewModel>() }
    private var mediaRecorder = MediaRecorder()
    var combinationsEmpty = true
    var undoSnackbarVisible = false
    var recordingEnabled = false

    private lateinit var adapter: CommandsAdapter

    companion object {
        fun newInstance() =
            TabFragmentTwo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleFab()
        recordingEnabled = checkPermissions()
        adapter = CommandsAdapter(
            viewModel.audioFileBaseDirectory,
            parentFragmentManager,
            { selectedCombinationCrossRef, isChecked ->
                if (isChecked) {
                    viewModel.addSelectedCommand(selectedCombinationCrossRef)
                } else {
                    viewModel.removeSelectedCommand(selectedCombinationCrossRef)
                }
                viewModel.validateTabTwo()
            },
            {
                viewModel.upsertCommand(it)
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
        return inflater.inflate(R.layout.fragment_commands, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        commands_RV.adapter = adapter

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

        viewModel.audioFileBaseDirectory =
            context?.getExternalFilesDir(null)?.absolutePath + "/"

        viewModel.subscribeLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                subscribeUI()
            }
        })

        setClickListeners()
    }

    private fun subscribeUI() {
        viewModel.getAllCommandsLD().observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                combinationsEmpty = true
                commands_RV.visibility = View.GONE
                if (!undoSnackbarVisible) {
                    empty_list_layout.visibility = View.VISIBLE
                }
            } else {
                combinationsEmpty = false
                empty_list_layout.visibility = View.GONE
                commands_RV.visibility = View.VISIBLE
                if (!viewModel.listAnimationShownOnce) {
                    val controller =
                        AnimationUtils.loadLayoutAnimation(
                            context,
                            R.anim.layout_animation_fall_down
                        )
                    commands_RV.layoutAnimation = controller
                    viewModel.listAnimationShownOnce = true
                }
            }
            adapter.setAdapter(it, viewModel.selectedCommands)
        })

        viewModel.tabTwoValidatedLD.observe(viewLifecycleOwner, Observer {
            if (!it) {
                if (viewModel.userHasAttemptedToProceedTwo) {
                    DialogManager.showDialog(
                        context = requireContext(),
                        messageId = R.string.add_commands_dialog_message,
                        negativeBtnTextId = R.string.add_command,
                        negativeBtnClick = {})
                    viewModel.userHasAttemptedToProceedTwo = false
                }
            }
        })
    }

    private fun setClickListeners() {
        next_btn_include.findViewById<Button>(R.id.next_btn).setOnClickListener {
            viewModel.userHasAttemptedToProceedTwo = true
            if (viewModel.tabTwoValidatedLD.value == true) {
                val viewPager =
                    parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                viewPager.currentItem = 2
            } else {
                viewModel.validateTabTwo()
            }
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
                    if (viewModel.timeSwapBuff > 500) {
                        showSaveCombinationDialog()
                    } else {
                        val file = File(viewModel.audioFileCompleteDirectory)
                        file.delete()
                        Toast.makeText(
                            context,
                            "Recording too short. Hold the microphone to record a combination command.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Recording too short. Hold the microphone to record a combination command.",
                        Toast.LENGTH_LONG
                    ).show()
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
        AddCommandDialog(
            viewModel.audioFileCompleteDirectory,
            false,
            Command("", 0, viewModel.audioFileName),
            { combination ->
                viewModel.upsertCommand(combination)
            }, {
                val file = File(viewModel.audioFileCompleteDirectory)
                file.delete()
            }).show(childFragmentManager, "")
    }



    private fun handleFab() {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        params.setMargins(80, 80, 80, 80)
        add_command_btn.layoutParams = params

        nested_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            when {
                scrollY > oldScrollY -> {
                    fab_tv.visibility = View.GONE
                    val params1 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.END
                    }
                    params.setMargins(80, 80, 80, 80)
                    add_workouts_btn.layoutParams = params1
                }
                scrollX == scrollY -> {
                    fab_tv.visibility = View.VISIBLE
                    val params2 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                    params.setMargins(80, 80, 80, 80)
                    add_workouts_btn.layoutParams = params2

                }
                else -> {
                    fab_tv.visibility = View.VISIBLE
                    val params3 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                    params.setMargins(80, 80, 80, 80)
                    add_workouts_btn.layoutParams = params3
                }
            }

        })
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }
}