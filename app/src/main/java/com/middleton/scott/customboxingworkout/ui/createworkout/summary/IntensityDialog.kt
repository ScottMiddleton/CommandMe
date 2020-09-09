import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.middleton.scott.commandMeBoxing.R
import kotlinx.android.synthetic.main.dialog_intensity.*
import kotlinx.android.synthetic.main.dialog_number_picker_mins_secs.cancel_btn

class IntensityDialog(
    private val intensityProgress: Int,
    private val onSave: ((Int) -> Unit),
    private val onCancel: (() -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_intensity, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intensity_seekbar.progress = intensityProgress
        intensity_value_pb.progress = intensityProgress
        intensity_value_tv.text = intensityProgress.toString()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Show soft keyboard automatically and request focus to field
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
        setClickListeners()
    }

    private fun setClickListeners() {
        intensity_seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                intensity_value_pb.progress = progress
                intensity_value_tv.text = progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        save_btn.setOnClickListener {
            onSave(intensity_seekbar.progress)
            dismiss()
        }

        cancel_btn.setOnClickListener {
            onCancel()
            dismiss()
        }
    }
}