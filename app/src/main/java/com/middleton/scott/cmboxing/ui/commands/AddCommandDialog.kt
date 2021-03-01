import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.ui.commands.CommandsViewModel
import com.middleton.scott.cmboxing.ui.createworkout.CreateWorkoutSharedViewModel
import com.middleton.scott.cmboxing.ui.createworkout.NumberPickerMinutesSecondsDialog
import com.middleton.scott.cmboxing.ui.createworkout.NumberPickerSecondsDialog
import com.middleton.scott.cmboxing.utils.DateTimeUtils
import com.middleton.scott.cmboxing.utils.MediaRecorderManager
import kotlinx.android.synthetic.main.dialog_save_command.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.File
import java.io.IOException

const val numberOfFieldsToValidate = 2

class AddCommandDialog(
    private val audioFileDirectory: String,
    private val isEditMode: Boolean,
    private val command: Command,
    private val onSave: ((Command) -> Unit),
    private val onDelete: (() -> Unit)
) : DialogFragment() {
    private val viewModel by lazy { requireParentFragment().getViewModel<CommandsViewModel>() }
    private var mediaRecorder = MediaRecorder()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_save_command, container)
    }

    private var timeToCompleteSecs: Int = 0
    private var saveAttempted = false
    private var mediaPlayer = MediaPlayer()

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Show soft keyboard automatically and request focus to field
        name_et.requestFocus()
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        play_audio_lottie.speed = 3f
        play_audio_lottie.setMinAndMaxFrame(30, 60)

        dialog?.setCanceledOnTouchOutside(false)

        timeToCompleteSecs = command.timeToCompleteSecs

        if (isEditMode) {
            name_et.setText(command.name)
            time_to_complete_et.setText(DateTimeUtils.toMinuteSeconds(timeToCompleteSecs))
            delete_btn.text = view.context.getString(R.string.cancel)
        }
        setClickListeners()
    }

    private fun setClickListeners() {
        save_btn.setOnClickListener {
            saveAttempted = true
            if (validateFields()) {
                command.name = name_et.text.toString()
                command.timeToCompleteSecs = timeToCompleteSecs
                onSave(command)
                dismiss()
            }
        }

        delete_btn.setOnClickListener {
            if (isEditMode) {
                dismiss()
            } else {
                onDelete()
                dismiss()
            }
        }


        play_cl.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                handlePlayAnimationLottie(true, play_audio_lottie)
                handlePlayAnimationLottie(false, play_audio_lottie)
                startPlaying()
            } else {
                stopPlaying()
            }
        }

        time_to_complete_et.setOnClickListener {
            val imm: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)

            var secs = 10
            if (!time_to_complete_et.text.isNullOrBlank()) {
                secs = timeToCompleteSecs
            }

            NumberPickerMinutesSecondsDialog("Title", secs, { newSecs ->
                timeToCompleteSecs = newSecs
                time_to_complete_et.setText(DateTimeUtils.toMinuteSeconds(newSecs))
                name_et.clearFocus()
                if (saveAttempted) {
                    if (timeToCompleteSecs <= 0) {
                        time_to_complete_til.error = getString(R.string.greater_than_zero)
                    } else {
                        time_to_complete_til.isErrorEnabled = false
                    }
                }
            }, {

            }).show(childFragmentManager, "")
        }

        name_et.doAfterTextChanged {
            if (saveAttempted) {
                if (name_et.text.isNullOrBlank()) {
                    name_til.error = getString(R.string.this_is_a_required_field)
                } else {
                    name_til.isErrorEnabled = false
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        var validFieldsCount = numberOfFieldsToValidate

        if (name_et.text.isNullOrBlank()) {
            name_til.error = getString(R.string.this_is_a_required_field)
            validFieldsCount--
        }

        if (timeToCompleteSecs <= 0) {
            time_to_complete_til.error = getString(R.string.greater_than_zero)
            validFieldsCount--
        }

        return validFieldsCount == numberOfFieldsToValidate
    }

    private fun startPlaying() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFileDirectory)
                prepare()
                this.setOnCompletionListener {
                    handlePlayAnimationLottie(true, play_audio_lottie)
                }
                start()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        handlePlayAnimationLottie(true, play_audio_lottie)
        mediaPlayer.stop()
        mediaPlayer.reset()
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
//                        showSaveCombinationDialog()
                    } else {
                        val file = File(viewModel.audioFileCompleteDirectory)
                        file.delete()
                        Toast.makeText(context, "Recording too short. Hold the microphone to record a command.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, getString(R.string.recording_too_short), Toast.LENGTH_LONG).show()
                    mediaRecorder = MediaRecorder()
                }
            }
            viewModel.recording = false
        }
    }

    private fun handlePlayAnimationLottie(
        playInReverse: Boolean,
        playAudioLottie: LottieAnimationView?
    ) {
        if (playInReverse) {
            playAudioLottie?.setMinAndMaxFrame(0, 30)
            playAudioLottie?.playAnimation()
        } else {
            playAudioLottie?.setMinAndMaxFrame(30, 60)
            playAudioLottie?.playAnimation()
        }
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