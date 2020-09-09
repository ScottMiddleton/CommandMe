import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.middleton.scott.commandMeBoxing.R
import kotlinx.android.synthetic.main.dialog_number_picker_mins_secs.*
import kotlinx.android.synthetic.main.dialog_save_combination.save_btn

class NumberPickerMinutesSecondsDialog(
    private val title: String,
    private val seconds: Int,
    private val onSave: ((Int) -> Unit),
    private val onCancel: (() -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_number_picker_mins_secs, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_tv.text = title
        mins_np.maxValue = 59
        mins_np.minValue = 0
        secs_np.maxValue = 59
        secs_np.minValue = 0
        setNumberPickers(seconds)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Show soft keyboard automatically and request focus to field
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
        setClickListeners()
    }

    private fun setClickListeners() {
        save_btn.setOnClickListener {
            onSave(getSeconds())
            dismiss()
        }

        cancel_btn.setOnClickListener {
            onCancel()
            dismiss()
        }
    }

    private fun setNumberPickers(totalSeconds: Int) {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds - (minutes * 60)
        mins_np.value = minutes
        secs_np.value = seconds
    }

    private fun getSeconds(): Int {
        val minsToSeconds = mins_np.value * 60
        return minsToSeconds + secs_np.value
    }
}