import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Nullable
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.datasource.local.model.Command
import com.middleton.scott.cmboxing.ui.createworkout.NumberPickerSecondsDialog
import kotlinx.android.synthetic.main.dialog_save_command.*
import java.io.IOException

const val numberOfFieldsToValidate = 2

class SaveCommandDialog(
    private val audioFileDirectory: String,
    private val isEditMode: Boolean,
    private val command: Command,
    private val onSave: ((Command) -> Unit),
    private val onDelete: (() -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_save_command, container)
    }

    private var timeToCompleteMillis: Long = 0L
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

        timeToCompleteMillis = command.timeToCompleteMillis

        if (isEditMode) {
            name_et.setText(command.name)
            time_to_complete_et.setText(getSecondsTextFromMillis(timeToCompleteMillis))
            delete_btn.text = view.context.getString(R.string.cancel)
        }
        setClickListeners()
    }

    private fun setClickListeners() {
        save_btn.setOnClickListener {
            saveAttempted = true
            if (validateFields()) {
                command.name = name_et.text.toString()
                command.timeToCompleteMillis = timeToCompleteMillis
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

            var millis = 2000L
            if (!time_to_complete_et.text.isNullOrBlank()) {
                millis = timeToCompleteMillis
            }

            NumberPickerSecondsDialog(millis, { newMillis ->
                timeToCompleteMillis = newMillis
                time_to_complete_et.setText(getSecondsTextFromMillis(newMillis))
                name_et.clearFocus()
                if (saveAttempted) {
                    if (timeToCompleteMillis <= 0) {
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

    private fun getSecondsTextFromMillis(millis: Long): String {
        return if ((millis % 1000) == 0L) {
            (millis / 1000).toString()
        } else {
            (millis / 1000.0).toString()
        }
    }

    private fun validateFields(): Boolean {
        var validFieldsCount = numberOfFieldsToValidate

        if (name_et.text.isNullOrBlank()) {
            name_til.error = getString(R.string.this_is_a_required_field)
            validFieldsCount--
        }

        if (timeToCompleteMillis <= 0) {
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
}