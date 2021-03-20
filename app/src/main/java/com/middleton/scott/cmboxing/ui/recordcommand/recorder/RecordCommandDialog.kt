package com.middleton.scott.cmboxing.ui.recordcommand.recorder

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.createworkout.NumberPickerMinutesSecondsDialog
import com.middleton.scott.cmboxing.utils.*
import com.middleton.scott.cmboxing.utils.DateTimeUtils.formatAsTime
import kotlinx.android.synthetic.main.appbar_record_command.*
import kotlinx.android.synthetic.main.fragment_record_command.*
import kotlinx.android.synthetic.main.include_layout_save_btn.view.*
import kotlinx.android.synthetic.main.include_play_recording.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.sqrt


const val REQUEST_ID_MULTIPLE_PERMISSIONS = 1

class RecordCommandDialog(commandId: Long) : DialogFragment() {
    private val viewModel: RecordCommandViewModel by viewModel {
        parametersOf(
            commandId
        )
    }

    private lateinit var recorder: Recorder
    private lateinit var player: AudioPlayer
    private var recordingEnabled = false
    private lateinit var mContext: Context

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)



        // creating the fullscreen dialog
        val dialog = Dialog(MainActivity.instance)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // If it is a new command and back is press delete the recording
                    viewModel.deleteRecordings(false)
                    dismiss()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_command, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        mContext = view.context
        if (checkAndRequestPermissions()) {
            recordingEnabled = true
        }
        initRecorderUI()
        setClickListeners()

        viewModel.saveButtonEnabledLD.observe(viewLifecycleOwner, {
            save_btn_include.save_btn.isEnabled = it
        })

        viewModel.saveCompleteLD.observe(viewLifecycleOwner, {
            if (it) {
                dismiss()
            }
        })

        viewModel.commandLD.observe(viewLifecycleOwner, {
            name_et.setText(it.name)
            time_to_complete_et.setText(DateTimeUtils.toMinuteSeconds(it.timeToCompleteSecs))
            initAudioPlayer()
            initPlayerUI()
            player_visualizer.visibility = VISIBLE
            record_audio_button.visibility = INVISIBLE
            recorder_visualizer.visibility = INVISIBLE
            include_play_recording.visibility = VISIBLE
            delete_recording_btn.visibility = VISIBLE
            viewModel.hasAudioRecording = true
            viewModel.validate()
        })

        viewModel.isEditModeLD.observe(viewLifecycleOwner, {
            if (!it) {
                initAudioRecorder()
            }
        })
    }

    private fun checkAndRequestPermissions(): Boolean {
        val recordAudioPermission = checkSelfPermission(
            mContext,
            RECORD_AUDIO
        )
        val writeExternalPermission = checkSelfPermission(mContext, WRITE_EXTERNAL_STORAGE)

        val listPermissionsNeeded: MutableList<String> = ArrayList()

        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(RECORD_AUDIO)
        }

        if (writeExternalPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            requestPermissions(
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {
                val perms: MutableMap<String, Int> = HashMap()
                // Initialize the map with both permissions
                perms[RECORD_AUDIO] = PackageManager.PERMISSION_GRANTED
                perms[WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    var i = 0
                    while (i < permissions.size) {
                        perms[permissions[i]] = grantResults[i]
                        i++
                    }
                    // Check for both permissions
                    if (perms[RECORD_AUDIO] == PackageManager.PERMISSION_GRANTED
                        && perms[WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permissions granted
                        // process the normal flow
                        recordingEnabled = true
                    } else {
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        // shouldShowRequestPermissionRationale will return true
                        // show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(),
                                RECORD_AUDIO
                            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(),
                                WRITE_EXTERNAL_STORAGE
                            )
                        ) {
                            DialogManager.showDialog(
                                context = mContext,
                                titleId = R.string.permissions_required,
                                messageId = R.string.permissions_dialog_message,
                                positiveBtnClick = { checkAndRequestPermissions() },
                                negativeBtnClick = {})
                        } else {
                            Toast.makeText(
                                mContext,
                                getString(R.string.go_to_settings_to_enable_permissions),
                                Toast.LENGTH_LONG
                            )
                                .show()
                            recordingEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun setClickListeners() {
        save_btn_include.save_btn.setOnClickListener {
            viewModel.deleteRecordings(true)
            viewModel.upsertCommand()
        }

        delete_recording_btn.setOnClickListener {
            viewModel.recordFileNamesToBeDeleted.add(viewModel.recordFileName)
            initAudioRecorder()
            viewModel.validate()
        }

        close_btn?.setOnClickListener {
            viewModel.deleteRecordings(false)
            dismiss()
        }

        time_to_complete_et.setOnClickListener {
            val imm: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)

            var secs = 10
            if (!time_to_complete_et.text.isNullOrBlank()) {
                secs = viewModel.timeToCompleteSecs
            }

            NumberPickerMinutesSecondsDialog(
                getString(R.string.default_time_allocated),
                secs,
                { newSecs ->
                    viewModel.timeToCompleteSecs = newSecs
                    viewModel.validate()
                    time_to_complete_et.setText(DateTimeUtils.toMinuteSeconds(newSecs))
                    name_et.clearFocus()
                    if (viewModel.timeToCompleteSecs <= 0) {
                        time_to_complete_til.error = getString(R.string.greater_than_zero)
                    } else {
                        time_to_complete_til.isErrorEnabled = false
                    }
                },
                {

                }).show(childFragmentManager, "")
        }

        name_et.doAfterTextChanged {
            viewModel.name = it.toString()
            viewModel.validate()
        }
    }

    override fun onStop() {
        try {
            player.release()
            recorder.release()
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
        super.onStop()
    }

    private fun initRecorderUI() {
        record_audio_button.setOnClickListener {
            if (recordingEnabled) {
                recorder.toggleRecording()
            } else {
                checkAndRequestPermissions()
            }
        }
        recorder_visualizer.ampNormalizer = { sqrt(it.toFloat()).toInt() }
    }

    private fun initPlayerUI() {
        player_visualizer.apply {
            ampNormalizer = { sqrt(it.toFloat()).toInt() }
            onStartSeeking = {
                player.pause()
            }
            onSeeking = { timeline_tv.text = it.formatAsTime() }
            onFinishedSeeking = { time, isPlayingBefore ->
                player.seekTo(time)
                if (isPlayingBefore) {
                    player.resume()
                }
            }
            onAnimateToPositionFinished = { time, isPlaying ->
                updateTime(time, isPlaying)
                player.seekTo(time)
            }
        }

        play_button.setOnClickListener { player.togglePlay() }
        seek_forward_button.setOnClickListener { player_visualizer.seekOver(SEEK_OVER_AMOUNT) }
        seek_backward_button.setOnClickListener { player_visualizer.seekOver(-SEEK_OVER_AMOUNT) }

        lifecycleScope.launchWhenCreated {
            val amps = player.loadAmps()
            player_visualizer.setWaveForm(amps, player.tickDuration)
        }
    }

    private fun initAudioRecorder() {
        viewModel.hasAudioRecording = false
        timeline_tv.text = getString(R.string.zero_seconds)
        viewModel.recordFileName = getRecordFileName(System.currentTimeMillis())
        recorder = Recorder.getInstance(mContext)
            .init(getRecordFileByFileName(viewModel.recordFileName).toString())
        player_visualizer.visibility = INVISIBLE
        include_play_recording.visibility = INVISIBLE
        delete_recording_btn.visibility = INVISIBLE
        record_audio_button.visibility = VISIBLE
        recorder_visualizer.visibility = VISIBLE

        recorder.onStart = {
            handleRecordAudioAnimations(true)
        }
        recorder.onStop = {
            handleRecordAudioAnimations(false)
            recorder_visualizer.clear()
            timeline_tv.text = 0L.formatAsTime()
            initAudioPlayer()
            initPlayerUI()
            player_visualizer.visibility = VISIBLE
            record_audio_button.visibility = INVISIBLE
            recorder_visualizer.visibility = INVISIBLE
            include_play_recording.visibility = VISIBLE
            delete_recording_btn.visibility = VISIBLE
            viewModel.hasAudioRecording = true
            viewModel.validate()
        }

        recorder.onAmpListener = {
            requireActivity().runOnUiThread {
                if (recorder.isRecording) {
                    timeline_tv.text = recorder.getCurrentTime().formatAsTime()
                    recorder_visualizer.addAmp(it, recorder.tickDuration)
                }
            }
        }
    }

    private fun initAudioPlayer() {
        player = AudioPlayer.getInstance(mContext)
            .init(getRecordFileByFileName(viewModel.recordFileName)).apply {
                onStart =
                    {
                        play_button.icon =
                            mContext.getDrawableCompat(R.drawable.ic_pause_24)
                    }
                onStop = {
                    play_button.icon =
                        mContext.getDrawableCompat(R.drawable.ic_play_arrow_24)
                }
                onPause = {
                    play_button.icon =
                        mContext.getDrawableCompat(R.drawable.ic_play_arrow_24)
                }
                onResume =
                    {
                        play_button.icon =
                            mContext.getDrawableCompat(R.drawable.ic_pause_24)
                    }
                onProgress = { time, isPlaying -> updateTime(time, isPlaying) }
            }
    }

    private fun handleRecordAudioAnimations(recording: Boolean) {
        if (recording) {
            record_audio_button.playAnimation()
            stop_button.visibility = VISIBLE
        } else {
            stop_button.visibility = INVISIBLE
            record_audio_button.cancelAnimation()
            record_audio_button.progress = 0.08f
        }
    }

    private fun updateTime(time: Long, isPlaying: Boolean) {
        timeline_tv.text = time.formatAsTime()
        player_visualizer.updateTime(time, isPlaying)
    }

    override fun onPause() {
        super.onPause()
        try {
            player.pause()
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            player.release()
            recorder.release()
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val SEEK_OVER_AMOUNT = 5000
    }
}
